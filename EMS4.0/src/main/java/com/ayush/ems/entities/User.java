package com.ayush.ems.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

//import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
import javax.persistence.Id;
//import javax.persistence.ManyToOne;
//import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
//import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "EMPLOYEE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	private int sno;
	private Integer id;
	private String username;
	private String state;
	private String email;
	private String password;
	private String repassword;
	private String phone;
	private boolean user_status;
	private String gender;
	private String dob;
	private boolean enabled;
	private String address;
	private String country;
	private String Image_Name;
	@Transient
	private String Image_File_Url;
	private String Image_file_id;
	private String Resume_file_id;
	private String experience;
	private String skills;
	private Date SeperationDate;
	private Date lastWorkingDay;
	private Date editdate;
	private String editwho;
	private boolean NewUserActiveOrInactive;
	private String Status;
	@Transient
	private String Captcha;
	private Date last_failed_attempt;
	private int alert_message_sent;
	@Transient
	private String hidden;
	private Date SystemDateAndTime;
	@Transient
	private String imageCaptcha;
	private String role;
	private String ipAddress;
	@Column(name = "account_non_locked")
	private boolean AccountNonLocked;
	@Column(name = "failed_attempts")
	private int failedAttempt;
	private Date lockDateAndTime;
	@Column(name = "Expire_lock_date_and_time")
	private Date expirelockDateAndTime;
	private boolean defaultPasswordSent;
	private String Session_Id;
	private boolean Excel_Download;
	private Date Excel_Download_Date;
	private int download_count;
	private String laptop_id;
	private String laptop_brand;
	private Date laptop_assign_date;
	private String laptop_serial_number;
	private String bank_account_holder_name;
	private long bank_account_number;
	private String ifsc_code;
	private String bank_name;
	private String resume_file_name;
	@Transient
	private String resume_file_url;
	private String Designation;
	private String base_location;
	private boolean manager_or_not;
	@Transient
	private String otp;

	@Transient
	private LocalDateTime otpGeneratedAt;

	private boolean twoStepEnabled;
	private Date twoStepEnabledEditDate;

	private String team;
	private String team_name;
	private String team_id;
	private String company;
	private String company_id;
	private boolean seperation_manager_approved;
	private boolean resignationRequestApplied;
	private String addwho;
	private  Date adddate;
	private String edithwo;
	private String admin_id;
	private String linkdin_url;
	private String github_url;
	private String instagram_url;
	private String facebook_url;
	private String X_url;
}
