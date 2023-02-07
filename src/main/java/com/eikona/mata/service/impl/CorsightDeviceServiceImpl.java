package com.eikona.mata.service.impl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.CorsightDeviceConstants;
import com.eikona.mata.constants.MessageConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.entity.Device;
import com.eikona.mata.entity.Employee;
import com.eikona.mata.entity.Transaction;
import com.eikona.mata.repository.EmployeeRepository;
import com.eikona.mata.repository.TransactionRepository;
import com.eikona.mata.util.CalendarUtil;
import com.eikona.mata.util.RequestExecutionUtil;
import com.eikona.mata.util.SavingCropImageUtil;

@Service
public class CorsightDeviceServiceImpl{

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private SavingCropImageUtil savingCropImageUtil;
	
	@Autowired
	private CalendarUtil calendarUtil; 
	
	@Autowired
	private RequestExecutionUtil requestExecutionUtil;
	
	@Value("${corsight.host.url}")
    private String corsightHost;
	
	@Value("${corsight.poi.port}")
    private String poiPort;
	
	@Value("${corsight.camera.port}")
    private String cameraPort;
	
	@Value("${corsight.history.port}")
    private String historyPort;

	public String deviceBasicInfo(String cameraId) {
		String msg = null;
		try {
			String deviceUrl = ApplicationConstants.HTTPS_COLON_DOUBLE_SLASH + corsightHost + ApplicationConstants.DELIMITER_COLON + cameraPort;
			String addDeviceUrl = CorsightDeviceConstants.CAMERA_API + cameraId;
 
			String responeData = requestExecutionUtil.executeHttpsGetRequest(deviceUrl, addDeviceUrl);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject jsonMetaData = (JSONObject) jsonResponse.get(CorsightDeviceConstants.METADATA);
			msg = (String) jsonMetaData.get(CorsightDeviceConstants.MSG);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}

	
	
	public String getTransactionByDate(Device device, Date sDate, Date eDate) {
		try {
			String afterId = ApplicationConstants.DELIMITER_EMPTY;
			syncCorsightHistory(afterId,device,sDate,eDate);
			return MessageConstants.TRANSACTION_SUCCESS;
		}catch (Exception e) {
			return MessageConstants.TRANSACTION_FAILED;
		}
		
		
	}
	
	
	private void syncCorsightHistory(String afterId,Device device,Date sDateStr, Date eDateStr) {
		try {
			JSONObject requestObj = getHistorySyncRequestObject(device, sDateStr, eDateStr);

			String historyUrl = ApplicationConstants.HTTPS_COLON_DOUBLE_SLASH + corsightHost + ApplicationConstants.DELIMITER_COLON + historyPort;
			String addHistoryUrl = null;
			if (afterId.isEmpty())
				addHistoryUrl = CorsightDeviceConstants.HISTORY_API_SYNC;
			else
				addHistoryUrl = CorsightDeviceConstants.HISTORY_API_SYNC_AFTER_ID + afterId;

			JSONArray matchArray = getHttpsPOSTArray(requestObj, historyUrl, addHistoryUrl);
			
			List<Transaction> eventList = new ArrayList<>();
			for (int i = NumberConstants.ZERO; i < matchArray.size(); i++) {
				JSONObject currobj = (JSONObject) matchArray.get(i);
				JSONObject appearanceDataObject = (JSONObject) currobj.get(CorsightDeviceConstants.APPEARANCE_DATA);
				
				DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
				DateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
				Timestamp ts = new Timestamp((long) ((double) appearanceDataObject.get(CorsightDeviceConstants.UTC_TIME_STARTED)));
				Date punchDate = new Date(ts.getTime() * NumberConstants.THOUSAND);
				
				JSONObject matchDataObject = (JSONObject) currobj.get(CorsightDeviceConstants.MATCH_DATA);
				String empId=(String) matchDataObject.get(CorsightDeviceConstants.POI_DISPLAY_NAME);
				
				
				Transaction eventReport = transactionRepository.findByEmpIdAndPunchDate(empId,punchDate);
				if (eventReport == null) {
					Transaction event = new Transaction();

					event.setAppearanceId((String) appearanceDataObject.get(CorsightDeviceConstants.APPEARANCE_ID));

					setTransactionDate(dateFormat, timeFormat, punchDate, event);

					JSONObject cameraDataObject = (JSONObject) currobj.get(CorsightDeviceConstants.CAMERA_DATA);
					event.setDeviceId((String) cameraDataObject.get(CorsightDeviceConstants.CAMERA_ID));
					event.setDeviceName((String) cameraDataObject.get(CorsightDeviceConstants.CAMERA_DESCRIPTION));

					setApperanceData(currobj, matchDataObject, event);
					
					eventList.add(event);
					
			}

				}
			
			transactionRepository.saveAll(eventList);
			if (!eventList.isEmpty()) {
				afterId = eventList.get(eventList.size() - 1).getAppearanceId();

				syncCorsightHistory(afterId,device,sDateStr,eDateStr);

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONArray getHttpsPOSTArray(JSONObject requestObj, String historyUrl,
			String addHistoryUrl) throws ParseException, KeyManagementException, NoSuchAlgorithmException, IOException {
		
		String responeData = requestExecutionUtil.executeHttpsPostRequest(requestObj, historyUrl, addHistoryUrl);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
		JSONObject dataObject = (JSONObject) jsonResponse.get(CorsightDeviceConstants.DATA);
		JSONArray jsonArray = (JSONArray) dataObject.get(CorsightDeviceConstants.MATCHES);
		return jsonArray;
	}
	@SuppressWarnings(ApplicationConstants.UNCHECKED)
	private JSONObject getHistorySyncRequestObject(Device device, Date sDate, Date eDate) {
		JSONObject requestObj = new JSONObject();
		try {
			JSONArray cameraArray = new JSONArray();
			JSONArray watchListArray = new JSONArray();

			JSONObject matchedObj = new JSONObject();
			
			Calendar fromCal = calendarUtil.dateToCalendar(sDate);

			Calendar toCal = calendarUtil.dateToCalendar(eDate);

			long from = fromCal.getTimeInMillis() / NumberConstants.THOUSAND;
			long to = toCal.getTimeInMillis() / NumberConstants.THOUSAND;

			cameraArray.add(device.getCameraId());
			
			for(String watchlist:device.getWatchlist()) {
				matchedObj.put(CorsightDeviceConstants.MATCH_OUTCOME, CorsightDeviceConstants.MATCHED);
				matchedObj.put(CorsightDeviceConstants.WATCHLIST_ID, watchlist);

				watchListArray.add(matchedObj);
			}
			requestObj.put(CorsightDeviceConstants.CAMERAS, cameraArray);
			requestObj.put(CorsightDeviceConstants.FROM, from);
			requestObj.put(CorsightDeviceConstants.TILL, to);
			requestObj.put(CorsightDeviceConstants.WATCHLISTS, watchListArray);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return requestObj;
		
	}

	private void setApperanceData(JSONObject currobj, JSONObject matchDataObject, Transaction event) {
		
		
		setEmployeeDetails(matchDataObject, event);

		setTransactionImage(currobj, event);
		
		if (null != matchDataObject.get(CorsightDeviceConstants.POI_ID)) {
			event.setPoiId((String) matchDataObject.get(CorsightDeviceConstants.POI_ID));
		}
		
		event.setPoiConfidence((double) matchDataObject.get(CorsightDeviceConstants.POI_CONFIDENCE));
		
		event.setEventId((String) currobj.get(CorsightDeviceConstants.EVENT_ID));

		JSONObject faceDataObject = (JSONObject) currobj.get(CorsightDeviceConstants.FACE_FEATURE_DATA);

		event.setMaskStatus((String) faceDataObject.get(CorsightDeviceConstants.MASK_OUTCOME));
		event.setGender((String) faceDataObject.get(CorsightDeviceConstants.GENDER_OUTCOME));
		event.setAge((String) faceDataObject.get(CorsightDeviceConstants.AGE_OUTCOME));

		if (CorsightDeviceConstants.NOT_MASKED.equalsIgnoreCase(event.getMaskStatus()))
			event.setWearingMask(true);
		else
			event.setWearingMask(false);
	}

	private void setTransactionImage(JSONObject currobj, Transaction event) {
		JSONObject cropDataObject = (JSONObject) currobj.get(CorsightDeviceConstants.CROP_DATA);

		if (null != cropDataObject) {
			String imagePath = savingCropImageUtil.saveCropImages((String) cropDataObject.get(CorsightDeviceConstants.FACE_CROP_IMG), event);
			event.setCropimagePath(imagePath);
		}
	}

	private void setEmployeeDetails(JSONObject matchDataObject, Transaction event) {
		if (null != matchDataObject.get(CorsightDeviceConstants.POI_DISPLAY_NAME)) {
			Employee employee = employeeRepository.findByEmpIdAndIsDeletedFalse((String) matchDataObject.get(CorsightDeviceConstants.POI_DISPLAY_NAME));

			if (null != employee) {
				event.setEmpId(employee.getEmpId());
				event.setName(employee.getName());
				event.setDepartment(employee.getDepartment());
			}
			else {
				employee= new Employee();
				employee.setEmpId((String) matchDataObject.get(CorsightDeviceConstants.POI_DISPLAY_NAME));
				event.setEmpId((String) matchDataObject.get(CorsightDeviceConstants.POI_DISPLAY_NAME));
			}
//			setWatchlistDetails(employee,matchDataObject, event);
			employeeRepository.save(employee);
		}
	}

//	private void setWatchlistDetails(Employee employee, JSONObject matchDataObject, Transaction transaction) {
//		JSONArray watchArray = (JSONArray) matchDataObject.get(CorsightDeviceConstants.WATCHLISTS);
//		if(!watchArray.isEmpty() && null!=watchArray) {
//			JSONObject watchobj =(JSONObject) watchArray.get(0);
//			transaction.setWatchlistId((String) watchobj.get(CorsightDeviceConstants.WATCHLIST_ID));
//			transaction.setWatchlistName((String) watchobj.get(CorsightDeviceConstants.DISPLAY_NAME));
//			transaction.setArea((String) watchobj.get(CorsightDeviceConstants.DISPLAY_NAME));
//			transaction.setEmployeeType((String) watchobj.get(CorsightDeviceConstants.DISPLAY_NAME));
//			
//			if(null==employee.getEmployeeType()) {
//				employee.setEmployeeType((String) watchobj.get(CorsightDeviceConstants.DISPLAY_NAME));
//				employeeRepository.save(employee);
//			}
//		}
//	}

	private void setTransactionDate(DateFormat dateFormat, DateFormat timeFormat, Date punchDate, Transaction event) {
		String dateString = dateFormat.format(punchDate);
		event.setPunchDateStr(dateString);
		event.setPunchDate(punchDate);
		event.setPunchTimeStr(timeFormat.format(punchDate));
	}

	@SuppressWarnings(ApplicationConstants.UNCHECKED)
	public String getPoiDetailsByPoiId(String poiId) {

		try {
			JSONObject requestObj = new JSONObject();
			JSONArray poiArray = new JSONArray();
			JSONObject poiObj = new JSONObject();

			poiObj.put(CorsightDeviceConstants.POI_ID, poiId);
			poiArray.add(poiObj);
			requestObj.put(CorsightDeviceConstants.POIS,poiArray);

			String httpsUrl = ApplicationConstants.HTTPS_COLON_DOUBLE_SLASH + corsightHost + ApplicationConstants.DELIMITER_COLON + poiPort;
			String postUrl = CorsightDeviceConstants.POI_API_GET;

			String responeData = requestExecutionUtil.executeHttpsPostRequest(requestObj, httpsUrl, postUrl);

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject jsonResponseData = (JSONObject) jsonResponse.get(CorsightDeviceConstants.DATA);
			JSONArray poiResponseArray=(JSONArray) jsonResponseData.get(CorsightDeviceConstants.POIS);
			JSONObject currentObj=(JSONObject) poiResponseArray.get(0);
			String poi_id=(String) currentObj.get(CorsightDeviceConstants.POI_ID);
			List<String> watchlistArray=(List<String>) currentObj.get(CorsightDeviceConstants.POI_WATCHLISTS);
			String msg=null;
			if(null!=poi_id && !(poi_id.isEmpty()))
				msg=ApplicationConstants.SUCCESS;
			return msg;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
		
}
