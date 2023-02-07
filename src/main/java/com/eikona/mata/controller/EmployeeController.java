package com.eikona.mata.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Employee;
import com.eikona.mata.service.EmployeeService;
import com.eikona.mata.util.ExportEmployee;

@Controller
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private ExportEmployee exportEmployee;

	@GetMapping({ "/", "/employee" })
	@PreAuthorize("hasAuthority('employee_view')")
	public String employeeList(Model model) {
		return "employee/employee_list";
	}

	// Import Employee
	@GetMapping("/import/employee-list")
	@PreAuthorize("hasAuthority('employee_import')")
	public String importCosecEmployeeList() {
		return "multipartfile/uploadCosecEmployeeList";
	}

	@PostMapping("/upload/cosec/employee-list/excel")
	@PreAuthorize("hasAuthority('employee_import')")
	public String uploadCosecEmployeeList(@RequestParam("uploadfile") MultipartFile file, Model model) {
		String message = employeeService.storeCosecEmployeeList(file);
		model.addAttribute("message", message);
		return "multipartfile/uploadCosecEmployeeList";
	}

	// Import Employee
	@GetMapping("/import/employee-shift-list")
	@PreAuthorize("hasAuthority('employee_import')")
	public String importEmployeeShiftList() {
		return "multipartfile/uploadEmployeeShiftList";
	}

	@PostMapping("/upload/employee-shift-list/excel")
	@PreAuthorize("hasAuthority('employee_import')")
	public String uploadEmployeeShiftList(@RequestParam("uploadfile") MultipartFile file, Model model) {
		String message = employeeService.storeEmployeeShiftList(file);
		model.addAttribute("message", message);
		return "multipartfile/uploadEmployeeShiftList";
	}

	@PostMapping("/employee/add")
	@PreAuthorize("hasAnyAuthority('employee_create','employee_update')")
	public String saveEmployee(@ModelAttribute("employee") Employee employee, Model model, @Valid Employee emp,
			Errors errors, String title) {
		if (errors.hasErrors()) {
			model.addAttribute("title", title);
			return "employee/employee_new";
		} else {
			if (null == employee.getId()) {
				employee = employeeService.save(employee);

			} else {
				Employee employeeObj = employeeService.getById(employee.getId());
				employee.setCreatedBy(employeeObj.getCreatedBy());
				employee.setCreatedDate(employeeObj.getCreatedDate());
				employee = employeeService.save(employee);

			}

			return "redirect:/employee";
		}
	}

	@GetMapping("/employee/edit/{id}")
	@PreAuthorize("hasAuthority('employee_update')")
	public String editEmployee(@PathVariable(value = "id") long id, Model model) {

		Employee employee = employeeService.getById(id);
		model.addAttribute("employee", employee);
		model.addAttribute("title", "Update Employee");
		return "employee/employee_new";
	}

	@GetMapping("/employee/delete/{id}")
	@PreAuthorize("hasAuthority('employee_delete')")
	public String deleteEmployee(@PathVariable(value = "id") long id) {
		this.employeeService.deleteById(id);
		return "redirect:/employee";
	}

	@RequestMapping(value = "/api/search/employee", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody PaginationDto<Employee> searchEmployee(Long id, String name, String empId, String department,String uId,
			int pageno, String sortField, String sortDir, Principal principal) {

		PaginationDto<Employee> dtoList = employeeService.searchByField(id, name, empId, department,uId, pageno, sortField,
				sortDir);
		return dtoList;
	}

	@RequestMapping(value = "/api/employee/export-to-excel", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_export')")
	public void exportToFile(HttpServletResponse response, Long id, String name, String empId, String department, String uId,
			String flag, Principal principal) {
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		String currentDateTime = dateFormat.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Employee_master_data" + currentDateTime + "." + flag;
		response.setHeader(headerKey, headerValue);
		try {
			exportEmployee.fileExportBySearchValue(response, id, name, empId, department,uId, flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
