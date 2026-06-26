package com.ayush.ems.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ayush.ems.entities.Holiday;

public interface HolidayDao extends JpaRepository<Holiday, Integer> {
	   List<Holiday> findByDateGreaterThanEqual(LocalDate date);
	   
		 @Query(value = "SELECT * FROM holiday WHERE company_id = :company_id", nativeQuery = true)
		 List<Holiday> findAllHolidaysByCompanyInfo(@Param("company_id") String company_id);
		 
		 @Query(value = "SELECT * FROM holiday h " +
	               "WHERE h.company_id = :companyId " +
	               "AND h.name IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday') " +
	               "AND h.type = 'Weekend'", nativeQuery = true)
	List<Holiday> findWeekendDaysByCompanyId(@Param("companyId") String companyId);

		 
		 @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h WHERE h.company_id = :companyId AND h.date = :date AND h.type IN :types")
		 boolean existsByCompanyIdAndDateAndTypeIn(@Param("companyId") String companyId, @Param("date") LocalDate date, @Param("types") List<String> types);


		     // Fetch holidays for a company within the date range
		     @Query("SELECT h FROM Holiday h WHERE h.company_id = :companyId " +
		            "AND h.date BETWEEN :startDate AND :endDate " +
		            "AND (h.type = 'Public Holiday' OR h.type = 'Festival')")
		     List<Holiday> getCompanyHolidays(@Param("companyId") String companyId,
		                                       @Param("startDate") LocalDate startDate,
		                                       @Param("endDate") LocalDate endDate);

		     // Fetch all weekends (e.g., Saturday, Sunday) for a company
		     @Query("SELECT h.name FROM Holiday h WHERE h.company_id = :companyId AND h.type = 'Weekend'")
		     List<String> getCompanyWeekendDays(@Param("companyId") String companyId);
}
