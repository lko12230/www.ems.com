package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Downtime_Maintaince {
	@Id
	private int sno;
	private String downtime_description;
	private Date server_downtime;
	private String status;
	private boolean server_down_or_not;
}
