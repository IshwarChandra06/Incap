package com.eikona.mata.service.impl;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.DailyAttendanceConstants;
import com.eikona.mata.constants.DeviceConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.constants.TransactionConstants;
import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Transaction;
import com.eikona.mata.repository.TransactionRepository;
import com.eikona.mata.service.TransactionService;
import com.eikona.mata.util.CalendarUtil;
import com.eikona.mata.util.GeneralSpecificationUtil;


@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private GeneralSpecificationUtil<Transaction> generalSpecification;

	@Autowired
	private CalendarUtil calendarUtil;
	
	

	@Override
	public PaginationDto<Transaction> searchByField(String sDate, String eDate, String employeeId,
			String employeeName, String department, String device, String uId, String designation, String area,
			String emp, String permissionStatus, String deviceType,int pageno,String sortField, String sortDir) {
		Date startDate = null;
		Date endDate = null;
		if (!sDate.isEmpty() && !eDate.isEmpty()) {
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			try {

				startDate = format.parse(sDate);
				endDate = format.parse(eDate);
				endDate = calendarUtil.getConvertedDate(endDate, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}

		Page<Transaction> page = getTransactionBySpecification(startDate,endDate, employeeId, employeeName, department,device,uId,designation,area,emp, permissionStatus,deviceType, pageno, sortField, sortDir);
		List<Transaction> transactionList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
		PaginationDto<Transaction> dtoList = new PaginationDto<Transaction>(transactionList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir,
				ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Transaction> getTransactionBySpecification(Date startDate, Date endDate, String employeeId, String employeeName, String department, String device, String uId, String designation, String area, String emp,
			String permissionStatus, String deviceType, int pageno, String sortField, String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno -NumberConstants.ONE , NumberConstants.TEN, sort);

		Specification<Transaction> empSpec = null;
		if("Unregistered".equalsIgnoreCase(emp)) {
			empSpec = generalSpecification.isNotNullSpecification("empId");
		}else if("Registered".equalsIgnoreCase(emp)) {
			empSpec = generalSpecification.isNullSpecification("empId");
		}else {
			empSpec = generalSpecification.allSpecification();
		}
		
		Specification<Transaction> dateSpec = generalSpecification.dateSpecification(startDate, endDate,TransactionConstants.PUNCH_DATE);
		Specification<Transaction> empIdSpec = generalSpecification.stringSpecification(employeeId, TransactionConstants.EMP_ID);
		Specification<Transaction> empNameSpec = generalSpecification.stringSpecification(employeeName, ApplicationConstants.NAME);
		Specification<Transaction> deptSpec = generalSpecification.stringSpecification(department, DailyAttendanceConstants.DEPARTMENT);
		Specification<Transaction> designationSpec = generalSpecification.stringSpecification(designation, DailyAttendanceConstants.DESIGNATION);
		Specification<Transaction> areaSpec = generalSpecification.stringSpecification(area, "area");
		Specification<Transaction> devSpec = generalSpecification.foreignKeyStringSpecification(device, TransactionConstants.DEVICE, ApplicationConstants.NAME);
		Specification<Transaction> devTypeSpec = generalSpecification.foreignKeyStringSpecification(deviceType, TransactionConstants.DEVICE, DeviceConstants.MODEL);
		Specification<Transaction> uIdSpec = generalSpecification.stringSpecification(uId, "uniqueId");
		Specification<Transaction> permissionStatusSpec = generalSpecification.stringSpecification(permissionStatus, "permissionStatus");

		Page<Transaction> page = transactionRepository.findAll(dateSpec.and(empIdSpec).and(empNameSpec).and(deptSpec).and(uIdSpec).and(devTypeSpec)
				.and(devSpec).and(empSpec).and(areaSpec).and(designationSpec).and(permissionStatusSpec), pageable);
		return page;
	}



	


	
}
