package com.eikona.mata.listener;

import java.text.ParseException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.DeviceListenerConstants;
import com.eikona.mata.service.impl.corsight.CorsightListenerServiceImpl;
import com.eikona.mata.service.impl.hfsecurity.HFSecurityListenerServiceImpl;

@RestController
public class CommonDeviceListener {
	
	@Autowired
	private CorsightListenerServiceImpl corsightListenerServiceImpl;
	
	@Autowired
	private HFSecurityListenerServiceImpl hfSecurityService;
	
	//HFSecurity Heartbeat
		@PostMapping(path ="/hfsecurity/heartbeat")
		public  ResponseEntity<String> hfSecurityHeartReportInfo(@RequestParam("ip") String ipAddress,
				@RequestParam("deviceKey") String deviceKey, @RequestParam("time") String time,
				@RequestParam("version") String version, 
				@RequestParam("faceCount") String faceCount, @RequestParam("personCount") String personCount) throws ParseException {
	        
			hfSecurityService.saveDeviceInfoFromResponse(ipAddress,deviceKey,time,version,faceCount,personCount);
			
			System.out.println("Heartbeat received!!");
			JSONObject response = new JSONObject();

			response.put(ApplicationConstants.STATUS, ApplicationConstants.SUCCESS);

			return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
	    }
		
		//HFSecurity Eventlog
		@PostMapping(path ="/hfsecurity/eventlog")
		public String hfSecurityEventLogInfo(@RequestParam("deviceKey") String deviceKey, @RequestParam("time") String time, @RequestParam("type") String type, 
				@RequestParam("searchScore") String searchScore,@RequestParam("livenessScore") String livenessScore,@RequestParam("personId") String personId,
				@RequestParam("mask") String mask,@RequestParam("imgBase64") String imgBase64) throws ParseException {
			
			hfSecurityService.saveTransactionInfoFromResponse(deviceKey,time,type,searchScore,livenessScore,mask,imgBase64,personId);
			
			System.out.println("Event log received!!");
			JSONObject response = new JSONObject();
			response.put("result", 1);
			response.put("success", true);

			return response.toString();
			
	}
	
	@PostMapping(path = DeviceListenerConstants.CORSIGHT_LISTENER_API)
	public ResponseEntity<String> corsightHeartReportInfo(@RequestBody JSONObject request) throws ParseException {

		corsightListenerServiceImpl.corsightHeartReportInfo(request);

		JSONObject response = new JSONObject();

		response.put(ApplicationConstants.STATUS, ApplicationConstants.SUCCESS);

		return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
	}
	
}
