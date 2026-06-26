package com.ayush.ems.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SalarySlip {
// 	@GeneratedValue(strategy = GenerationType.AUTO)
//    private String sno; // Serial Number (Optional, can be auto-generated if needed)   
    private String employeeName;  // Employee's Name
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Serialkey;
    private Integer employeeId;  // Employee ID
    private String department;  // Employee's Department
    private String designation;  // Employee's Designation
    private BigDecimal basicSalary;  // Basic Salary (Use BigDecimal for precision)
    private BigDecimal bonus;  // Bonus (Use BigDecimal for precision)
    private BigDecimal deductions;  // Deductions (Use BigDecimal for precision)
    private BigDecimal grossSalary;  // Gross Salary (Use BigDecimal for precision)
    private BigDecimal netSalary;  // Net Salary (Use BigDecimal for precision)
	// --- Arrear fields ---
    @Column(precision = 10, scale = 2)
    private BigDecimal basicArrear;

    @Column(precision = 10, scale = 2)
    private BigDecimal hraArrear;

    @Column(precision = 10, scale = 2)
    private BigDecimal bonusArrear;

    @Column(precision = 10, scale = 2)
    private BigDecimal ltaArrear;

    @Column(precision = 10, scale = 2)
    private BigDecimal retentionArrear;

    @Column(precision = 10, scale = 2)
    private BigDecimal specialArrear;

    @Column(precision = 10, scale = 2)
    private BigDecimal transportArrear;

	private BigDecimal arrear;
    private Date paymentDate;  // Payment Date
    private String fileViewUrl;  // URL to view the salary slip PDF (Optional)
    private String fileDownloadUrl;  // URL to download the salary slip PDF (Optional)
    private String salarySlipMonth; // Month of the salary slip (e.g., "January")
    private String salarySlipYear;  // Year of the salary slip (e.g., 2025)
    private String salarySlipDate;
    private boolean isEmailSent; 
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
}
