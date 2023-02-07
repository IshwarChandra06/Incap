package com.eikona.mata.sync;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.CorsightDeviceConstants;
import com.eikona.mata.constants.MessageConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.entity.Employee;
import com.eikona.mata.repository.EmployeeRepository;
import com.eikona.mata.util.RequestExecutionUtil;

@Service
public class EmployeeSync {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private RequestExecutionUtil requestExecutionUtil;
	
	@Value("${corsight.host.url}")
    private String corsightHost;
	
	@Value("${corsight.poi.port}")
    private String poiPort;

	public String syncEmployee() {
		try {
			String afterId = ApplicationConstants.DELIMITER_EMPTY;
			getEmployeeList(afterId);
			return MessageConstants.SYNC_SUCCESSFULLY;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageConstants.SYNC_FAILED;
		}
	}

	public void getEmployeeList(String afterId) {
		try {

			String poiUrl = ApplicationConstants.HTTPS_COLON_DOUBLE_SLASH + corsightHost
					+ ApplicationConstants.DELIMITER_COLON + poiPort;
			String addPoiUrl = null;
			if (afterId.isEmpty())
				addPoiUrl = CorsightDeviceConstants.POI_API_SYNC;
			else
				addPoiUrl = CorsightDeviceConstants.POI_API_SYNC_AFTER_ID;

			String responeData = requestExecutionUtil.executeHttpsGetRequest(poiUrl, addPoiUrl);

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject jsonResponseData = (JSONObject) jsonResponse.get(CorsightDeviceConstants.DATA);
			JSONArray jsonArray = (JSONArray) jsonResponseData.get(CorsightDeviceConstants.POIS);
			List<Employee> employeeList = new ArrayList<>();

			setEmployeeDetails(jsonArray, employeeList);

			employeeRepository.saveAll(employeeList);

			if (!employeeList.isEmpty()) {
				afterId = employeeList.get(employeeList.size() - 1).getPoi();

				getEmployeeList(afterId);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setEmployeeDetails(JSONArray jsonArray, List<Employee> employeeList) {
		for (int i = NumberConstants.ZERO; i < jsonArray.size(); i++) {
			JSONObject jsonData = (JSONObject) jsonArray.get(i);

			Employee employee = employeeRepository.findByEmpIdAndIsDeletedFalse((String) jsonData.get(CorsightDeviceConstants.DISPLAY_NAME));
			if (null != employee && null==employee.getPoi()) {
				employee.setPoi((String) jsonData.get(CorsightDeviceConstants.POI_ID));
				employeeList.add(employee);
			}
		}
	}

}
