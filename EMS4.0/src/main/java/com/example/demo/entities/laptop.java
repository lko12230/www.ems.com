package com.example.demo.entities;

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
public class laptop {
	@Id
	private int sno;
	private String serial_number;
	private String Product_ID;
	private String Model;
	private String laptop_colour;
	private String laptop_brand;
	private String device_id;
}
