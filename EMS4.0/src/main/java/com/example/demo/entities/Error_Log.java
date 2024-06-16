package com.example.demo.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Error_Log {
	@Id
	private int sno;
	private String error_description;
	private Date errorDate;
	private String Java_class_Name;
	private String error_message;
	private String method_name;
	private int error_line_number;

}
