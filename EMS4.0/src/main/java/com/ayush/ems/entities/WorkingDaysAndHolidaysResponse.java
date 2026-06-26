package com.ayush.ems.entities;

import java.util.List;

public class WorkingDaysAndHolidaysResponse {
	 private int workingDays;
	    private List<Holiday> holidays;  // List of holidays that were excluded from working days

	    public WorkingDaysAndHolidaysResponse(int workingDays, List<Holiday> holidays) {
	        this.workingDays = workingDays;
	        this.holidays = holidays;
	    }

	    public int getWorkingDays() {
	        return workingDays;
	    }

	    public void setWorkingDays(int workingDays) {
	        this.workingDays = workingDays;
	    }

	    public List<Holiday> getHolidays() {
	        return holidays;
	    }

	    public void setHolidays(List<Holiday> holidays) {
	        this.holidays = holidays;
	    }
}
