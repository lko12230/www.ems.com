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
@Table(name = "JOB")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Job {
	@Id
	private int sno;
	private int id;
	private String job_description;
	private String job_running_time;
	private String job_Status;
	private String job_active_or_not;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
}
