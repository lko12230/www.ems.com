package com.example.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.Downtime_Maintaince;

public interface DowntimeMaintaince_Dao extends JpaRepository<Downtime_Maintaince, Integer>{
	@Query("select u.status from Downtime_Maintaince u where u.downtime_description=?1")
	public String server_status_check(String downtime_description);

	@Query("select u.server_down_or_not from Downtime_Maintaince u where u.downtime_description=?1")
	public boolean server_status_check_active_or_not(String downtime_description);
	
	@Query(value = "update Downtime_Maintaince u set u.server_down_or_not='0' where  u.downtime_description=?1", nativeQuery = true)
	@Modifying
	public void update_server_status_down(String downtime_description);
	
	@Query(value = "update Downtime_Maintaince u set u.server_down_or_not='1' where  u.downtime_description=?1", nativeQuery = true)
	@Modifying
	public void update_server_status_up(String downtime_description);
	
	@Query("select u from Downtime_Maintaince u where u.downtime_description = ?1")
	public Optional<Downtime_Maintaince> findByUserName(String description);
	
	@Query(value = "select count(1) from Downtime_Maintaince",nativeQuery = true)
	public int getDowntimeMaintainceCount();
}
