package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.ArchiveDisabledUserDetail;

public interface ArchiveDisabledUserDetailDao extends JpaRepository<ArchiveDisabledUserDetail, Integer> {
	@Query(value = "select count(1) from database_ems.archive_disabled_user_detail", nativeQuery = true)
	public int getArchiveUserCount();

	@Query(value = "select u.sno from database_ems.archive_disabled_user_detail u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastSno();
}
