package com.ayush.ems.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Attendance {

    @Id
    private long sno; 

    private String request_id;
    private Integer employee_id;
    private LocalDate date;

    private String status; // Present, Absent, Leave, WFH

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    private String company_id;
    private String reason;
    private boolean leave_request;
    private LocalTime updatedCheckInTime;
    private LocalTime updatedcheckOutTime;
    private boolean attendance_request;
    private boolean wfh_request;
    private String team_id;
    private String employee_name;
    private String email;
    private int manager_id;
    private String approved_or_reject_or_pending;

    private Date adddate;
    private String addwho;
    private Date editdate;
    private String editwho;

    // ✅ Newly added fields
    private int totalBreak;    // Duration of break (e.g., 00:30 for 30 mins)
    private String hoursWorked;   // Total worked hours
    private String shift;            // e.g., "09:30 - 18:30"
    // 🆕 Add these fields:
    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalDays;
    private boolean withdrawnRequest;
    private String fileId;
    private String requestType;
    private String file_name;
    // Mark this field as transient to avoid persistence in DB
    @Transient
    private String fileDownloadUrl;  // This field will not be persisted in the database
    @Transient
    private String fileViewUrl;
    @Column(name = "num_of_requests")
    private long numOfRequests; // This will store the count of requests with the same request_id
    // Update to LocalTime if you are storing only time (HH:mm)
//    private LocalTime addTime;  // For storing HH:mm time
    // Update to LocalTime if you are storing only time (HH:mm)
//    private LocalTime editTime;  // For storing HH:mm time
}
