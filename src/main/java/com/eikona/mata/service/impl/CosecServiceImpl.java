package com.eikona.mata.service.impl;

import static com.eikona.mata.constants.ApplicationConstants.DELIMITER_COLON;
import static com.eikona.mata.constants.ApplicationConstants.SUCCESS;
import static com.eikona.mata.constants.NumberConstants.ZERO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.TransactionConstants;
import com.eikona.mata.dto.TransactionDto;
import com.eikona.mata.entity.Transaction;
import com.eikona.mata.repository.EmployeeRepository;
import com.eikona.mata.repository.TransactionRepository;
import com.eikona.mata.util.CalendarUtil;
import com.eikona.mata.util.CosecTransactionUtil;
import com.eikona.mata.util.GeneralSpecificationUtil;

@Service
@EnableScheduling
public class CosecServiceImpl {

	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private CosecTransactionUtil cosecTransactionUtil;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private GeneralSpecificationUtil<Transaction> generalSpecification;
	
	@Scheduled(cron = "0 0 1 * * *")
	public void autoSyncTransactionToCosec() {
		List<String> employeeIdList = employeeRepository.findAllEmployeeByDepartmentCustom();
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		
		String dateStr = dateFormat.format(new Date());
		
		try {
			Date startDate = calendarUtil.getPreviousDate(dateFormat.parse(dateStr), -1, 0, 0, 0);
			Date endDate = calendarUtil.getPreviousDate(dateFormat.parse(dateStr), -1, 23, 59, 59);
			
			List<TransactionDto> transactionDtoList = transactionRepository.findMinAndMaxEventByDateCustom(employeeIdList, startDate, endDate);
			for(TransactionDto transactionDto : transactionDtoList) {
				cosecTransactionUtil.transactionAutoSyncToCosec(transactionDto.getEmpId(), transactionDto.getCheckInTime(), 0);
				if(transactionDto.getCheckInTime().before(transactionDto.getCheckOutTime())) {
					cosecTransactionUtil.transactionAutoSyncToCosec(transactionDto.getEmpId(), transactionDto.getCheckOutTime(), 1);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void syncEventFromMataToCosecDepartmentWise(String sDate, String eDate, String department) throws Exception {
		SimpleDateFormat inputformatStr = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SLASH);
		SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);

		    Date startDate = null;
		    Date endDate = null;
			startDate = inputformatStr.parse(sDate);
			endDate = inputformatStr.parse(eDate);

			String sDateStr = format.format(startDate);
			String eDateStr = format.format(endDate);

			startDate = format.parse(sDateStr);
			endDate = format.parse(eDateStr);
			
			List<TransactionDto> transactionDtoList = transactionRepository.findMinAndMaxEventByDateAndDepartmentCustom(department,startDate, endDate);
				for(TransactionDto transactionDto : transactionDtoList) {
					cosecTransactionUtil.transactionAutoSyncToCosec(transactionDto.getEmpId(), transactionDto.getCheckInTime(), 0);
					if(transactionDto.getCheckInTime().before(transactionDto.getCheckOutTime())) {
						cosecTransactionUtil.transactionAutoSyncToCosec(transactionDto.getEmpId(), transactionDto.getCheckOutTime(), 1);
					}
				}
				
	}
	//------------------------------------------------------------------->Old Syncing Concept<------------------------------------------------------------------//
//	@Scheduled(cron = "0 0 0/1 * * ?")
	public void sendTransactionToCosec() throws Exception {
		List<Transaction> transactions= transactionRepository.findAllByIsSyncFalseCustom();
		List<Transaction> transactionList=new ArrayList<Transaction>();
		for(Transaction trans : transactions) {
			
			String response = cosecTransactionUtil.transactionSendToCosec(trans);
			
			String[] parts = response.split(DELIMITER_COLON);
			String message=parts[ZERO];
			if(SUCCESS.equalsIgnoreCase(message)) {
				trans.setSync(true);
				trans.setFailed(false);
			}else {
				trans.setSync(false);
				trans.setFailed(true);
			}
			transactionList.add(trans);
		}
		transactionRepository.saveAll(transactionList);
	}
	
	public void syncTransactionFromMataToCosec(String sDate, String eDate) throws Exception{
		SimpleDateFormat inputformatStr = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SLASH);
		SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);

		    Date startDate = null;
		    Date endDate = null;
			startDate = inputformatStr.parse(sDate);
			endDate = inputformatStr.parse(eDate);

			String sDateStr = format.format(startDate);
			String eDateStr = format.format(endDate);

			startDate = format.parse(sDateStr);
			endDate = format.parse(eDateStr);
			
			Specification<Transaction> dateSpec = generalSpecification.dateSpecification(startDate, endDate,TransactionConstants.PUNCH_DATE);
			Specification<Transaction> isSyncSpec = generalSpecification.booleanSpecification(false, "isSync");
			Specification<Transaction> empIdSpec = generalSpecification.isNotNullSpecification(TransactionConstants.EMP_ID);
			Specification<Transaction> accessTypeSpec = generalSpecification.isNotNullSpecification("accessType");
			
			List<Transaction> transactions = transactionRepository.findAll(dateSpec.and(accessTypeSpec).and(empIdSpec).and(isSyncSpec));
			
			List<Transaction> transactionList = new ArrayList<Transaction>();
			for (Transaction trans : transactions) {

				String response = cosecTransactionUtil.transactionSendToCosec(trans);
				if(null!= response) {
					String[] parts = response.split(DELIMITER_COLON);
					String msg = parts[ZERO];
					if (SUCCESS.equalsIgnoreCase(msg)) {
						trans.setSync(true);
						trans.setFailed(false);
					} else {
						trans.setSync(false);
						trans.setFailed(true);
					}
					transactionList.add(trans);
				}
				
			}
			transactionRepository.saveAll(transactionList);
		}

	
}
	

