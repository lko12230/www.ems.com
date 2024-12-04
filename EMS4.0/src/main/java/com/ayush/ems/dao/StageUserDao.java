package com.ayush.ems.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ayush.ems.entities.stage_user;

public interface StageUserDao extends JpaRepository<stage_user, Integer>{
	@Query("select u from stage_user u where u.email = :email")
	public Optional<stage_user> findByUserName(@Param("email") String email);

	@Query(value = "select max(sno) from stage_employee u",nativeQuery = true)
	public int maxSno();
	
	@Query(value = "select count(1) from stage_employee u",nativeQuery = true)
	public long count();

	@Query("select u from stage_user u where u.email = :email or u.phone = :phone")
	public Optional<stage_user> findByUserNameAndPhone(@Param("email") String email, @Param("phone") String phone);
	

}
