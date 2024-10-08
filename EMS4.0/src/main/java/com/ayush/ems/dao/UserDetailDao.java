package com.ayush.ems.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ayush.ems.entities.UserDetail;

public interface UserDetailDao extends JpaRepository<UserDetail, Integer> {
	@Query("select u from UserDetail u where u.team = :team and length(u.team) > 1 and u.enabled = 1")
	public List<UserDetail> getUserByTeam(String team);

	@Query(value = "update employeedetail u set u.user_status='0' where u.email= ?1", nativeQuery = true)
	@Modifying
	public void update_user_status(String username);
	
	 @Query("SELECT u FROM UserDetail u WHERE u.username = :input OR u.email = :input OR u.id = :input")
	  public List<UserDetail> findByNameContainingOrEmailContainingOrIdContaining(@Param("input") String input);
	 
	 @Query("select u from UserDetail u where u.enabled=1")
	 public List<UserDetail> findAllEnabledUser();
	 
		@Query(value = "select * from database_ems.employeedetail u where u.enabled='0' and u.last_working_day <= (NOW() - INTERVAL 30 DAY)",nativeQuery = true )
		public List<UserDetail>  Get_ALL_Disabled_Old_UserDetail_Job();
		
		@Query(value = "select count(1) from database_ems.employeedetail", nativeQuery = true)
		public int getUserDetailCount();

		@Query(value = "select u.sno from database_ems.employeedetail u order by u.sno desc limit 1", nativeQuery = true)
		public int getLastSno();
}
