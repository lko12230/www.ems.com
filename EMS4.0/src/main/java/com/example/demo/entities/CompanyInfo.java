package com.example.demo.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
public class CompanyInfo {
	@Id
private int sno;
private String Company_id;
private String Company_name;
private String Company_registration_date;
private String company_image_logo_url;
private String company_email;
private String company_address;
private String company_phone;
private String gst_no;
private Date adddate;
private Date editdate;
private String addwho;
private String editwho;
}
