package com.eikona.mata.service.impl;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.eikona.mata.constants.CorsightDeviceConstants;
import com.eikona.mata.constants.DeviceConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Device;
import com.eikona.mata.repository.DeviceRepository;
import com.eikona.mata.service.DeviceService;
import com.eikona.mata.util.GeneralSpecificationUtil;
import com.eikona.mata.util.RequestExecutionUtil;

@Service
public class DeviceServiceImpl implements DeviceService {

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private RequestExecutionUtil requestExecutionUtil;


	@Autowired
	private GeneralSpecificationUtil<Device> generalSpecification;


	@Autowired
	private DeviceService deviceService;


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

			this.deviceRepository.save(device);
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
		} else {
			throw new RuntimeException(DeviceConstants.DEVICE_NOT_FOUND + id);
		}
		this.deviceRepository.save(device);
	}



	@Override
	public PaginationDto<Device> searchByField(int pageno, String sortField, String sortDir) {

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Device> page = getDevicePage(pageno, sortField, sortDir);
		
		List<Device> deviceList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC
				: ApplicationConstants.ASC;
		PaginationDto<Device> dtoList = new PaginationDto<Device>(deviceList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(),
				page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Device> getDevicePage(int pageno,String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);

		Specification<Device> isdeleted = generalSpecification.isDeletedSpecification();

		Page<Device> page = deviceRepository
				.findAll(isdeleted, pageable);
		return page;
	}


	@Override
	public String generateTransactionByDate(long id, String sDate, String eDate) {

		Device device = deviceService.getById(id);

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
