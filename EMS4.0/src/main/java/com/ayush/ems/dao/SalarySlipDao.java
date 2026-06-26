package com.ayush.ems.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayush.ems.entities.SalarySlip;

public interface SalarySlipDao extends JpaRepository<SalarySlip, Integer>{

}
