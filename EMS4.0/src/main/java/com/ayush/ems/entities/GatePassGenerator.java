package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gate_pass")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GatePassGenerator {
	@Id
	@Column(unique = true, nullable = false, length = 20)
	private String gatePassId; // Manually set auto-incrementing ID

	@Column(nullable = false)
	private Integer userId; // User ID
	private String laptopBrand;
	private String laptopId;
	private String laptopSerialNumber;
	private String laptop_operational_status;
	private String laptop_assignment_status;
	private Date issueDate;
	private String actionType; // "ASSIGNED" or "REMOVED"
	private String issuedBy; // IT Admin Name
	private String status; // "ACTIVE" or "EXPIRED"
	private String employee_email;
	private Date adddate;
	private Date editdate;
	private String addwho;
	private String editwho;
}
