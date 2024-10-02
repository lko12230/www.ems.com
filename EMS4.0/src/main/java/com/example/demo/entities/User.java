package com.example.demo.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
	private int sno;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
//	private int a_id;
//	@NotBlank(message="username field is required")
//	@Size(min=2,max=20,message="min 2 and max 20 characters are allowed")
	@NotEmpty(message = "Username cannot be empty")
	@Size(min = 2, max = 20, message = "min 2 and max 20 characters are allowed")
	private String username;
	private String state;
	@Column(unique = true)
	@NotEmpty(message = "Email cannot be empty")
	@Email(message = "Email is not valid")
	private String email;
	@NotEmpty(message = "Password cannot be empty")
	@Size(min = 4, message = "Password size min 4 characters")
	private String password;
	@NotEmpty(message = "Repassword cannot be empty")
	@Size(min = 4, message = "Repassword size min 4 characters")
	private String repassword;
	@Column(unique = true)
	@NotEmpty(message = "Phone Number cannot be empty")
	@Size(min = 10, max = 10, message = "Phone Number should only 10 numbers")
	private String phone;
	private boolean user_status;
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
	private Date SperationDate;
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
//	@NotEmpty(message = "Team cannot be empty")
//	@Size(min = 8,max=8, message = "Team ID size must be in 8 characters")
//	private String team;
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
	private boolean defaultPasswordSent;
	@ManyToOne
	@JsonIgnore
	private Admin admin;
//	@OneToMany(cascade = CascadeType.ALL,mappedBy = "user",orphanRemoval = true )
//	private List<UserDetail> AllUsersList=new ArrayList<>();
	@OneToOne(cascade = CascadeType.ALL,mappedBy = "user",orphanRemoval = true)
	private UserDetail userDetail;
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
	private String resume_file_url;
	private String Designation;
	private String base_location;
	private boolean manager_or_not;
	private String team;
	private String company;
	private String company_id;
}
