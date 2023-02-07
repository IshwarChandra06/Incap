package com.eikona.mata.service.impl;

import java.io.IOException;
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
import com.eikona.mata.constants.DailyAttendanceConstants;
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
	private GeneralSpecificationUtil<Employee> generalSpecificationEmployee;
	
	
	@Override
	public List<Employee> getAll() {
		return (List<Employee>) employeeRepository.findAll();
	}

	@Override
	public Employee save(Employee employee) {
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
	public void deleteById(long id) {
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
	public String storeCosecEmployeeList(MultipartFile file) {
		try {
			List<Employee> employeeList = excelEmployeeImport.parseCosecExcelFileEmployeeList(file.getInputStream());
			employeeRepository.saveAll(employeeList);
			return "File uploaded successfully!";
		} catch (IOException | InvalidFormatException e) {
			e.printStackTrace();
			return "Fail! -> uploaded filename: " + file.getOriginalFilename();
		}
	}
	@Override
	public String storeEmployeeShiftList(MultipartFile file) {
		try {
			List<Employee> employeeList = excelEmployeeImport.parseEmployeeShiftListExcelFile(file.getInputStream());
			employeeRepository.saveAll(employeeList);
			return "File uploaded successfully!";
		} catch (IOException | InvalidFormatException e) {
			e.printStackTrace();
			return "Fail! -> uploaded filename: " + file.getOriginalFilename();
		}
	}
	
	@Override
	public PaginationDto<Employee> searchByField(Long id,String name,String empId,String department, String uId,int pageno, String sortField, String sortDir) {

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Employee> page = getEmployeePage(id, name, empId,department,uId,pageno, sortField,
				sortDir);
        List<Employee> employeeList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Employee> dtoList = new PaginationDto<Employee>(employeeList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Employee> getEmployeePage(Long id, String name, String empId,String department, String uId, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Employee> isDeletedSpc = generalSpecificationEmployee.booleanSpecification(false, "isDeleted");
		Specification<Employee> idSpc = generalSpecificationEmployee.longSpecification(id, ApplicationConstants.ID);
		Specification<Employee> nameSpc = generalSpecificationEmployee.stringSpecification(name, ApplicationConstants.NAME);
		Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(empId, EmployeeConstants.EMPID);
		Specification<Employee> departmentSpc = generalSpecificationEmployee.stringSpecification(department, DailyAttendanceConstants.DEPARTMENT);
		Specification<Employee> uIdSpec = generalSpecificationEmployee.stringSpecification(uId,"uniqueId");
    	Page<Employee> page = employeeRepository.findAll(idSpc.and(nameSpc).and(empIdSpc).and(departmentSpc).and(uIdSpec).and(isDeletedSpc),pageable);
		return page;
	}
	



	

}