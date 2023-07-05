package com.eikona.mata.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.mata.entity.Employee;
import com.eikona.mata.entity.Organization;


@Repository
public interface EmployeeRepository extends DataTablesRepository<Employee, Long> {

	
	@Query("select emp.id from com.eikona.mata.entity.Employee as emp where emp.empId=:empId and emp.isDeleted=false")
	Long findIdByEmpIdCustom(String empId);
	
	Employee findByEmpIdAndIsDeletedFalse(String empId);
	
	@Query("select emp.empId from com.eikona.mata.entity.Employee as emp where emp.isDeleted=false")
	List<String> getEmpIdAndIsDeletedFalseCustom();

	@Query("select emp.empId from com.eikona.mata.entity.Employee as emp where emp.isDeleted=false and emp.department.name in ('HR','IT','FINANCE')")
	List<String> findAllEmployeeByDepartmentCustom();
	
	@Query("select emp.empId from com.eikona.mata.entity.Employee as emp where emp.isDeleted=false")
	List<String> findAllEmployeeIdCustom();

	List<Employee> findAllByIsDeletedFalseAndIsSyncTrueAndIsFaceSyncFalse();
	
	@Query("Select e from com.eikona.mata.entity.Employee e where e.isDeleted=false and e.isSync=false and NOT(e.area IS EMPTY)")
	List<Employee> findAllByIsDeletedFalseAndIsSyncFalseCustom();

	List<Employee> findAllByIsDeletedTrueAndIsSyncTrue();
	
	@Query("Select e from com.eikona.mata.entity.Employee e where e.isDeleted=false and e.organization.name=:org and NOT(e.area IS EMPTY)")
	List<Employee> findAllByIsDeletedFalseAndOrganizationAndArea(String org);
	
	@Query("select count(e.id) from com.eikona.mata.entity.Employee e JOIN e.area as a where e.isDeleted = false and e.isSync=false and e.isFaceSync=false and e.organization.name=:org"
			+" and a.id = :id")
	long countEmployeeAndIsDeletedFalseCustom(String org, Long id);
	
	@Query("select e from com.eikona.mata.entity.Employee e JOIN e.area as a where e.isDeleted = false and e.isSync=false and e.isFaceSync=false and e.organization.name=:org"
			+ " and a.id = :id")
	List<Employee> findByIsDeletedFalseCustom(String org,Long id, Pageable paging);
	
	@Query("Select e from com.eikona.mata.entity.Employee e where e.isDeleted=false and e.organization= :organization and e.area IS EMPTY")
	List<Employee> findAllByOrganizationAndEmptyAreaCustom(Organization organization);

	List<Employee> findAllByIsDeletedFalse();


}