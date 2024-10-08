package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Performance;

public interface PerformanceDao extends JpaRepository<Performance, Integer> {

}
