package com.ayush.ems.entities;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UploadHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer employeeId;      // Optional, for audit or employee-specific uploads
    private String companyId;          // To filter upload history by company
    private String fileName;
    private String uploadedBy;
    private LocalDateTime uploadTime;
    private String status;  // Success / Fail
    private String remarks;
    private Date adddate;
    private String addwho;
    private Date editdate;
    private String editwho;
}
