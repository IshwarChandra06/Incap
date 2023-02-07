package com.eikona.mata.dto;

import java.util.Date;

public class TransactionDto {
	
	private String empId;
	private Date checkInTime;
	private Date checkOutTime;
	
	public String getEmpId() {
		return empId;
	}
	public void setEmpId(String empId) {
		this.empId = empId;
	}
	public Date getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}
	public Date getCheckOutTime() {
		return checkOutTime;
	}
	public void setCheckOutTime(Date checkOutTime) {
		this.checkOutTime = checkOutTime;
	}
	public TransactionDto(String empId, Date checkInTime, Date checkOutTime) {
		super();
		this.empId = empId;
		this.checkInTime = checkInTime;
		this.checkOutTime = checkOutTime;
	}
}
