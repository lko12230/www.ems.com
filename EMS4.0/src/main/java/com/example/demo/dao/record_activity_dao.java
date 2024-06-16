package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.RecordActivity;

public interface record_activity_dao extends JpaRepository<RecordActivity, Integer> {
	@Query(value = "select u.sno from record_activity u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastId();

	@Query(value = "select count(1) from record_activity u order by u.sno desc limit 1", nativeQuery = true)
	public int getCount();
}
