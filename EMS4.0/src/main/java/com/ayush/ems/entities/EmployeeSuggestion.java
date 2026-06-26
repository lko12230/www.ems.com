package com.ayush.ems.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeSuggestion {
	private int id;
    private String name;
    private String email;
}
