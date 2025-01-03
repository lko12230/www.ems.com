package com.ayush.ems.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ayush.ems.entities.LoginDataArchive;

public interface ArchiveLoginDao extends JpaRepository<LoginDataArchive, Integer> {
	@Query(value = "select count(1) from login_data_archive", nativeQuery = true)
	public int getArchiveLoginCount();

	@Query(value = "select u.sno from login_data_archive u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastId();

}
