package com.eikona.mata.util;

import java.io.IOException;
import java.io.InputStream;
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
import com.eikona.mata.entity.Employee;
import com.eikona.mata.entity.Shift;
import com.eikona.mata.repository.EmployeeRepository;
import com.eikona.mata.repository.ShiftRepository;

@Component
public class ExcelEmployeeImport {

	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ShiftRepository shiftRepository;
	

	
	
private Employee cosecExcelRowToEmployee(Row currentRow) {
		
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
				setUniqueId(employeeObj, currentCell);
			}
			else if (cellIndex == NumberConstants.ONE) {
				setEmpId(employeeObj, currentCell);
			} else if (cellIndex == NumberConstants.TWO) {
				employeeObj.setName(currentCell.getStringCellValue().trim());
			}
			else if (cellIndex == NumberConstants.THREE) {
				employeeObj.setDepartment(currentCell.getStringCellValue().trim());
			}

			cellIndex++;
		}
		return employeeObj;
		
	}
	public List<Employee> parseCosecExcelFileEmployeeList(InputStream inputStream) throws InvalidFormatException {
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
				
				Employee employee=cosecExcelRowToEmployee(currentRow);
				
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
	private void setEmpId(Employee employeeObj, Cell currentCell) {
		currentCell.setCellType(CellType.STRING);
		if (currentCell.getCellType() == CellType.NUMERIC) {
			employeeObj.setEmpId(String.valueOf(currentCell.getNumericCellValue()).trim());
		} else if (currentCell.getCellType() == CellType.STRING) {
			employeeObj.setEmpId(currentCell.getStringCellValue().trim());
		}
	}
	
	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private void setUniqueId(Employee employeeObj, Cell currentCell) {
		currentCell.setCellType(CellType.STRING);
		if (currentCell.getCellType() == CellType.NUMERIC) {
			employeeObj.setUniqueId(String.valueOf(currentCell.getNumericCellValue()).trim());
		} else if (currentCell.getCellType() == CellType.STRING) {
			employeeObj.setUniqueId(currentCell.getStringCellValue().trim());
		}
	}
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
				setEmpId(employeeObj, currentCell);
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
}
