package com.ayush.ems.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.web.context.annotation.SessionScope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SessionScope
public class RecordActivity{
	@Id
	private int sno;
	private int employee_id;
	private String Employee_name;
	private java.util.Date Date;
	private String ipAddress;
	private String functionality;
}
