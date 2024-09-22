package com.example.demo.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class LoginDataArchive {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	private int Sno;
	private int id;
	private String username;
	private String email;
	private Date LoginDateAndTime;
	private String ipAddress;
//private Date SesssionExpiredDateAndTime;
	private boolean is_session_interrupted;
	private Date LogoutDateAndTime;
	private boolean User_status;
	private String Session_Id;
	private String location;

}