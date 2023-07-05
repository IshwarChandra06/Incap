package com.eikona.mata.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.mata.dto.TransactionDto;
import com.eikona.mata.entity.Transaction;
@Repository
public interface TransactionRepository extends DataTablesRepository<Transaction, Long>{

	Transaction findByAppearanceId(String appearance_id);

	@Query("select t from com.eikona.mata.entity.Transaction t where t.isSync=false and t.empId is not null and t.accessType is not null")
	List<Transaction> findAllByIsSyncFalseCustom();
	
	@Query("SELECT tr FROM com.eikona.mata.entity.Transaction as tr where tr.punchDate >=:sDate and tr.punchDate <=:eDate"
			+ " and tr.empId is not null order by tr.punchDateStr asc, tr.punchTimeStr asc")
	List<Transaction> getTransactionData(Date sDate, Date eDate);

	Transaction findByEmpIdAndPunchDate(String empId, Date punchDate);

	@Query("select t from com.eikona.mata.entity.Transaction t where t.empId is not null and t.punchDateStr=:date order by t.empId,t.punchDate")
	List<Transaction> findAllByDateCustom(String date);
	
	@Query("select t from com.eikona.mata.entity.Transaction t where t.empId is not null and t.department = :department "
			+ "and t.punchDate >= :startDate and t.punchDate <= :endDate order by t.empId,t.punchDate")
	List<Transaction> findAllByDateRangeCustom(String department, Date startDate, Date endDate);

	@Query("select new com.eikona.mata.dto.TransactionDto(t.empId, min(t.punchDate), max(t.punchDate)) from com.eikona.mata.entity.Transaction t where t.empId in :employeeIdList "
			+ "and t.punchDate >= :startDate and t.punchDate <= :endDate and t.device.cosecEnabled=true group by t.empId")
	List<TransactionDto> findMinAndMaxEventByDateCustom(List<String> employeeIdList, Date startDate, Date endDate);
	

	@Query("select new com.eikona.mata.dto.TransactionDto(t.empId, min(t.punchDate), max(t.punchDate)) from com.eikona.mata.entity.Transaction t where t.department = :department "
			+ "and t.punchDate >= :startDate and t.punchDate <= :endDate group by t.empId")
	List<TransactionDto> findMinAndMaxEventByDateAndDepartmentCustom(String department, Date startDate, Date endDate);

	@Query("select count(t.id) from com.eikona.mata.entity.Transaction t where t.empId =:empId "
			+ "and t.punchDate >= :startDate and t.punchDate <= :endDate")
	Long findAllByEmpIdAndDateRangeCustom(String empId, Date startDate, Date endDate);
	
//	@Query("select new com.eikona.mata.dto.TransactionDto(t.empId, min(t.punchDate), max(t.punchDate)) from com.eikona.mata.entity.Transaction t where "
//			+ "t.punchDate >= :startDate and t.punchDate <= :endDate group by t.empId")
//	List<TransactionDto> findMinAndMaxEventByDateAndDepartmentCustom(Date startDate, Date endDate);

}
