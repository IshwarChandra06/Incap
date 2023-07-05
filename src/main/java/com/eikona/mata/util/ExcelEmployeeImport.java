package com.eikona.mata.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.MessageConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.entity.Department;
import com.eikona.mata.entity.Designation;
import com.eikona.mata.entity.Employee;
import com.eikona.mata.entity.Organization;
import com.eikona.mata.entity.Shift;
import com.eikona.mata.entity.User;
import com.eikona.mata.repository.DepartmentRepository;
import com.eikona.mata.repository.DesignationRepository;
import com.eikona.mata.repository.EmployeeRepository;
import com.eikona.mata.repository.ShiftRepository;
import com.eikona.mata.repository.UserRepository;

@Component
public class ExcelEmployeeImport {

	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ShiftRepository shiftRepository;
	
	@Autowired
	private DepartmentRepository departmentrepository;
	
	@Autowired
	private DesignationRepository designationRepository;
	
	@Autowired
	private UserRepository userRepository;
	

	
	
private Employee cosecExcelRowToEmployee(Row currentRow,Map<String, Department> deptMap,Map<String, Designation> desigMap, Organization org) {
		
		Employee employeeObj = null;
		
		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();

		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();
			cellIndex = currentCell.getColumnIndex();
			if (null == employeeObj) {
				break;
			}
			else if (cellIndex == NumberConstants.ZERO) {
				String str=setString(employeeObj, currentCell);
				employeeObj.setEmpId(str);
				employeeObj.setOrganization(org);
			} else if (cellIndex == NumberConstants.ONE) {
				String str=setString(employeeObj, currentCell);
				employeeObj.setUniqueId(str);
			}else if (cellIndex == NumberConstants.TWO) {
				employeeObj.setName(currentCell.getStringCellValue().trim());
			}
			else if (cellIndex == NumberConstants.THREE) {
				setDepartment(deptMap,employeeObj,currentCell);
			}
			else if (cellIndex == NumberConstants.FOUR) {
				setDesignation(desigMap, employeeObj, currentCell);
			}

		}
		return employeeObj;
		
	}
public Map<String, Department> getDepartment(){
	List<Department> departmentList = departmentrepository.findAllByIsDeletedFalse();
	Map<String, Department> deptMap = new HashMap<String, Department>();
	
	for(Department department: departmentList ) {
		deptMap.put(department.getName(), department);
	}
	return deptMap;
}

public Map<String, Designation> getDesignation(){
	List<Designation> desigList = designationRepository.findAllByIsDeletedFalse();
	Map<String, Designation> desigMap = new HashMap<String, Designation>();
	
	for(Designation desig: desigList ) {
		desigMap.put(desig.getName(), desig);
	}
	return desigMap;
}
private void setDepartment(Map<String, Department> deptMap, Employee employeeObj,Cell currentCell) {
	String str = currentCell.getStringCellValue().trim();

	if (null != str && !str.isEmpty()) {
		
		Department department = deptMap.get(str);
		if (null == department) {
			department = new Department();
			department.setName(str);
			departmentrepository.save(department);
			deptMap.put(department.getName(), department);
		}
		employeeObj.setDepartment(department);
	}
}

private void setDesignation(Map<String, Designation> desigMap, Employee employeeObj,Cell currentCell) {
	String str = currentCell.getStringCellValue().trim();

	if (null != str && !str.isEmpty()) {
		
		Designation designation = desigMap.get(str);
		if (null == designation) {
			designation = new Designation();
			designation.setName(str);
			designationRepository.save(designation);
			desigMap.put(designation.getName(), designation);
		}
		employeeObj.setDesignation(designation);
	}
}
	public List<Employee> parseCosecExcelFileEmployeeList(InputStream inputStream, Principal principal) throws InvalidFormatException {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			

			int rowNumber = NumberConstants.ZERO;
			List<String> empIdList=employeeRepository.getEmpIdAndIsDeletedFalseCustom();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;
				Map<String, Department> deptMap = getDepartment();
				Map<String, Designation> desigMap = getDesignation();
				User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
				Organization org= user.getOrganization();
				Employee employee=cosecExcelRowToEmployee(currentRow,deptMap,desigMap,org);
				
				boolean isContains=empIdList.contains(employee.getEmpId());
				
				if(!isContains  && null!=employee.getEmpId()&& !employee.getEmpId().isEmpty())
				 employeeList.add(employee);
				
				if(rowNumber%NumberConstants.HUNDRED==NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}
					
					
			}
			
			if(!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}
			
			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}

	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private String setString(Employee employeeObj, Cell currentCell) {
		String str="";
		currentCell.setCellType(CellType.STRING);
		if (currentCell.getCellType() == CellType.NUMERIC) {
			str= String.valueOf(currentCell.getNumericCellValue()).trim();
		} else if (currentCell.getCellType() == CellType.STRING) {
			str= currentCell.getStringCellValue().trim();
		}
		return str;
	}
	
//	@SuppressWarnings(ApplicationConstants.DEPRECATION)
//	private void setUniqueId(Employee employeeObj, Cell currentCell) {
//		currentCell.setCellType(CellType.STRING);
//		if (currentCell.getCellType() == CellType.NUMERIC) {
//			employeeObj.setUniqueId(String.valueOf(currentCell.getNumericCellValue()).trim());
//		} else if (currentCell.getCellType() == CellType.STRING) {
//			employeeObj.setUniqueId(currentCell.getStringCellValue().trim());
//		}
//	}
	public List<Employee> parseEmployeeShiftListExcelFile(InputStream inputStream)  throws InvalidFormatException {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			

			int rowNumber = NumberConstants.ZERO;
			List<String> empIdList=employeeRepository.getEmpIdAndIsDeletedFalseCustom();
			Map<String, Shift> shiftMap = getShiftByName();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;
				
				Employee employee=ExcelRowToEmployeeShift(currentRow,shiftMap);
				
				boolean isContains=empIdList.contains(employee.getEmpId());
				
				if(!isContains && null!=employee.getShift()  && null!=employee.getEmpId()&& !employee.getEmpId().isEmpty())
				employeeList.add(employee);
				
				if(rowNumber%NumberConstants.HUNDRED==NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}
					
					
			}
			
			if(!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}
			
			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}
	private Employee ExcelRowToEmployeeShift(Row currentRow, Map<String, Shift> shiftMap) {
		
		Employee employeeObj = null;
		
		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();
		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();

			if (null == employeeObj) {
				break;
			}

			else if (cellIndex == NumberConstants.ZERO) {
				String str=setString(employeeObj, currentCell);
				employeeObj.setEmpId(str);
			} else if (cellIndex == NumberConstants.ONE) {
				setShift(shiftMap, employeeObj, currentCell);
			}

			cellIndex++;
		}
		return employeeObj;
		
	}

private void setShift( Map<String, Shift> shiftMap, Employee employeeObj,Cell currentCell) {
	String str = currentCell.getStringCellValue().trim();

	if (null != str && !str.isEmpty()) {
		
		Shift shift = shiftMap.get(str);
		if (null == shift) {
			shift = new Shift();
			shift.setName(str);
			shiftRepository.save(shift);
			shiftMap.put(shift.getName(), shift);
		}
		employeeObj.setShift(shift);
	}
}

private Map<String, Shift> getShiftByName(){
	List<Shift> shiftList = shiftRepository.findAllByIsDeletedFalse();
	Map<String, Shift> shiftMap = new HashMap<String, Shift>();
	
	for(Shift shift: shiftList ) {
		shiftMap.put(shift.getName(), shift);
	}
	return shiftMap;
}

public void parseEmployeeListForDelete(InputStream inputStream) {

	try {

		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

		Iterator<Row> rows = sheet.iterator();

		int rowNumber = NumberConstants.ZERO;
		while (rows.hasNext()) {
			Row currentRow = rows.next();

			// skip header
			if (rowNumber == NumberConstants.ZERO) {
				rowNumber++;
				continue;
			}

			rowNumber++;

			Iterator<Cell> cellsInRow = currentRow.iterator();
			int cellIndex = NumberConstants.ZERO;

			while (cellsInRow.hasNext()) {
				Cell currentCell = cellsInRow.next();
				cellIndex = currentCell.getColumnIndex();

				 if (cellIndex == NumberConstants.ZERO) {
					String value = getStringValue(currentCell);
					Employee employee = employeeRepository.findByEmpIdAndIsDeletedFalse(value.trim());
					if (null!=employee) {
						employee.setDeleted(true);
						employeeRepository.save(employee);
					}
				}

		}


		workbook.close();

	}
	}catch (IOException e) {
		throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
	}

}

@SuppressWarnings(ApplicationConstants.DEPRECATION)
private String getStringValue(Cell currentCell) {
	currentCell.setCellType(CellType.STRING);
	String value = "";
	if (currentCell.getCellType() == CellType.NUMERIC) {
		value = String.valueOf(currentCell.getNumericCellValue());
	} else if (currentCell.getCellType() == CellType.STRING) {
		value = currentCell.getStringCellValue();
	}
	return value;
}
}
