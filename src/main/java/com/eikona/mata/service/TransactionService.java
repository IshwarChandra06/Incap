package com.eikona.mata.service;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Transaction;

public interface TransactionService {


	PaginationDto<Transaction> searchByField(String employee, Long id, String sDate, String eDate, String employeeId,
			String employeeName, String employeeType, String device, String uId, int pageno, String sortField,
			String sortDir);

}
