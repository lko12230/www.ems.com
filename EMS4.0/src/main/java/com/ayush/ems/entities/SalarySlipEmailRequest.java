package com.ayush.ems.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class SalarySlipEmailRequest {
	private String salarySlip;
    private String message;
    private String subject;
    private String to;
}
