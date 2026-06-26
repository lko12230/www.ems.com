package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tasks {
	@Id
	private String taskId;
	private Integer id;
	private String taskDescription;
	private boolean taskInProgress;
	private boolean taskPending;
	private boolean taskOverdue;
	private boolean taskCompleted;
	private boolean taskDeleted;
	private String whoAssignedTask;
	private boolean taskClosed;
	private boolean taskReopen;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date taskAssignedDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date taskEndDate;
	private Date adddate;
	private Date editdate;
	private String addwho;
	private String editwho;
	private Date taskCompletedDate;
	private String taskStatus;
	private String email;
	private String whoAssignedTaskEmail;
}
