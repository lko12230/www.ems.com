package com.ayush.ems.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ayush.ems.entities.RecordActivity;

public interface RecordActivityDao extends JpaRepository<RecordActivity, Integer> {
	@Query(value = "select u.sno from record_activity u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastId();

	@Query(value = "select count(1) from record_activity u order by u.sno desc limit 1", nativeQuery = true)
	public int getCount();
}
