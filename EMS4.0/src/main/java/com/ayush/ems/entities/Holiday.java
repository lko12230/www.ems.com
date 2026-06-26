package com.ayush.ems.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@ToString
public class Holiday {

    @Id
    private long sno; 
    private String name; // e.g. "Diwali", "Independence Day"
    private LocalDate date;
    private String type; // "Public Holiday", "Restricted Holiday"
    private String applicableTo; // Optional: "All", "HR Only", etc.
    private String company_name;
    private String company_id;
    private String location; 
    private Date adddate;
    private String addwho;
    private Date editdate;
    private String editwho;

    // Getters and Setters
}
