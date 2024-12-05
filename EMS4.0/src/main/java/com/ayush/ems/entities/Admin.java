package com.ayush.ems.entities;

import java.io.Serializable;
//import java.util.ArrayList;
import java.util.Date;
//import java.util.List;
//
//import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
//import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name="ADMIN")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Admin implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int sno;
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
private int aid;
@Column(unique = true)
@NotEmpty(message="Email cannot be empty")
@Email(message="Email is not valid")
private String email;
@NotEmpty(message="Password cannot be empty")
@Size(min=4,message="Password size min 4 characters")
private String password;
//@OneToMany(cascade = CascadeType.ALL,mappedBy = "admin",orphanRemoval = true )
//private List<User> userList=new ArrayList<>();
@Transient
private String Captcha;
//private String addwho;
private String Role;
@Transient
private String hidden;
private Date SystemDateAndTime;
@Transient
private String imageCaptcha;
private Date adddate;
private String addwho;
private Date editdate;
private String editwho;
private String company_id;
private boolean allowMultipleCompany;
}
