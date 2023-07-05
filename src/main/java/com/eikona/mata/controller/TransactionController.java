package com.eikona.mata.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Transaction;
import com.eikona.mata.repository.DepartmentRepository;
import com.eikona.mata.service.TransactionService;
import com.eikona.mata.service.impl.CosecServiceImpl;
import com.eikona.mata.util.ExportEventReport;
import com.eikona.mata.util.ImageProcessingUtil;

@Controller
public class TransactionController {
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private CosecServiceImpl cosecServiceImpl;
	
	@Autowired
	private ImageProcessingUtil imageProcessingUtil;
	
	@Autowired
	private ExportEventReport exportEventReport;
	
	@GetMapping("/transaction")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String transactionList() {
		return "transaction/transaction_list";
	}
	
	@RequestMapping(value = "/push-events-to-cosec", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public String pushEventsToMatrix(Model model) {
		model.addAttribute("listDepartment", departmentRepository.findAll());
		return "transaction/pushTransactionToCosec";
	}
	
	@GetMapping("/sync-events-to-cosec")
	@PreAuthorize("hasAuthority('transaction_view')")
	public  @ResponseBody String syncEventsToCosec(String sDate,String eDate,String department)
	{
		String message="";
		try {
			 cosecServiceImpl.syncEventFromMataToCosecDepartmentWise(sDate,eDate,department);
			 message="Sync in to cosec successfully !!";
			 return message;
		} 
		catch (Exception e) {
			e.printStackTrace();
			 message="Sync Failed !!";
			 return message;
		}
	}
	
	//search data
	@RequestMapping(value = "/api/search/transaction", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public @ResponseBody PaginationDto<Transaction> search(String sDate,String eDate, String employeeId,String employeeName,String uId,String department,String designation,String area,
			String device,String emp,String deviceType,int pageno, String sortField, String sortDir) {
		
		PaginationDto<Transaction> dtoList = transactionService.searchByField(sDate, eDate, employeeId, employeeName,department, device,uId,designation,area,emp,"",deviceType, pageno, sortField, sortDir);
		setTransactionImage(dtoList);
		return dtoList;
	}
	private void setTransactionImage(PaginationDto<Transaction> dtoList) {
		List<Transaction> eventsList = dtoList.getData();
		List<Transaction> transactionList = new ArrayList<Transaction>();
		for (Transaction trans : eventsList) {
			byte[] image = imageProcessingUtil.searchTransactionImage(trans);
			trans.setCropImageByte(image);
			transactionList.add(trans);
		}
		dtoList.setData(transactionList);
	}
	
	
	@RequestMapping(value = "/api/event-report/export-to-excel", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_export')")
	public void exportToFile(HttpServletResponse response, String sDate,String eDate, String employeeId,String employeeName,String uId,String department,String designation,String area,
			String device,String emp,String deviceType,String flag) {
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		String currentDateTime = dateFormat.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Event_Report_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		try {
			exportEventReport.fileExportBySearchValue(response,sDate, eDate, employeeId, employeeName,department, device,uId,designation,area,emp,deviceType,flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//Prohibited Device
	@GetMapping("/perimeter-event")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String prohibitedEventList() {
		return "transaction/prohibited_event_list";
	}
	@RequestMapping(value = "/api/search/prohibited-event", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public @ResponseBody PaginationDto<Transaction> searchProhibitedEvent(String sDate,String eDate, String employeeId,String employeeName,String uId,String department,String designation,String area,
			String device,String emp,String deviceType,int pageno, String sortField, String sortDir) {
		
		PaginationDto<Transaction> dtoList = transactionService.searchByField(sDate, eDate, employeeId, employeeName,department, device,uId,designation,area,emp,"Prohibited", deviceType, pageno, sortField, sortDir);
		setTransactionImage(dtoList);
		return dtoList;
	}
	
	
	//Restricted Time 
	@GetMapping("/deadline-event")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String restrictedTimeEventList() {
		return "transaction/deadline_event_list";
	}
	@RequestMapping(value = "/api/search/deadline-event", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public @ResponseBody PaginationDto<Transaction> searchRestrictedTimeEvent(String sDate,String eDate, String employeeId,String employeeName,String uId,String department,String designation,String area,
			String device,String emp,String deviceType,int pageno, String sortField, String sortDir) {
		
		PaginationDto<Transaction> dtoList = transactionService.searchByField(sDate, eDate, employeeId, employeeName,department, device,uId,designation,area,emp,"Deadline", deviceType, pageno, sortField, sortDir);
		setTransactionImage(dtoList);
		return dtoList;
	}
	
}
