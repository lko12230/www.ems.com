package com.ayush.ems.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayush.ems.entities.Performance;

public interface PerformanceDao extends JpaRepository<Performance, Integer> {

}
