package com.eikona.mata.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.mata.entity.DailyReport;


@Repository
public interface DailyAttendanceRepository extends DataTablesRepository<DailyReport, Long> {

	@Query("SELECT  dr FROM com.eikona.mata.entity.DailyReport as dr where dr.date >= :sDate and dr.date <= :eDate")
		List<DailyReport> findByDateAndOrganization(Date sDate,Date eDate);

	DailyReport findByEmpIdAndDate(String empId, Date currDate);
	
	@Query("SELECT  dr FROM com.eikona.mata.entity.DailyReport as dr where dr.empId = :empId and dr.date between :sDate and :eDate order by dr.date asc")
	List<DailyReport> findDetailsByDateCustom(String empId, Date sDate, Date eDate);
	

}
