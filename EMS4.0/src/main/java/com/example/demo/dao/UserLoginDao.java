package com.example.demo.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.entities.UserLoginDateTime;

public interface UserLoginDao extends JpaRepository<UserLoginDateTime, Integer> {

//	@Query(value = "delete from employee_login_record   where login_date_and_time <= (NOW() - INTERVAL 30 DAY)", nativeQuery = true)
//	@Modifying
//	public void deleteOldLoginRecord(Date system_lock_date_time);

	@Query(value = "update employee_login_record u set u.logout_date_and_time = CURRENT_TIMESTAMP,u.user_status='0' where u.email = ?1 ORDER BY u.login_date_and_time DESC LIMIT 1 ", nativeQuery = true)
	@Modifying
	public void getUpdateUserLogoutTimeUserByUserName(String email);

//	@Query(value = "update employee_login_record u set u.session_expired_date_and_time = CURRENT_TIMESTAMP,u.is_session_interrupted='0' where u.email = ?1 ORDER BY u.login_date_and_time DESC LIMIT 1 ",nativeQuery = true)
//	@Modifying
//	public void getUserByUserNameInterrupted(String email);
//	

	@Query(value = "update employee_login_record u set u.is_session_interrupted='1' where u.logout_date_and_time is null and u.email =?1 order by u.login_date_and_time desc limit 1", nativeQuery = true)
	@Modifying
	public void getUserSessionInterrupted(String username);
	
	@Query(value = "update employee_login_record u set u.user_status='0' where u.logout_date_and_time is null and u.email =?1 and u.is_session_interrupted='1' order by u.login_date_and_time desc limit 1", nativeQuery = true)
	@Modifying
	public void getUserStatusSessionExpired(String username);

	@Query(value = "select login_date_and_time,is_session_expired from employee_login_record u where u.email = :email order by u.login_date_and_time desc limit 1", nativeQuery = true)
	public Optional<UserLoginDateTime> findByUserName(@Param("email") String email);

	@Query(value = "update employee_login_record u set user_status='0' where u.logout_date_and_time is null and u.is_session_interrupted='1'", nativeQuery = true)
	@Modifying
	public void Update_Inactive_user_Status();

	@Query(value = "update employee_login_record u set u.user_status='0' where  u.is_session_interrupted='1' ", nativeQuery = true)
	@Modifying
	public void updateuserstatus();

	@Query(value = "select u.sno from employee_login_record u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastId();

	@Query(value = "update employee_login_record u set u.user_status='0'", nativeQuery = true)
	@Modifying
	public void updateuserstatusreset();
	
	@Query(value="select count(1) from database_ems.employee_login_record",nativeQuery = true)
	public int getLoginCount();
}
