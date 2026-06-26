package com.ayush.ems.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Tasks;
@Repository
public interface TaskDao extends JpaRepository<Tasks, String>{
	
	
	@Query(value = "SELECT t.* FROM tasks t, employee e " +
	        "WHERE t.id = e.id " +
	        "AND t.task_id = :taskId " +
	        "AND (e.role = 'ROLE_MANAGER' " +
	        "OR (e.role IN ('ROLE_HR', 'ROLE_ADMIN', 'ROLE_USER', 'ROLE_IT') " +
	        "AND NOT (t.task_closed = '1' AND t.task_reopen = '0'))) " +
	        "ORDER BY t.task_id", nativeQuery = true)
	Optional<Tasks> findTasksByTaskId(@Param("taskId") String taskId);
	
	
	@Query(value = "SELECT t.* FROM tasks t, employee e " +
	        "WHERE t.id = e.id " +
	        "AND t.task_id = :taskId " +
	        "ORDER BY t.task_id", nativeQuery = true)
	Optional<Tasks> findTasksByTaskIdByManager(@Param("taskId") String taskId);

	
	  // Fetch last inserted task ID (order by descending)
    @Query("SELECT t.taskId FROM Tasks t ORDER BY t.taskId DESC")
    List<String> findLatestTaskId(Pageable pageable);
    
    @Query(value="SELECT " +
            "COALESCE(SUM(CASE WHEN t.task_Pending = true THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.task_Completed = true THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.task_Overdue = true THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.task_In_Progress = true THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.task_Deleted = true THEN 1 ELSE 0 END), 0) " +
            "FROM tasks t WHERE t.id = :id", nativeQuery = true)
 List<Object[]> fetchTaskCounts(@Param("id") int id);
 

 @Query("SELECT t FROM Tasks t WHERE t.id = :userId")
 List<Tasks> findTasksByUserId(@Param("userId") int userId);
 
 @Query(value = "SELECT * FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 0 " +
         "AND t.task_in_progress = 1 " +
         "AND t.task_overdue = 0 " +
         "AND t.task_pending = 0", 
 nativeQuery = true)
 List<Tasks> findByUserIdAndTaskInProgressTrue(Integer userId);
 
 @Query(value = "SELECT * FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 0 " +
         "AND t.task_in_progress = 0 " +
         "AND t.task_overdue = 0 " +
         "AND t.task_pending = 1", 
 nativeQuery = true)
 List<Tasks> findByUserIdAndTaskPendingTrue(Integer userId);

 @Query(value = "SELECT * FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 1 " +
         "AND t.task_in_progress = 0 " +
         "AND t.task_overdue = 0 " +
         "AND t.task_pending = 0", 
 nativeQuery = true)
List<Tasks> findByUserIdAndTaskCompletedTrue(@Param("userId") Integer userId);

 @Query(value = "SELECT * FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 0 " +
         "AND t.task_in_progress = 0 " +
         "AND t.task_overdue = 1 " +
         "AND t.task_pending = 0", 
 nativeQuery = true)
List<Tasks> findByUserIdAndTaskOverdueTrue(@Param("userId") Integer userId);

// @Query(value = "SELECT * FROM Tasks t " +
//	       "JOIN EmployeeDetail ed ON t.id = ed.id " +
//	       "JOIN Employee e ON e.id = ed.id " +
//	       "WHERE t.id = :empId AND ed.company_id = :companyId",nativeQuery = true)
//	List<Tasks> findTasksByEmpIdAndCompanyId(@Param("empId") int empId, 
//	                                         @Param("companyId") String companyId);
// 
// @Query(value = "SELECT t.* FROM Tasks t " +
//         "JOIN EmployeeDetail ed ON t.who_assigned_task = ed.id " +
//         "JOIN Employee e ON e.id = ed.id " +
//         "WHERE t.who_assigned_task = :empId AND ed.company_id = :companyId " +
//         "UNION " +
//         "SELECT t.* FROM Tasks t " +
//         "JOIN EmployeeDetail ed ON t.id = ed.id " +
//         "JOIN Employee e ON e.id = ed.id " +
//         "WHERE t.id = :empId AND ed.company_id = :companyId", 
// nativeQuery = true)
//List<Tasks> managerFindTasksByEmpIdAndCompanyId(@Param("empId") int empId, 
//                                          @Param("companyId") String companyId);

// List<Tasks> findByTaskIdContainingIgnoreCase(String taskId);
 
 @Query("SELECT t FROM Tasks t WHERE LOWER(t.taskId) LIKE LOWER(CONCAT('%', :taskId, '%')) AND t.id = :userId")
 List<Tasks> searchByTaskIdAndUserId(@Param("taskId") String taskId, @Param("userId") int userId);
 
 @Query(value = "SELECT t.* FROM Tasks t JOIN EmployeeDetail u ON u.id = t.who_assigned_task " +
	       "WHERE LOWER(t.task_id) LIKE LOWER(CONCAT('%', :taskId, '%')) " +
	       "AND u.role = 'ROLE_MANAGER' " +
	       "AND u.id = :userId",nativeQuery = true)
	List<Tasks> managerSearchByTaskIdAndUserId(@Param("taskId") String taskId, @Param("userId") int userId);

 @Query(value = "SELECT count(1) FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 0 " +
         "AND t.task_in_progress = 1 " +
         "AND t.task_overdue = 0 " +
         "AND t.task_pending = 0", 
 nativeQuery = true)
Integer countTasksInProgress(@Param("userId") Integer userId);

@Query(value = "SELECT count(1) FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 0 " +
         "AND t.task_in_progress = 0 " +
         "AND t.task_overdue = 0 " +
         "AND t.task_pending = 1", 
 nativeQuery = true)
Integer countTasksPending(@Param("userId") Integer userId);

@Query(value = "SELECT count(1) FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 1 " +
         "AND t.task_in_progress = 0 " +
         "AND t.task_overdue = 0 " +
         "AND t.task_pending = 0", 
 nativeQuery = true)
Integer countTasksCompleted(@Param("userId") Integer userId);

@Query(value = "SELECT count(1) FROM tasks t WHERE t.id = :userId " +
         "AND t.task_completed = 0 " +
         "AND t.task_in_progress = 0 " +
         "AND t.task_overdue = 1 " +
         "AND t.task_pending = 0", 
 nativeQuery = true)
Integer countTasksOverdue(@Param("userId") Integer userId);

@Query(value = "SELECT COUNT(1) FROM tasks t WHERE t.id = :userId AND t.task_completed = 1 "
		+ "AND t.task_in_progress = 0 AND t.task_overdue = 0 AND t.task_pending = 0 "
		+ "AND t.task_completed_date <= t.task_end_date",nativeQuery = true)
Integer getOnTimeCompletedCount(@Param("userId") Integer userId);

@Query(value = "SELECT COUNT(1) FROM tasks t WHERE t.id = :userId AND t.task_completed = 1 "
		+ "AND t.task_in_progress = 0 AND t.task_overdue = 0 AND t.task_pending = 0 "
		+ "AND t.task_completed_date > t.task_end_date",nativeQuery = true)
Integer getLateCompletedCount(@Param("userId") Integer userId);


@Query(value = "SELECT COUNT(1) FROM tasks t WHERE t.id = :userId", nativeQuery = true)
Integer countTotalTasksByUserId(@Param("userId") Integer userId);

@Query(value = "SELECT DATE_FORMAT(t.task_completed_date, '%Y-%m') AS month, COUNT(t.id) " +
        "FROM tasks t WHERE t.id = :taskId " +
        "AND t.task_completed = 1 " +
        "AND t.task_completed_date IS NOT NULL " +
        "GROUP BY month ORDER BY month ASC", nativeQuery = true)
List<Object[]> findTaskCompletionTrends(@Param("taskId") Integer taskId);

//@Query(value = "SELECT DATE_FORMAT(t.task_assigned_date, '%Y-%m') AS month, " +
//        "COUNT(t.id) AS total_tasks, " +
//        "SUM(CASE WHEN t.task_completed = 1 THEN 1 ELSE 0 END) AS completed_tasks, " +
//        "SUM(CASE WHEN t.task_pending = 1 THEN 1 ELSE 0 END) AS pending_tasks, " +
//        "SUM(CASE WHEN t.task_in_progress = 1 THEN 1 ELSE 0 END) AS in_progress_tasks, " +
//        "SUM(CASE WHEN t.task_overdue = 1 THEN 1 ELSE 0 END) AS overdue_tasks, " +
//        "SUM(CASE WHEN t.task_completed = 1 AND DATE(t.task_completed_date) > DATE(t.task_end_date) THEN 1 ELSE 0 END) AS late_completed_tasks, " +
//        "SUM(CASE WHEN t.task_completed = 1 AND DATE(t.task_completed_date) <= DATE(t.task_end_date) THEN 1 ELSE 0 END) AS on_time_completed_tasks," +
//        "e.username "+
//        "FROM tasks t " +
//        "JOIN employeedetail e ON t.id = e.id " +
//        "WHERE e.id = :userId " +
//        "GROUP BY month, e.username " +
//        "ORDER BY month ASC", 
//nativeQuery = true)
//List<Object[]> getTaskPerformanceData(@Param("userId") Integer userId);

@Query(value = "WITH months AS ( " +
	    "SELECT DATE_FORMAT(DATE_SUB(NOW(), INTERVAL n MONTH), '%Y-%m') AS month " +
	    "FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 " +
	    "UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 " +
	    "UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11) t " +
	    "), " +
	    "user_info AS ( " +
	    "SELECT DISTINCT e.id, COALESCE(e.username, 'Username') AS username " +
	    "FROM employeedetail e WHERE e.id = :userId " +
	    ") " +
	    "SELECT m.month, COALESCE(COUNT(t.task_id), 0) AS total_tasks, " +
	    "COALESCE(SUM(CASE WHEN t.task_completed = 1 THEN 1 ELSE 0 END), 0) AS completed_tasks, " +
	    "COALESCE(SUM(CASE WHEN t.task_pending = 1 THEN 1 ELSE 0 END), 0) AS pending_tasks, " +
	    "COALESCE(SUM(CASE WHEN t.task_in_progress = 1 THEN 1 ELSE 0 END), 0) AS in_progress_tasks, " +
	    "COALESCE(SUM(CASE WHEN t.task_overdue = 1 THEN 1 ELSE 0 END), 0) AS overdue_tasks, " +
	    "COALESCE(SUM(CASE WHEN t.task_completed = 1 AND DATE(t.task_completed_date) > DATE(t.task_end_date) THEN 1 ELSE 0 END), 0) AS late_completed_tasks, " +
	    "COALESCE(SUM(CASE WHEN t.task_completed = 1 AND DATE(t.task_completed_date) <= DATE(t.task_end_date) THEN 1 ELSE 0 END), 0) AS on_time_completed_tasks, " +
	    "ui.username, " +
	    "COALESCE(GROUP_CONCAT(CASE WHEN t.task_completed = 1 THEN t.task_id END ORDER BY t.task_id ASC SEPARATOR ', '), '') AS completed_task_list, " +
	    "COALESCE(GROUP_CONCAT(CASE WHEN t.task_pending = 1 THEN t.task_id END ORDER BY t.task_id ASC SEPARATOR ', '), '') AS pending_task_list, " +
	    "COALESCE(GROUP_CONCAT(CASE WHEN t.task_in_progress = 1 THEN t.task_id END ORDER BY t.task_id ASC SEPARATOR ', '), '') AS in_progress_task_list, " +
	    "COALESCE(GROUP_CONCAT(CASE WHEN t.task_overdue = 1 THEN t.task_id END ORDER BY t.task_id ASC SEPARATOR ', '), '') AS overdue_task_list, " +
	    "COALESCE(GROUP_CONCAT(CASE WHEN t.task_completed = 1 AND DATE(t.task_completed_date) > DATE(t.task_end_date) THEN t.task_id END ORDER BY t.task_id ASC SEPARATOR ', '), '') AS late_completed_task_list, " +
	    "COALESCE(GROUP_CONCAT(CASE WHEN t.task_completed = 1 AND DATE(t.task_completed_date) <= DATE(t.task_end_date) THEN t.task_id END ORDER BY t.task_id ASC SEPARATOR ', '), '') AS on_time_completed_task_list, " +
	    "COALESCE(GROUP_CONCAT(t.task_id ORDER BY t.task_id ASC SEPARATOR ', '), '') AS all_task_list " +
	    "FROM months m " +
	    "INNER JOIN user_info ui ON 1=1 " +
	    "LEFT JOIN tasks t ON DATE_FORMAT(t.task_assigned_date, '%Y-%m') = m.month AND t.id = ui.id " +
	    "GROUP BY m.month, ui.username " +
	    "ORDER BY YEAR(STR_TO_DATE(m.month, '%Y-%m')) ASC, " +
	    "MONTH(STR_TO_DATE(m.month, '%Y-%m')) ASC", nativeQuery = true)
	List<Object[]> getTaskPerformanceData(@Param("userId") int userId);
	
	
	@Modifying
	  @Query(value = "UPDATE tasks t SET t.task_closed = true, t.task_reopen = false WHERE t.task_id = :taskId",nativeQuery = true)
	    int updateTaskStatusClosed(@Param("taskId") String taskId);
	
	@Modifying
	  @Query(value = "UPDATE tasks t SET t.task_reopen = true, t.task_closed = false WHERE t.task_id = :taskId",nativeQuery = true)
	    int updateTaskStatusReopen(@Param("taskId") String taskId);

}
