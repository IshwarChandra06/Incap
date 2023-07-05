package com.eikona.mata.util;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.DailyAttendanceConstants;
import com.eikona.mata.constants.DeviceConstants;
import com.eikona.mata.constants.EmployeeConstants;
import com.eikona.mata.constants.HeaderConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.constants.TransactionConstants;
import com.eikona.mata.entity.Transaction;
import com.eikona.mata.repository.TransactionRepository;

@Component
public class ExportEventReport {
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private GeneralSpecificationUtil<Transaction> generalSpecification;
	
	@Autowired
	private TransactionRepository transactionRepository;

	public void fileExportBySearchValue(HttpServletResponse response, String sDate, String eDate, String employeeId,
			String employeeName, String department, String device, String uId, String designation, String area,
			String emp, String deviceType,String permissionStatus) throws Exception {
       List<Transaction> transList = getListOfEvent(sDate,eDate, employeeId, employeeName, department,device,uId,designation,area,emp,permissionStatus,deviceType);
		
		excelGenerator(response, transList);
	}

	private List<Transaction> getListOfEvent(String sDate, String eDate, String employeeId,
			String employeeName, String department, String device, String uId, String designation, String area, String employee, String permissionStatus, String deviceType) {
		Date startDate = null;
		Date endDate = null;
		if (!sDate.isEmpty() && !eDate.isEmpty()) {
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			try {

				startDate = format.parse(sDate);
				endDate = format.parse(eDate);
				endDate = calendarUtil.getConvertedDate(endDate, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Specification<Transaction> empSpec = null;
		if("Unregistered".equalsIgnoreCase(employee)) {
			empSpec = generalSpecification.isNotNullSpecification("empId");
		}else if("Registered".equalsIgnoreCase(employee)) {
			empSpec = generalSpecification.isNullSpecification("empId");
		}else {
			empSpec = generalSpecification.allSpecification();
		}
		
		Specification<Transaction> dateSpec = generalSpecification.dateSpecification(startDate, endDate,TransactionConstants.PUNCH_DATE);
		Specification<Transaction> empIdSpec = generalSpecification.stringSpecification(employeeId, TransactionConstants.EMP_ID);
		Specification<Transaction> empNameSpec = generalSpecification.stringSpecification(employeeName, ApplicationConstants.NAME);
		Specification<Transaction> deptSpec = generalSpecification.stringSpecification(department, DailyAttendanceConstants.DEPARTMENT);
		Specification<Transaction> designationSpec = generalSpecification.stringSpecification(designation, DailyAttendanceConstants.DESIGNATION);
		Specification<Transaction> areaSpec = generalSpecification.stringSpecification(area, "area");
		Specification<Transaction> devSpec = generalSpecification.foreignKeyStringSpecification(device, TransactionConstants.DEVICE, ApplicationConstants.NAME);
		Specification<Transaction> devTypeSpec = generalSpecification.foreignKeyStringSpecification(deviceType, TransactionConstants.DEVICE, DeviceConstants.MODEL);
		Specification<Transaction> uIdSpec = generalSpecification.stringSpecification(uId, "uniqueId");
		Specification<Transaction> permissionStatusSpec = generalSpecification.stringSpecification(permissionStatus, "permissionStatus");
		
		List<Transaction> transList =transactionRepository.findAll(dateSpec.and(empIdSpec).and(empNameSpec).and(deptSpec).and(uIdSpec).and(devTypeSpec)
				.and(devSpec).and(empSpec).and(areaSpec).and(designationSpec).and(permissionStatusSpec));
		return transList;
	}
	private void excelGenerator(HttpServletResponse response, List<Transaction> transList) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = EmployeeConstants.EMPLOYEE_REPORT + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);

		setHeaderForExcel(row, cellStyle);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);
		
		//set data for excel
		setExcelDataCellWise(transList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}
	
	private void setExcelDataCellWise(List<Transaction> transList, Sheet sheet, int rowCount, CellStyle cellStyle) {
		for (Transaction transaction : transList) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getPunchDateStr());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getPunchTimeStr());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getEmpId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getName());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getUniqueId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getDepartment());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getDesignation());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getArea());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(transaction.getDevice().getName());
			cell.setCellStyle(cellStyle);
		}
		
	}

	private CellStyle setBorderStyle(Workbook workBook, BorderStyle borderStyle, Font font) {
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setBorderTop(borderStyle);
		cellStyle.setBorderBottom(borderStyle);
		cellStyle.setBorderLeft(borderStyle);
		cellStyle.setBorderRight(borderStyle);
		cellStyle.setFont(font);
		return cellStyle;
	}
	
	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		int columnCount = NumberConstants.ZERO;
		
		Cell cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.TIME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.EMPLOYEE_ID);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue("Unique Id");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.DEPARTMENT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.DESIGNATION);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.AREA);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.DEVICE);
		cell.setCellStyle(cellStyle);
		
	}

}
