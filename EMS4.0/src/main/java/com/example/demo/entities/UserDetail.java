package com.example.demo.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int sno;
	@Id
	private int id;
	private String username;
	@Column(unique = true)
	private String email;
	private String password;
	private String repassword;
	private String phone;
//@NotBlank(message="Gender field is required")
	private String gender;
	@NotBlank(message = "Date Of Birth Cannot be Empty")
	private String dob;
	private boolean enabled;
	@NotBlank(message = "Home Address field is required")
	private String address;
	@NotEmpty(message = "Country cannot be empty")
	private String country;
	private String Image_Url;
	private String experience;
	private String skills;
	private Date editdate;
	private String editwho;
	@Transient
	private String Captcha;
	private int alert_message_sent;
	@Transient
	private String hidden;
	private Date SystemDateAndTime;
	@Transient
	private String imageCaptcha;
	private int aaid;
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
	private Date CheckIn;
	private Date CheckOut;
	private boolean user_status;
	private String laptop_id;
	private String laptop_brand;
	private Date laptop_assign_date;
	private String laptop_serial_number;
	private String who_assign_laptop;
	private int who_assign_laptop_employee_id;
//	@ManyToOne
//	@JsonIgnore
//	private User user;
	@OneToOne
	@JoinColumn(name = "User_Id")
	private User user;
	private String Status;
	private String bank_account_holder_name;
	private boolean laptop_assign_or_not;
	private String laptop_status;
	private long bank_account_number;
	private String ifsc_code;
	private String bank_name;
	private String Designation;
	private String base_location;
	private boolean manager_or_not;
	private String resume_file_url;
	private String team_desc;
	private String review_rating;
}
