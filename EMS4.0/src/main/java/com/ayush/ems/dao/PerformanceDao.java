package com.ayush.ems.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Performance;
@Repository
public interface PerformanceDao extends JpaRepository<Performance, Integer> {

	@Query("SELECT u FROM Performance u WHERE u.id = :id")
	Optional<Performance> findByIdField(@Param("id") Integer id);
}
