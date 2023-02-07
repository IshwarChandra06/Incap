package com.eikona.mata.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.DailyAttendanceConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.DailyReport;
import com.eikona.mata.entity.Transaction;
import com.eikona.mata.repository.DailyAttendanceRepository;
import com.eikona.mata.repository.TransactionRepository;
import com.eikona.mata.service.DailyAttendanceService;
import com.eikona.mata.util.CalendarUtil;
import com.eikona.mata.util.GeneralSpecificationUtil;

@Service
@EnableScheduling
public class DailyAttendanceServiceImpl implements DailyAttendanceService{

	@Autowired
	private DailyAttendanceRepository dailyAttendanceRepository;

	@Autowired
	private GeneralSpecificationUtil<DailyReport> generalSpecificationDailyAttendance;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Value("${dailyreport.autogenerate.enabled}")
	private String enableGenerate;
	
//	@Scheduled(cron ="0 0 9 * * *")
	public void autoGenerateDailyReport() {
		
		if("Yes".equalsIgnoreCase(enableGenerate)) {
			SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date yesterday=calendarUtil.getPreviousDate(new Date(), -1, 0, 0,0);
			String date=inputFormat.format(yesterday);
			generateDailyAttendance(date,date);
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public List<DailyReport> generateDailyAttendance(String sDate, String eDate) {

		List<DailyReport> dailyReportList = new ArrayList<>();
		try {
			
			SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
			sDate = format.format(inputFormat.parse(sDate));
			eDate = format.format(inputFormat.parse(eDate));
			
			Date startDate = calendarUtil.getConvertedDate(format.parse(sDate), 00, 00, 00);
			Date endDate = calendarUtil.getConvertedDate(format.parse(eDate), 23, 59, 59);
	
			List<Transaction> transactionList = transactionRepository.getTransactionData(startDate,endDate);
			
			List<DailyReport> dailyAttList = dailyAttendanceRepository.findByDateAndOrganization(startDate,endDate);

			Map<String, DailyReport> existingReportMap = new HashMap<String, DailyReport>();
			
			for (DailyReport dailyReport : dailyAttList) {
				String key = dailyReport.getEmpId() + "-" + dailyReport.getDateStr();
				key = key.trim();
				existingReportMap.put(key, dailyReport);
			}
			
			Map<String, DailyReport> reportMap = new HashMap<String, DailyReport>();

			for (Transaction transaction : transactionList) {
	
				try {
					
					String key = transaction.getEmpId() + "-" + transaction.getPunchDateStr();
					key = key.trim();
					DailyReport dailyReport = null;
					
					Date currDate = format.parse(transaction.getPunchDateStr());
					if(transaction.getOrganization().contains("Incap")  && transaction.getPunchDate().getHours() < 8) {
						
						Calendar currDateCal = Calendar.getInstance();
						currDateCal.setTime(currDate);
						currDateCal.add(Calendar.DAY_OF_MONTH, -1);
						currDate = currDateCal.getTime();
						
						DailyReport dailyReportYesterday = dailyAttendanceRepository.findByEmpIdAndDate(transaction.getEmpId().trim(),currDate);
						
						if(null!=dailyReportYesterday) {
							if(Integer.parseInt(dailyReportYesterday.getEmpInTime().split(":")[0]) < 20) {
								currDate = format.parse(transaction.getPunchDateStr());
							}else {
								key = dailyReportYesterday.getEmpId() + "-" + dailyReportYesterday.getDateStr();
								key = key.trim();
								if(!existingReportMap.containsKey(key)) {
									existingReportMap.put(key, dailyReportYesterday);
								}
							}
						}
					}
					
					if(existingReportMap.containsKey(key)) {
						reportMap.put(key, existingReportMap.get(key));
					}
					
					if(!reportMap.containsKey(key)) {
						
						dailyReport = new DailyReport();
						dailyReport.setEmpId(transaction.getEmpId().trim());
						dailyReport.setDateStr(transaction.getPunchDateStr());
						dailyReport.setDate(format.parse(transaction.getPunchDateStr()));
						dailyReport.setEmployeeName(transaction.getName());
						dailyReport.setOrganization(transaction.getOrganization());
						dailyReport.setDepartment(transaction.getDepartment());
						dailyReport.setUserType("Employee");
						dailyReport.setMissedOutPunch(true);
						dailyReport.setEmpInTime(transaction.getPunchTimeStr());
						dailyReport.setEmpInTemp(transaction.getTemperature());
						dailyReport.setEmpInMask(transaction.getWearingMask());
						dailyReport.setEmpInAccessType(transaction.getAccessType());
						dailyReport.setEmpInLocation(transaction.getDeviceName());
						dailyReport.setAttendanceStatus("Present");
						String city = "";
						 if(transaction.getOrganization().contains("Incap") && null!=transaction.getShift()) {
							city = "Tumkur";
							
							dailyReport.setShift(transaction.getShift().getName());
							dailyReport.setShiftInTime(timeFormat.format(transaction.getShift().getStartTime()));
							dailyReport.setShiftOutTime(timeFormat.format(transaction.getShift().getEndTime()));
							
//							dailyReport.setShift("General");
//							dailyReport.setShiftInTime("09:00:00");
//							dailyReport.setShiftOutTime("17:30:00");
//							
//							int hour = Integer.parseInt(transaction.getPunchTimeStr().split(":")[0]);
//							if(hour <= 7) {
//									dailyReport.setShift("1st Shift"); //5:30 to 13:00
//									dailyReport.setShiftInTime("06:00:00"); 
//									dailyReport.setShiftOutTime("14:00:00");
//							}
//							
//							if(hour >= 12) {
//									dailyReport.setShift("2nd Shift"); //13:30 to 20:30
//									dailyReport.setShiftInTime("14:00:00");
//									dailyReport.setShiftOutTime("22:00:00");
//							}
//							
//							if(hour >= 20) {
//								dailyReport.setShift("3rd Shift"); //21:30 to 4:30 9to 5:30 
//								dailyReport.setShiftInTime("22:00:00");
//								dailyReport.setShiftOutTime("06:00:00");
//							}
						}
						
						dailyReport.setCity(city);
	
						LocalTime shiftIn = LocalTime.parse(dailyReport.getShiftInTime());
						LocalTime empIn = LocalTime.parse(dailyReport.getEmpInTime().replace("'", ""));
	
						Long lateComing = shiftIn.until(empIn, ChronoUnit.MINUTES);
						Long earlyComing = empIn.until(shiftIn, ChronoUnit.MINUTES);
	
						if (earlyComing > 0)
							dailyReport.setEarlyComing(earlyComing);
	
						if (lateComing > 0)
							dailyReport.setLateComing(lateComing);
						
						dailyReport.setCity(city);
						reportMap.put(key, dailyReport);
					} else {
						
						dailyReport = reportMap.get(key);
						if (dailyReport.getEmpInTime().equalsIgnoreCase(transaction.getPunchTimeStr())) {
							continue;
						} else {
							
							dailyReport.setEmpOutTime(transaction.getPunchTimeStr());
							dailyReport.setEmpOutTemp(transaction.getTemperature());
							dailyReport.setEmpOutMask(transaction.getWearingMask());
							dailyReport.setEmpOutAccessType(transaction.getAccessType());
							dailyReport.setEmpOutLocation(transaction.getDeviceName());
	
							dailyReport.setMissedOutPunch(false);
	
							LocalTime shiftIn = LocalTime.parse(dailyReport.getShiftInTime());
							LocalTime shiftOut = LocalTime.parse(dailyReport.getShiftOutTime());
							LocalTime empIn = LocalTime.parse(dailyReport.getEmpInTime());
							LocalTime empOut = LocalTime.parse(dailyReport.getEmpOutTime());
	
							Long shiftMinutes = shiftIn.until(shiftOut, ChronoUnit.MINUTES);
	
							Long workHours = empIn.until(empOut, ChronoUnit.HOURS);
							Long workMinutes = empIn.until(empOut, ChronoUnit.MINUTES);
							
							workMinutes = workMinutes % 60;
							
							if(workHours<0) {
								workHours = 24 + workHours;
								workMinutes = 60 + workMinutes;
							}
							
	
							dailyReport.setWorkTime(String.valueOf(workHours) + ":" + String.valueOf(workMinutes % 60));
	
							Long overTime = workMinutes - shiftMinutes;
	
							Long lateGoing = shiftOut.until(empOut, ChronoUnit.MINUTES);
							Long earlyGoing = empOut.until(shiftOut, ChronoUnit.MINUTES);
	
							
							if (lateGoing > 0)
								dailyReport.setLateGoing(lateGoing);
	
							if (earlyGoing > 0) {
								dailyReport.setEarlyGoing(earlyGoing);
							} else {
								dailyReport.setEarlyGoing(null);
							}
	
							if (overTime > 0)
								dailyReport.setOverTime(overTime);
							
							reportMap.put(key, dailyReport);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		
			Set<String> keySet =  reportMap.keySet();
			for (String string : keySet) {
				dailyAttList.add(reportMap.get(string));
			}
			dailyAttendanceRepository.saveAll(dailyAttList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dailyReportList;
	}


	@Override
	public PaginationDto<DailyReport> searchByField(Long id, String sDate, String eDate, String employeeId,String employeeName,String department, String status,String shift, int pageno,String sortField, String sortDir) {

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
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		
		
		
		Page<DailyReport> page = getDailyAttendancePage(id, employeeId, employeeName, department,
				pageno, sortField, sortDir, startDate, endDate, status,shift);
		List<DailyReport> employeeShiftList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
		PaginationDto<DailyReport> dtoList = new PaginationDto<DailyReport>(employeeShiftList,
				page.getTotalPages(), page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(),
				page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}
	
	private Page<DailyReport> getDailyAttendancePage(Long id, String employeeId, String employeeName,
			String department,  int pageno, String sortField, String sortDir, Date startDate,
			Date endDate, String status,String shift) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);

		Specification<DailyReport> idSpec = generalSpecificationDailyAttendance.longSpecification(id, ApplicationConstants.ID);
		Specification<DailyReport> dateSpec = generalSpecificationDailyAttendance.dateSpecification(startDate, endDate, ApplicationConstants.DATE);
		Specification<DailyReport> empIdSpec = generalSpecificationDailyAttendance.stringSpecification(employeeId, DailyAttendanceConstants.EMPLOYEE_ID);
		Specification<DailyReport> empNameSpec = generalSpecificationDailyAttendance.stringSpecification(employeeName, DailyAttendanceConstants.EMPLOYEE_NAME);
		Specification<DailyReport> deptSpec = generalSpecificationDailyAttendance.stringSpecification(department, DailyAttendanceConstants.DEPARTMENT);
		Specification<DailyReport> statusSpec = generalSpecificationDailyAttendance.stringSpecification(status, DailyAttendanceConstants.ATTENDANCE_STATUS);
		Specification<DailyReport> shiftSpec = generalSpecificationDailyAttendance.stringSpecification(shift, DailyAttendanceConstants.SHIFT);
		Page<DailyReport> page = dailyAttendanceRepository.findAll(statusSpec.and(idSpec).and(dateSpec).and(empIdSpec)
				.and(empNameSpec).and(deptSpec).and(shiftSpec), pageable);
		return page;
	}

	@Override
	public List<DailyReport> getAllDailyReport() {
		 return (List<DailyReport>) dailyAttendanceRepository.findAll();
	}
}