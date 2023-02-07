package com.eikona.mata.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.mata.dto.IncapMonthlyAttendanceDto;
import com.eikona.mata.dto.IncapReportDto;
import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.service.impl.IncapMonthlyAttendanceReportServiceImpl;


@Controller
public class MonthlyAttendanceReportController {
	@Autowired
	private IncapMonthlyAttendanceReportServiceImpl incapMonthlyAttendanceReportServiceImpl;
	
	@GetMapping("/monthly-attendance/report")
	@PreAuthorize("hasAuthority('monthlyattendance_view')")
	public String attendanceReportPage() {
		return "reports/monthlyAttendanceReport";
	}
	
	
	@GetMapping("/api/monthly/attendance/export-to-file")
	@PreAuthorize("hasAuthority('monthlyattendance_export')")
	public void excelGenerateIncapAttendance(HttpServletResponse response, String date,String employeeId,String employeeName,String department,String shift,String flag) {
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		String currentDateTime = dateFormat.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Monthly_Attendance_" + currentDateTime + "."+flag;
		response.setHeader(headerKey, headerValue);
		
		try {
			
			IncapReportDto<IncapMonthlyAttendanceDto> monthlyDataList = incapMonthlyAttendanceReportServiceImpl.calculateMonthlyReport(date,employeeId,employeeName,department,shift);
			incapMonthlyAttendanceReportServiceImpl.excelGenerator(response, monthlyDataList);
			
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/api/search/monthly-attendance-report", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('monthlyattendance_view')")
	public @ResponseBody PaginationDto<IncapReportDto<IncapMonthlyAttendanceDto>> searchMonthlyReport(String date,String employeeId,String employeeName,String department,String shift, int pageno, String sortField, String sortDir) {
		PaginationDto<IncapReportDto<IncapMonthlyAttendanceDto>> dtoList = incapMonthlyAttendanceReportServiceImpl.search(date,employeeId,employeeName,department,shift, pageno, sortField, sortDir);
		
		return dtoList;
	}
}
