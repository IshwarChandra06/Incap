package com.eikona.mata.service;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Transaction;

public interface TransactionService {


	PaginationDto<Transaction> searchByField(String sDate, String eDate, String employeeId, String employeeName,
			String department, String device, String uId, String designation, String area, String emp, String permissionStatus, String deviceType, int pageno,
			String sortField, String sortDir);

}
