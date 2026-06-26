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
@Table(name = "NSqlConfig")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NSqlConfig {
	@Id
	private int sno;
	private int configid;
	private String config_description;
	private String NSqlValue;
	private String Configkey;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
	private String company_id;
	private String configurable;
}
