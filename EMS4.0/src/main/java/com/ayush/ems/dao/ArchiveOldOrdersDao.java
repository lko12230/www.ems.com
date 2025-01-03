package com.ayush.ems.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ayush.ems.entities.ArchiveOldOrders;

public interface ArchiveOldOrdersDao extends JpaRepository<ArchiveOldOrders, Integer> {
	@Query(value = "select count(1) from archive_old_orders", nativeQuery = true)
	public int getArchiveOrdersCount();

	@Query(value = "select u.sno from archive_old_orders u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastId();
}
