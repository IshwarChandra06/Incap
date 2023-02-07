package com.eikona.mata.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.mata.entity.Employee;


@Repository
public interface EmployeeRepository extends DataTablesRepository<Employee, Long> {

	
	@Query("select emp.id from com.eikona.mata.entity.Employee as emp where emp.empId=:empId and emp.isDeleted=false")
	Long findIdByEmpIdCustom(String empId);
	
	Employee findByEmpIdAndIsDeletedFalse(String empId);
	
	@Query("select emp.empId from com.eikona.mata.entity.Employee as emp where emp.isDeleted=false")
	List<String> getEmpIdAndIsDeletedFalseCustom();

	@Query("select emp.empId from com.eikona.mata.entity.Employee as emp where emp.isDeleted=false and emp.department in ('HR','IT','FINANCE')")
	List<String> findAllEmployeeByDepartmentCustom();
	



}