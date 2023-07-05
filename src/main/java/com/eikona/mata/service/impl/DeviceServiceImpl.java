package com.eikona.mata.service.impl;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.AreaConstants;
import com.eikona.mata.constants.CorsightDeviceConstants;
import com.eikona.mata.constants.DeviceConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Area;
import com.eikona.mata.entity.Device;
import com.eikona.mata.entity.Employee;
import com.eikona.mata.repository.DeviceRepository;
import com.eikona.mata.repository.EmployeeRepository;
import com.eikona.mata.service.ActionService;
import com.eikona.mata.service.DeviceService;
import com.eikona.mata.service.impl.corsight.CorsightDeviceServiceImpl;
import com.eikona.mata.util.GeneralSpecificationUtil;
import com.eikona.mata.util.RequestExecutionUtil;

@Service
public class DeviceServiceImpl implements DeviceService {


	@Autowired
	private RequestExecutionUtil requestExecutionUtil;


	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ActionService actionService;

	@Autowired
	private GeneralSpecificationUtil<Device> generalSpecification;


	@Autowired
	private CorsightDeviceServiceImpl corsightSyncServiceImpl;
	
	@Value("${corsight.host.url}")
	private String host;
	
	@Value("${corsight.controller.port}")
	private String portController;



	@Override
	public List<Device> getAll() {
		return deviceRepository.findAllByIsDeletedFalse();

	}

	@Override
	public void save(Device device, Principal principal) {

		device.setDeleted(false);
		device.setSync(false);
		if (null == device.getId()) {

			this.deviceRepository.save(device);
		} else {
			Device deviceObj = deviceRepository.findById(device.getId()).get();
			device.setCreatedBy(deviceObj.getCreatedBy());
			device.setCreatedDate(deviceObj.getCreatedDate());

			this.deviceRepository.save(device);
//			}
		}
	}

	@Override
	public Device getById(long id) {
		Device optional = deviceRepository.findByIdAndIsDeletedFalse(id);
		Device device = null;
		if (null != optional) {
			device = optional;
		} else {
			throw new RuntimeException(DeviceConstants.DEVICE_NOT_FOUND + id);
		}
		return device;

	}

	@Override
	public void deleteById(long id) {
		Optional<Device> optional = deviceRepository.findById(id);
		Device device = null;
		if (optional.isPresent()) {
			device = optional.get();
			device.setDeleted(true);
			device.setSync(false);
		} else {
			throw new RuntimeException(DeviceConstants.DEVICE_NOT_FOUND + id);
		}
		this.deviceRepository.save(device);
	}


	@Override
	public PaginationDto<Device> searchByField(String name, String ipAddress,  String area, int pageno, String sortField, String sortDir,String organization) {

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Device> page = getDevicePage( name, ipAddress,area, pageno, sortField, sortDir,organization);
		
		List<Device> deviceList = page.getContent();
//		if(!status.isEmpty()) {
//			List<Device> newDeviceList = new ArrayList<>();
//			for(Device device :deviceList) {
//				try {
//					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String dateStr = format.format(new Date());
//					Date date = format.parse(dateStr);
//					Date lastonline = device.getLastOnline();
//					
//					long mileseconds = date.getTime() - lastonline.getTime();
//					
//					if("active".equalsIgnoreCase(status) && mileseconds<=120000) {
//						newDeviceList.add(device);
//					}else if("inactive".equalsIgnoreCase(status) && mileseconds>120000) {
//						newDeviceList.add(device);
//					}
//					
//				}catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			
//			deviceList = newDeviceList;
//		}

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC
				: ApplicationConstants.ASC;
		PaginationDto<Device> dtoList = new PaginationDto<Device>(deviceList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(),
				page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Device> getDevicePage(String name, String ipAddress, String area, int pageno, String sortField, String sortDir, String organization) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);

		Specification<Device> isdeleted = generalSpecification.isDeletedSpecification();
		Specification<Device> nameSpc = generalSpecification.stringSpecification(name, DeviceConstants.NAME);
		Specification<Device> areaSpc = generalSpecification.foreignKeyStringSpecification(area,"area", DeviceConstants.NAME);
		Specification<Device> ipSpc = generalSpecification.stringSpecification(ipAddress, DeviceConstants.IP_ADDRESS);
		Specification<Device> orgSpc = generalSpecification.foreignKeyStringSpecification(organization, AreaConstants.ORGANIZATION,
				DeviceConstants.NAME);

		Page<Device> page = deviceRepository.findAll(isdeleted.and(areaSpc).and(nameSpc).and(orgSpc).and(ipSpc), pageable);
		return page;
	}

	@Override
	public void employeeSyncFromMataToDevice(long id, String orgName) {

		Device device = getById(id);
		List<Area> areaList = new ArrayList<>();
		areaList.add(device.getArea());
		long empCount = employeeRepository.countEmployeeAndIsDeletedFalseCustom(orgName,device.getArea().getId());
		int limit = NumberConstants.THOUSAND;
		int totalPage = (int) (empCount / limit);

		for (int i = NumberConstants.ZERO; i <= totalPage; i++) {
			Pageable paging = PageRequest.of(i, limit, Sort.by(ApplicationConstants.ID).ascending());
			List<Employee> employeeList = employeeRepository
					.findByIsDeletedFalseCustom(orgName,device.getArea().getId(), paging);
			for (Employee employee : employeeList) {
				actionService.employeeDeviceAction(device, employee, ApplicationConstants.SYNC,
						ApplicationConstants.ACCESS_TYPE_APP);
			}
		}

	
		
	}


	@Override
	public String generateTransactionByDate(long id, String sDate, String eDate) {

		Device device = deviceRepository.findById(id).get();

		SimpleDateFormat inputformatStr = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SLASH);
		SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);

		Date startDate = null;
		Date endDate = null;
		String message = ApplicationConstants.DELIMITER_EMPTY;
		try {
			startDate = inputformatStr.parse(sDate);
			endDate = inputformatStr.parse(eDate);

			String sDateStr = format.format(startDate);
			String eDateStr = format.format(endDate);

			startDate = format.parse(sDateStr);
			endDate = format.parse(eDateStr);

			String deviceInfo = corsightServerBasicInfo();
			if (null != deviceInfo) {
				message = corsightSyncServiceImpl.getTransactionByDate(device, startDate, endDate);
			}

		} catch (ParseException e) {
			System.out.println(e);
		}

		return message;
	}
	
	private String corsightServerBasicInfo() {
		String msg = null;
		try {
			String httpsUrl = ApplicationConstants.HTTPS_COLON_DOUBLE_SLASH + host + ApplicationConstants.DELIMITER_COLON+ portController;
			String getUrl =  CorsightDeviceConstants.SERVER_STATUS_API;

			String responeData = requestExecutionUtil.executeHttpsGetRequest(httpsUrl, getUrl);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject jsonMetaData = (JSONObject) jsonResponse.get(CorsightDeviceConstants.METADATA);
			msg = (String) jsonMetaData.get(CorsightDeviceConstants.MSG);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}
}
