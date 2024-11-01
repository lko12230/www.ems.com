package com.ayush.ems.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.ayush.ems.entities.UserLoginDateTime;

public interface UserLoginDao extends JpaRepository<UserLoginDateTime, Integer> {

    // Fetch the latest session ID for a user whose session has not been logged out
    @Query(value = "SELECT u.id FROM employee_login_record u WHERE u.logout_date_and_time IS NULL AND u.email = ?1 ORDER BY u.login_date_and_time DESC LIMIT 1", nativeQuery = true)
    Optional<Integer> findLatestSessionRecordId(String email);

    // Update session interruption status for a specific session ID
    @Modifying
    @Transactional
    @Query(value = "UPDATE employee_login_record u SET u.is_session_interrupted = '1', u.user_status='0' WHERE u.id = ?1 ORDER BY u.login_date_and_time DESC LIMIT 1", nativeQuery = true)
    void updateSessionInterruptedStatus(Integer id);

    // Set a default logout time for expired sessions
    @Modifying
    @Transactional
    @Query(value = "UPDATE employee_login_record u SET u.logout_date_and_time = CURRENT_TIMESTAMP  WHERE u.id = ?1 AND u.logout_date_and_time IS NULL ORDER BY u.login_date_and_time DESC LIMIT 1", nativeQuery = true)
    void setDefaultLogoutTime(Integer id);

 // Check if a specific session has been manually logged out by getting the latest matching record for a session ID
    @Query(value = "SELECT CASE WHEN u.logout_date_and_time IS NOT NULL THEN true ELSE false END FROM employee_login_record u WHERE u.id = ?1 ORDER BY u.login_date_and_time DESC LIMIT 1", nativeQuery = true)
    Integer isSessionManuallyLoggedOut(Integer id);


    // Set the logout time for a specific session ID (for manual logouts)
    @Modifying
    @Transactional
    @Query(value = "UPDATE employee_login_record u SET u.logout_date_and_time = CURRENT_TIMESTAMP, u.user_status='0' WHERE u.id = ?1 AND u.logout_date_and_time IS NULL", nativeQuery = true)
    void markSessionAsLoggedOut(Integer id);
    
	@Query(value = "select u.sno from employee_login_record u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastId();

	@Query(value = "update employee_login_record u set u.user_status='0'", nativeQuery = true)
	@Modifying
	public void updateUserStatusReset();
	
	@Query(value="select count(1) from database_ems.employee_login_record",nativeQuery = true)
	public int getLoginCount();
}
