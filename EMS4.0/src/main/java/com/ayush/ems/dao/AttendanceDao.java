package com.ayush.ems.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Attendance;

@Repository
public interface AttendanceDao extends JpaRepository<Attendance, Long> {
	List<Attendance> findByDate(LocalDate date);

	@Query(value = "SELECT * FROM attendance WHERE DATE(adddate) = CURDATE() AND employee_id = :emp_id "
			+ "AND status NOT IN ('Suspended','Attendance Update','Leave Update', 'Attendance Rejected', 'Leave Rejected')  ORDER BY adddate DESC LIMIT 1", nativeQuery = true)
	Optional<Attendance> findLatestAttendanceToday(@Param("emp_id") int emp_id);

	@Query(value = "SELECT * FROM attendance a WHERE a.employee_id = :emp_id AND a.status NOT IN ('Suspended','Attendance Update','Leave Update', 'Attendance Rejected', 'Leave Rejected', 'Withdrawn Suspended')  ORDER BY adddate", nativeQuery = true)
	List<Attendance> findDistinctStatusByEmployeeId(@Param("emp_id") int emp_id);

	@Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND date = :date ORDER BY adddate DESC LIMIT 1", nativeQuery = true)
	Optional<Attendance> findByEmployeeIdAndDate(@Param("employeeId") Integer employeeId,
			@Param("date") LocalDate date);

	@Query(value = "select count(1) from attendance", nativeQuery = true)
	long findAttendanceCount();

	@Query(value = "select sno from attendance order by sno desc limit 1", nativeQuery = true)
	long findLastSno();
	
	@Query(value = "select request_id from attendance order by request_id desc limit 1", nativeQuery = true)
	String findLastRequestId();


    // ✅ For marking non-finalized statuses as "Withdrawn Suspended"
    @Query(value = "SELECT * FROM attendance a WHERE a.date = :date " +
            "AND a.status NOT IN ('Suspended','Attendance Update','Leave Update','Attendance Rejected','Leave Rejected','Withdrawn Suspended','Absent'," +
            "'WFH Request','Leave Request','Present') ORDER BY a.adddate DESC", nativeQuery = true)
    List<Attendance> findAttendanceByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT u.id, u.company_id, u.username, u.team " +
            "FROM employee u " +
            "WHERE u.status = 'ACTIVE' " +
	    "AND u.company_id = :companyId "+
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM attendance a " +
            "    WHERE a.employee_id = u.id " +
            "    AND DATE(a.date) = CURDATE() " +
            "    AND a.status IN ( " +
            "        'Suspended', 'Attendance Update', 'Leave Update', 'Attendance Rejected', " +
            "        'Leave Rejected', 'Withdrawn Suspended', 'Absent', " +
            "        'WFH Request', 'Leave Request', 'Present' " +
            "    ) " +
            ") " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM holiday h " +
            "    WHERE DATE(h.date) = CURDATE() " +
            "    AND h.company_id = u.company_id " +
            ")", 
           nativeQuery = true)
    List<Object[]> findEmployeesWithoutValidAttendanceToday(String companyId);
	@Query(value = "SELECT * FROM attendance a WHERE a.employee_id = :employeeId AND a.date = :date and a.status = :status order by adddate desc LIMIT 1", nativeQuery = true)
	Optional<Attendance> findAttendanceByEmployeeIdAndDate(int employeeId, LocalDate date, String status);

	@Query(value = "SELECT * FROM attendance a " + "WHERE a.employee_id = :employeeId " + "AND a.date = :date "
			+ "AND (a.status = 'Attendance Update' OR a.status = 'Leave Update') "
			+ "AND (a.attendance_request = 1 OR a.leave_request = 1) "
			+ "ORDER BY a.adddate DESC LIMIT 1", nativeQuery = true)
	Optional<Attendance> findPendingUpdateOrLeaveRequest(int employeeId, LocalDate date);

	@Query(value = "SELECT * FROM attendance a " + "WHERE a.employee_id = :employeeId " + "AND a.date = :date "
			+ "AND a.status NOT IN ('Suspended','Attendance Update','Leave Update', 'Attendance Rejected', 'Leave Rejected') "
			+ "ORDER BY a.adddate DESC", nativeQuery = true)
	List<Attendance> findActiveAttendance(int employeeId, LocalDate date);


	// Query to find all pending requests for a specific team, grouped by request_id
	@Query(value = "SELECT a.request_id, e.username, e.email, a.status, a.reason, COUNT(*) AS total_days, "
	        + "MIN(a.from_date) AS from_date, MAX(a.to_date) AS to_date, "
	        + "MAX(a.adddate) AS add_time, MAX(a.editdate), employee_id, approved_or_reject_or_pending, request_type, file_name, file_id "
	        + "FROM attendance a "
	        + "JOIN employee e ON a.employee_id = e.id "
	        + "WHERE (a.attendance_request = 1 OR a.leave_request = 1 OR a.wfh_request = 1 OR a.withdrawn_request = 1) "
	        + "AND a.approved_or_reject_or_pending = 'Pending' "
	        + "AND e.team = :team "
	        + "GROUP BY a.request_id, e.username, e.email, a.status, a.reason, a.employee_id, approved_or_reject_or_pending, request_type, file_name, file_id "
	        + "LIMIT 0, 100000", nativeQuery = true)
	List<Object[]> findPendingRequestsByTeam(@Param("team") String team);

	// Fallback: If manager has no team, return all pending requests
	@Query(value = "SELECT a.request_id, e.username, e.email, a.status, a.reason, COUNT(*) AS total_days, "
	        + "MIN(a.from_date) AS from_date, MAX(a.to_date) AS to_date, "
	        + "MAX(a.adddate) AS add_time, MAX(a.editdate), a.employee_id, approved_or_reject_or_pending, request_type, file_name, file_id "
	        + "FROM attendance a "
	        + "JOIN employee e ON a.employee_id = e.id "
	        + "WHERE (a.attendance_request = 1 OR a.leave_request = 1 OR a.wfh_request = 1 OR a.withdrawn_request = 1) "
	        + "AND a.approved_or_reject_or_pending = 'Pending' "
	        + "GROUP BY a.request_id, e.username, e.email, a.status, a.reason,  a.employee_id, approved_or_reject_or_pending, request_type , file_name, file_id"
	        + "LIMIT 0, 100000", nativeQuery = true)
	List<Object[]> findRequestsForNoTeamManager();

	// Query to find pending requests for employees who don't have a team assigned
	@Query(value = "SELECT a.request_id, e.username, e.email, a.status, a.reason, COUNT(*) AS total_days, "
	        + "MIN(a.from_date) AS from_date, MAX(a.to_date) AS to_date, "
	        + "MAX(a.adddate) AS add_time, MAX(a.editdate), a.employee_id, approved_or_reject_or_pending, request_type, file_name, file_id "
	        + "FROM attendance a "
	        + "JOIN employee e ON a.employee_id = e.id "
	        + "WHERE (a.attendance_request = 1 OR a.leave_request = 1 OR a.wfh_request = 1 OR a.withdrawn_request = 1) "
	        + "AND a.approved_or_reject_or_pending = 'Pending' "
	        + "AND (e.team IS NULL OR e.team = 'No Record Found' OR e.team = '0') "
	        + "GROUP BY a.request_id, e.username, e.email, a.status, a.reason ,a.employee_id, approved_or_reject_or_pending, request_type, file_name, file_id "
	        + "LIMIT 0, 100000", nativeQuery = true)
	List<Object[]> findEmployeeAttendanceWithoutTeam();


	@Query(value = "SELECT a.request_id, e.username, e.email, a.status, a.reason, COUNT(*) AS total_days, "
	        + "MAX(a.from_date) AS from_date, MAX(a.to_date) AS to_date, "
	        + "MAX(a.adddate) AS add_time, MAX(a.editdate) AS edit_time, a.employee_id, approved_or_reject_or_pending, request_type, file_name, file_id "
	        + "FROM attendance a "
	        + "JOIN employee e ON a.employee_id = e.id "
	        + "WHERE a.manager_id = :managerId "
	        + "GROUP BY a.request_id, e.username, e.email, a.status, a.reason, a.employee_id, approved_or_reject_or_pending, request_type, file_name, file_id "
	        + "ORDER BY MAX(a.adddate) DESC", nativeQuery = true)
	List<Object[]> findRequestHistory(@Param("managerId") int managerId);




	@Query(value = "SELECT a.* FROM employeedetail ed, attendance a " + "WHERE a.employee_id = ed.id "
			+ "AND ed.team NOT LIKE 'T%' " + "AND (a.status = 'Attendance Update' OR a.status = 'Leave Update') "
			+ "AND (a.attendance_request = 1 OR a.leave_request = 1) " + "AND ed.company_id = :companyId "
			+ "AND ed.admin_id = :adminId", nativeQuery = true)
	List<Attendance> findEmployeeAttendanceWithoutTeam(@Param("companyId") String companyId,
			@Param("adminId") int adminId);

	@Query(value = "SELECT COALESCE(SUM(a.total_days), 0) FROM attendance a WHERE a.employee_id = :employeeId "
			+ "AND a.leave_request = true AND a.approved_or_reject_or_pending = 'Approved' "
			+ "AND YEAR(a.adddate) = :year",nativeQuery = true)
	int countConsumedLeaves(@Param("employeeId") int employeeId, @Param("year") int year);

	@Query(value = "SELECT COALESCE(SUM(a.total_days), 0) FROM attendance a  WHERE a.status = 'WFH' "
			+ "AND a.approved_or_reject_or_pending = 'Approved' AND a.employee_id = :employeeId "
			+ "AND YEAR(a.adddate) = :year",nativeQuery = true)
	int countConsumedWfh(@Param("employeeId") int employeeId, @Param("year") int year);

	@Query(value = "SELECT COALESCE(SUM(a.total_days), 0) FROM attendance a " + "WHERE a.status='Leave Request' AND a.leave_request = 1 "
			+ "AND a.approved_or_reject_or_pending = 'Pending' AND a.employee_id = :employeeId", nativeQuery = true)
	int countPendingLeaves(@Param("employeeId") int employeeId);

	@Query(value = "SELECT COALESCE(SUM(a.total_days), 0) FROM attendance a WHERE a.status = 'WFH Request' AND a.wfh_request = 1 "
			+ "AND a.approved_or_reject_or_pending = 'Pending' AND a.employee_id = :employeeId", nativeQuery = true)
	int countPendingWfh(@Param("employeeId") int employeeId);

	@Query(value = " SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM attendance a"
			+ "   WHERE a.employee_id = :employeeId AND a.approved_or_reject_or_pending = 'Pending'"
			+ "     AND (a.leave_request = true OR a.wfh_request       = true"
			+ "       OR a.attendance_request= true) AND a.from_date  <= :date"
			+ "     AND a.to_date    >= :date", nativeQuery = true)
	int existsAnyPendingRequestOnDate(@Param("employeeId") int employeeId, @Param("date") LocalDate date);

	@Query(value = "SELECT * FROM attendance a WHERE a.employee_id = :employeeId AND (a.leave_request = true OR a.wfh_request = true OR a.attendance_request = true) ORDER BY a.date DESC", nativeQuery = true)
	List<Attendance> findByEmployeeIdOrderByDateDesc(@Param("employeeId") Integer employeeId);

	@Query(value = "SELECT * FROM attendance a WHERE a.request_id = :request_id AND a.employee_id = :empId",nativeQuery = true)
	List<Attendance> findByRequestIdAndEmployeeId(@Param("request_id") String request_id, @Param("empId") int empId);
	
	@Query(value = "SELECT * FROM attendance a WHERE a.employee_id = :empId AND YEAR(a.adddate) = :year and status not in ('Present','Absent') ORDER BY a.adddate DESC, a.request_id DESC", nativeQuery = true)
	List<Attendance> findByEmployeeId(@Param("empId") int empId, @Param("year") int year);

	
	@Query(value = "SELECT * FROM attendance a WHERE a.request_id = :request_id AND a.sno = :sno",nativeQuery = true)
	Optional<Attendance> findByRequestIdAndSno(@Param("request_id") String request_id, @Param("sno") int sno);
	
	
	@Query(value = "SELECT * FROM attendance a WHERE a.request_id = :request_id",nativeQuery = true)
	List<Attendance> findByRequestId(@Param("request_id") String request_id);
	
	@Query(value = "DELETE FROM attendance a WHERE a.request_id = :request_id AND a.sno = :sno",nativeQuery = true)
	Optional<Attendance> deleteByRequestId(@Param("request_id") String request_id, @Param("sno") int sno);
}
