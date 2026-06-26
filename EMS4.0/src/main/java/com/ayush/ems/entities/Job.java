package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

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
	private String company_id;
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private Date startDate;
	private String jobFrequency; // values: MINUTE, HOURLY, DAILY, etc.
	private Date lastRun;
	private Date nextRun;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
	private long lastRunDurationInMs;  // Duration in milliseconds for last run
	private long totalRunCount;        // Total number of times this job has run
    private boolean startflag; // Optional: used to track if job started
	private boolean endflag;   // Optional: used to track if job ended

}
