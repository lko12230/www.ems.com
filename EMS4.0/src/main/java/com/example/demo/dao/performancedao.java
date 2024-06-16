package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Performance;

public interface performancedao extends JpaRepository<Performance, Integer> {

}
