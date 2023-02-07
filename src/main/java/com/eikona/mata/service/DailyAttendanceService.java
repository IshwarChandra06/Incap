package com.eikona.mata.service;

import java.util.List;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.DailyReport;


public interface DailyAttendanceService {
    
	List<DailyReport> generateDailyAttendance(String sDate, String eDate);

	PaginationDto<DailyReport> searchByField(Long id, String sDate, String eDate, String employeeId, String employeeName,
			 String department, String status, String shift, int pageno, String sortField, String sortDir);

	List<DailyReport> getAllDailyReport();

}

