package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.Error_Log;

public interface error_log_dao extends JpaRepository<Error_Log, Integer> {
	@Query(value = "delete from error_log where error_date <= (NOW() - INTERVAL 7 DAY)", nativeQuery = true)
	@Modifying
	public void deleteOldErrorLog();

	@Query(value = "select u.sno from error_log u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastId();
	
	@Query(value = "select count(1) from error_log", nativeQuery = true)
	public int getCount();
}
