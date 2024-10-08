package com.ayush.ems.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ayush.ems.entities.ArchiveDisabledUser;

public interface ArchiveDisabledUserDao extends JpaRepository<ArchiveDisabledUser, Integer> {
	@Query(value = "select count(1) from database_ems.archive_disabled_user", nativeQuery = true)
	public int getArchiveUserCount();

	@Query(value = "select u.sno from database_ems.archive_disabled_user u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastSno();
}
