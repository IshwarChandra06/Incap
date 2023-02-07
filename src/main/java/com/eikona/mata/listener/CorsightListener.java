package com.eikona.mata.listener;

import java.text.ParseException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.DeviceListenerConstants;
import com.eikona.mata.service.impl.CorsightListenerServiceImpl;

@RestController
public class CorsightListener {
	
	@Autowired
	private CorsightListenerServiceImpl corsightListenerServiceImpl;
	
	
	
	@PostMapping(path = DeviceListenerConstants.CORSIGHT_LISTENER_API)
	public ResponseEntity<String> corsightHeartReportInfo(@RequestBody JSONObject request) throws ParseException {

		corsightListenerServiceImpl.corsightHeartReportInfo(request);

		JSONObject response = new JSONObject();

		response.put(ApplicationConstants.STATUS, ApplicationConstants.SUCCESS);

		return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
	}
	
}
