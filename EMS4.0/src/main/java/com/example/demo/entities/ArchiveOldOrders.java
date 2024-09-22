package com.example.demo.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class ArchiveOldOrders {
	@Id
	private int sno;
	private String orderId;
	private String receipt;
	private String status;
	private String paymentId;
	private String email;
	private String phone;
	private String company;
	private String company_id;
	private Date subscription_start_date;
	private Date subscription_expiry_date;
	private Date system_date_and_time;
	private String license_number;
	private float discount;
	private String tax;
	private String license_status;
	private float amount_without_gst;
	private float amount;
	private float gst_amount;
	private String gst_no;
	private boolean invoice_sent_or_not;
}
