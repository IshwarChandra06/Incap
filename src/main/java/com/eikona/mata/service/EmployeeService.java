package com.eikona.mata.service;


import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Employee;


public interface EmployeeService {
	/**
	 * Returns all employee List, which are isDeleted false.
	 * @param
	 */
	List<Employee> getAll();
	/**
	 * This function saves the employee in database according to the respective object.  
	 * @param 
	 */
    Employee save(Employee employee);
    /**
	 * This function retrieves the employee from database according to the respective id.  
	 * @param
	 */
    Employee getById(long id);
    
    /**
	 * This function deletes the employee from database according to the respective id.  
	 * @param
	 */
	void deleteById(long id);
    
	/**
	 * This function retrieves the employee data from the excel file and set into database.
	 * @param file -MultipartFile
	 */
	String storeCosecEmployeeList(MultipartFile file);
	
	String storeEmployeeShiftList(MultipartFile file);
	
	
	PaginationDto<Employee> searchByField(Long id, String name, String empId, String department, String uId, int pageno, String sortField, String sortDir);
	
	



}
