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
	public PaginationDto<Transaction> searchByField(String employee, Long id, String sDate, String eDate, String employeeId, 
			String employeeName, String department,String device,String uId,int pageno,String sortField, String sortDir) {
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

		Page<Transaction> page = getTransactionBySpecification(employee, id, employeeId,  employeeName,department, device,uId,  pageno, sortField, sortDir, startDate, endDate);
		List<Transaction> transactionList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
		PaginationDto<Transaction> dtoList = new PaginationDto<Transaction>(transactionList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir,
				ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Transaction> getTransactionBySpecification(String employee, Long id, String employeeId,  String employeeName,String department,
			String device,String uId, int pageno, String sortField,
			String sortDir, Date startDate, Date endDate) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno -NumberConstants.ONE , NumberConstants.TEN, sort);

		Specification<Transaction> allSpec = null;
		if(TransactionConstants.ONLY_EMPLOYEE.equalsIgnoreCase(employee)) {
			allSpec = generalSpecification.isNotNullSpecification(TransactionConstants.EMP_ID);
		}else {
			allSpec = generalSpecification.isNotNullSpecification(ApplicationConstants.DELIMITER_EMPTY);
		}
		
		Specification<Transaction> idSpec = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<Transaction> dateSpec = generalSpecification.dateSpecification(startDate, endDate,
				TransactionConstants.PUNCH_DATE);
		Specification<Transaction> empIdSpec = generalSpecification.stringSpecification(employeeId, TransactionConstants.EMP_ID);
		Specification<Transaction> empNameSpec = generalSpecification.stringSpecification(employeeName, ApplicationConstants.NAME);
		Specification<Transaction> deptSpec = generalSpecification.stringSpecification(department, DailyAttendanceConstants.DEPARTMENT);
		Specification<Transaction> devSpec = generalSpecification.stringSpecification(device, TransactionConstants.DEVICE_NAME);
		Specification<Transaction> uIdSpec = generalSpecification.stringSpecification(uId, "uniqueId");

		Page<Transaction> page = transactionRepository.findAll(idSpec.and(dateSpec).and(empIdSpec).and(empNameSpec).and(deptSpec).and(uIdSpec)
				.and(devSpec).and(allSpec), pageable);
		return page;
	}



	


	
}
