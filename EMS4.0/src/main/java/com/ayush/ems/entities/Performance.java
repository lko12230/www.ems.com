package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PERFORMANCE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Performance {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	private int sno;
	private int id;
	private double january;
	private double february;
	private double march;
	private double april;
	private double may;
	private double june;
	private double july;
	private double august;
	private double september;
	private double october;
	private double november;
	private double december;
	private int year;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;

}
