package com.ayush.ems.entities;

import java.io.Serializable;
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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
//import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "STAGE_EMPLOYEE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class stage_user implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int sno;
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
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
	private Date editdate;
	private String editwho;
	private String Status;
	@Transient
	private String Captcha;
	private int alert_message_sent;
	@Transient
	private String hidden;
	private Date adddate;
	@Transient
	private String imageCaptcha;
	private int addwho;
	private String role;
	private String ipAddress;
	@Column(name = "account_non_locked")
	private boolean AccountNonLocked;
	private boolean defaultPasswordSent;
	private int process_flag;
	private String Designation;
	private String base_location;
	private boolean manager_or_not;
	private String company;
	private String company_id;
	private String error_message;
}
