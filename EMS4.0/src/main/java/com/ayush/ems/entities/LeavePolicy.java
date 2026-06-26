package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LeavePolicy {

    @Id
    private int id;
    private int employee_id;
    private int year;
    private int leave_quota;
    private int wfh_quota;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
}