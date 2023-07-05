package com.eikona.mata.service.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.AreaConstants;
import com.eikona.mata.constants.EmployeeConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Employee;
import com.eikona.mata.repository.EmployeeRepository;
import com.eikona.mata.service.EmployeeService;
import com.eikona.mata.util.ExcelEmployeeImport;
import com.eikona.mata.util.GeneralSpecificationUtil;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ExcelEmployeeImport excelEmployeeImport;
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecification;
	
	
	@Override
	public List<Employee> getAll() {
		return employeeRepository.findAllByIsDeletedFalse();
	}

	@Override
	public Employee save(Employee employee) {
		employee.setDeleted(false);
		return this.employeeRepository.save(employee);
	}

	@Override
	public Employee getById(long id) {
		Optional<Employee> optional = employeeRepository.findById(id);
		Employee employee = null;
		if (optional.isPresent()) {
			employee = optional.get();
		} else {
			throw new RuntimeException(EmployeeConstants.EMPLOYEE_NOT_FOUND+ id);
		}
		return employee;
	}

	@Override
	public void deleteById(long id,Principal principal) {
		Optional<Employee> optional = employeeRepository.findById(id);
		Employee employee = null;
		if (optional.isPresent()) {
			employee = optional.get();
			employee.setDeleted(true);
		} else {
			throw new RuntimeException(EmployeeConstants.EMPLOYEE_NOT_FOUND + id);
		}
		this.employeeRepository.save(employee);

	}
	
	@Override
	public String storeCosecEmployeeList(MultipartFile file,Principal principal) {
		try {
			List<Employee> employeeList = excelEmployeeImport.parseCosecExcelFileEmployeeList(file.getInputStream(),principal);
			employeeRepository.saveAll(employeeList);
			return "File uploaded successfully!";
		} catch (IOException | InvalidFormatException e) {
			e.printStackTrace();
			return "Fail! -> uploaded filename: " + file.getOriginalFilename();
		}
	}
	
	@Override
	public PaginationDto<Employee> searchByField(String name, String empId, String department,String designation, String uId, int pageno, String sortField, String sortDir, String orgname) {

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Employee> page = getEmployeePage(name, empId,department,designation,uId,pageno, sortField,
				sortDir,orgname);
        List<Employee> employeeList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Employee> dtoList = new PaginationDto<Employee>(employeeList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Employee> getEmployeePage(String name, String empId, String department, String designation, String uniqueId, int pageno, String sortField, String sortDir, String orgName) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Employee> employeeNameSpec = generalSpecification.stringSpecification(name,EmployeeConstants.NAME);
		Specification<Employee> employeeIdSpec = generalSpecification.stringSpecification(empId,EmployeeConstants.EMPID);
		Specification<Employee> departmentSpec = generalSpecification.foreignKeyStringSpecification(department,EmployeeConstants.DEPARTMENT,EmployeeConstants.NAME);
		Specification<Employee> designationSpec = generalSpecification.foreignKeyStringSpecification(designation,EmployeeConstants.DESIGNATION,EmployeeConstants.NAME);
		Specification<Employee> orgSpec = generalSpecification.foreignKeyStringSpecification(orgName, AreaConstants.ORGANIZATION,EmployeeConstants.NAME);
		Specification<Employee> uIdSpec = generalSpecification.stringSpecification(uniqueId,"uniqueId");
		Specification<Employee> isDeletedFalseSpec = generalSpecification.booleanSpecification(false, "isDeleted");
    	Page<Employee> page = employeeRepository.findAll(employeeNameSpec.and(employeeIdSpec).and(departmentSpec).and(uIdSpec).and(designationSpec).and(isDeletedFalseSpec).and(orgSpec),pageable);
		return page;
	}

	@Override
	public String deleteEmployeeList(MultipartFile file) {
		try {
			excelEmployeeImport.parseEmployeeListForDelete(file.getInputStream());
			return "Employee Deleted successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Fail! -> uploaded filename: " + file.getOriginalFilename();
		}
	}

	@Override
	public void saveEmployeeAreaAssociation(Employee employee, Long id) {
		Employee employeeObj = getById(id);
		employeeObj.setArea(employee.getArea());
		employeeRepository.save(employeeObj);
	}	



	

}