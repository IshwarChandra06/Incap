package com.eikona.mata.service;

import java.util.List;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.DailyReport;


public interface DailyAttendanceService {
    
	List<DailyReport> generateDailyAttendance(String sDate, String eDate);

	PaginationDto<DailyReport> searchByField(String sDate, String eDate, String employeeId, String employeeName,
			 String department, String status, int pageno, String sortField, String sortDir);

	List<DailyReport> getAllDailyReport();

}

