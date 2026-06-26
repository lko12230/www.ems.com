package com.ayush.ems.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ayush.ems.entities.UploadHistory;

public interface UploadHistoryReportDao extends JpaRepository<UploadHistory, Long> {
	@Query(value = "SELECT * FROM upload_history WHERE company_id = :companyId ORDER BY upload_time DESC", nativeQuery = true)
	List<UploadHistory> findByCompanyIdOrderByUploadTimeDesc(@Param("companyId") String companyId);
}
