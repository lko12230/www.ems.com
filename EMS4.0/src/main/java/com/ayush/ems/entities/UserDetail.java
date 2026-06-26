package com.ayush.ems.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "EMPLOYEEDETAIL")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int sno;
	private Integer id;
	private String username;

	@Column(unique = true)
	private String email;

	private String password;
	private String repassword;
	private String phone;
	private String gender;
	private String dob;
	private boolean enabled;
	private String address;
	private String country;
	private String Image_Name;

	@Transient
	private String Image_File_Url;

	private String experience;
	private String skills;
	private String team_name;
	private Date editdate;
	private String editwho;

	@Transient
	private String Captcha;

	private int alert_message_sent;
	private String role;
	private String ipAddress;

	@Column(name = "account_non_locked")
	private boolean AccountNonLocked;

	@Column(name = "failed_attempts")
	private int failedAttempt;

	@Column(name = "lock_date_and_time")
	private Date lockDateAndTime;

	@Column(name = "Expire_lock_date_and_time")
	private Date expirelockDateAndTime;

	private int defaultPasswordSent;
	private int admin;
	private Date lastWorkingDay;
	private String team;
	private String department;
	private String project;
	private boolean EmployeeOnBench;
	private boolean user_status;
	private String laptop_id;
	private String laptop_brand;
	private Date laptop_assign_date;
	private String laptop_serial_number;
	private String device_id;
	private String who_assign_laptop;
	private int who_assign_laptop_employee_id;
	private String Status;
	private String bank_account_holder_name;
	private boolean laptop_assign_or_not;
	private String laptop_status;
	private String laptop_operational_status;
	private String laptop_assignment_status;
	private long bank_account_number;
	private String ifsc_code;
	private String bank_name;
	private String Designation;
	private String base_location;
	private boolean manager_or_not;
	private String resume_file_name;

	@Transient
	private String resume_file_url;
	private String Image_file_id;
	private String Resume_file_id;
	private boolean Has_drive_access;

	private String team_desc;
	private String team_id;
	private String review_rating;
	private boolean seperation_manager_approved;
	private boolean resignationRequestApplied;
	private String addwho;
	private Date adddate;
	private String admin_id;
	private String company_id;
	private Date SeperationDate;
	private String laptop_model;
	private String laptop_color;
	private String gate_pass;
	private String linkdin_url;
	private String github_url;
	private String instagram_url;
	private String facebook_url;
	private String X_url;

	@Transient
	private String taskDescription;

	@Transient
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date taskAssignedDate;

	@Transient
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date taskEndDate;

	private Integer managerId;
	private long default_leave_quota;
	private long default_wfh_quota;

	// Salary-related fields
	private BigDecimal hra;
	private BigDecimal basicSalary;    // Basic Salary from Salary Slip
	private BigDecimal bonus;          // Bonus from Salary Slip
	private BigDecimal deductions;     // Deductions from Salary Slip
	private BigDecimal grossSalary;    // Gross Salary from Salary Slip
	private BigDecimal netSalary;      // Net Salary from Salary Slip
	private LocalDate paymentDate;     // Payment Date from Salary Slip

	private Date DateOfJoining;
	private String UanNumber;
	private String PanNumber;
	private String PfNumber;
	private String Division;

	// Changed from String to Integer for easier numeric handling
	private Integer payableDays;

	private String Grade;

	// Transient fields for payslip generation (not stored in DB)
	@Transient
	private String salarySlipMonth;

	@Transient
	private String salarySlipYear;
	
	// Additional salary components used in PDF generation
	private BigDecimal statutoryBonus;
	private BigDecimal leaveTravelAllowance;
	private BigDecimal retentionBonus;
	private BigDecimal specialAllowance;
	private BigDecimal transportAllowance;
	private BigDecimal arrear;
	private BigDecimal pfEmployee;   // Employee PF contribution
	private BigDecimal tds;          // Tax Deducted at Source
	
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


}
