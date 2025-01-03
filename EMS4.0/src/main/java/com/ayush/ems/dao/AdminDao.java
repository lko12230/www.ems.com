package com.ayush.ems.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ayush.ems.entities.Admin;

public interface AdminDao extends JpaRepository<Admin, Integer> {
	@Query("select u from Admin u where u.email = :email")
	public Optional<Admin> findByUserName(@Param("email") String email);
	
	@Query("select u from Admin u where u.aid = :id")
	public Optional<Admin> findByAdminId(int id);

	@Query(value = "select u.aid from admin u order by u.aid desc limit 1", nativeQuery = true)
	public int getLastId();

}
