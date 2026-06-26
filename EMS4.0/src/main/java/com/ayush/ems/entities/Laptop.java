package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "LAPTOP")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Laptop {
	@Id
	private int sno;
	private String serial_number;
	private String Product_ID;
	private String laptop_model;
	private String laptop_color;
	private String laptop_brand;
	private String laptop_operational_status;
	private String laptop_assignment_status;
	private String device_id;
	private String company_id;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
}
