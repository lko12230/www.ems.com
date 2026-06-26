package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Error_Log {
	@Id
	private long sno;
	@Column(columnDefinition = "TEXT") 
	private String error_description;
	private Date errorDate; 
	private String Java_class_Name;
	@Column(columnDefinition = "TEXT") 
	private String error_message;
	private String method_name;
	private int error_line_number;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
	@Column(columnDefinition = "TEXT") 
	private String full_stack_trace;


}
