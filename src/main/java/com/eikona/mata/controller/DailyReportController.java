package com.eikona.mata.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.DailyReport;
import com.eikona.mata.service.DailyAttendanceService;
import com.eikona.mata.util.ExportDailyReports;

@Controller
public class DailyReportController {

	@Autowired
	private DailyAttendanceService dailyAttendanceService;

	
	@Autowired
	private ExportDailyReports exportDailyReports;
	
	@Value("${dailyreport.autogenerate.enabled}")
	private String enableGenerate;
	
	@GetMapping("/daily-reports")
	@PreAuthorize("hasAuthority('dailyreport_view')")
	public String viewHomePage(Model model) {
		model.addAttribute("enableGenerate", enableGenerate);
		return "reports/daily_report";
	}

	@RequestMapping(value = "/generate/daily-reports", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('dailyreport_view')")
	public String generateDailyReportsPage(Model model) {
		return "reports/generate_daily_report";
	}
	
	@RequestMapping(value = "/api/search/daily-reports", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('dailyreport_view')")
	public @ResponseBody PaginationDto<DailyReport> search(Long id, String sDate,String eDate, String employeeId, String employeeName, String department,
			String status,String shift,int pageno, String sortField, String sortDir) {
		
		PaginationDto<DailyReport> dtoList = dailyAttendanceService.searchByField(id, sDate, eDate, employeeId, employeeName, department, status,shift, pageno, sortField, sortDir);
		
		return dtoList;
	}
	
	@RequestMapping(value = "/get/data-by-organization", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('dailyreport_view')")
	public @ResponseBody List<DailyReport> generateDailyReports(String sDate, String eDate) {
		
		return dailyAttendanceService.generateDailyAttendance(sDate, eDate);
		
	}
	
	@RequestMapping(value="/api/daily-attendance/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('dailyreport_export')")
	public void exportToFile(HttpServletResponse response, Long id, String sDate, String eDate, String employeeName,String employeeId, 
			 String department,String status,String shift, String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Daily_Report" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportDailyReports.fileExportBySearchValue(response, id, sDate, eDate, employeeName,employeeId,department,status,shift, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}
