package com.eikona.mata.dto;

import java.util.List;

public class IncapMonthlyAttendanceDto {
	
	private String empId;
	private String empName;
	private String employeeType;
	private String department;
	private String shift;
	private String totalPresentCount;
	private String totalAbsentCount;
	private String totalOverTime;

	private List<String> dateList;

	
	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEmpId() {
		return empId;
	}

	public String getEmpName() {
		return empName;
	}

	public String getEmployeeType() {
		return employeeType;
	}

	public void setEmployeeType(String employeeType) {
		this.employeeType = employeeType;
	}

	public String getTotalPresentCount() {
		return totalPresentCount;
	}

	public void setTotalPresentCount(String totalPresentCount) {
		this.totalPresentCount = totalPresentCount;
	}

	public String getTotalAbsentCount() {
		return totalAbsentCount;
	}

	public void setTotalAbsentCount(String totalAbsentCount) {
		this.totalAbsentCount = totalAbsentCount;
	}

	public String getTotalOverTime() {
		return totalOverTime;
	}

	public void setTotalOverTime(String totalOverTime) {
		this.totalOverTime = totalOverTime;
	}

	public List<String> getDateList() {
		return dateList;
	}

	public void setDateList(List<String> dateList) {
		this.dateList = dateList;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public String getShift() {
		return shift;
	}

	public void setShift(String shift) {
		this.shift = shift;
	}
	
}
