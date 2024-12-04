package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SubscriptionPlans {
	@Id
	private int sno;
	private String plan_description;
	private float amount;
	private float gst;
	private float discount;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
}
