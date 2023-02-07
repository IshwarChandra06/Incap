package com.eikona.mata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Device;
import com.eikona.mata.service.DeviceService;
import com.eikona.mata.sync.DeviceSync;


@Controller
public class DeviceController {
	
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private DeviceSync cameraSync;
	
	@GetMapping("/sync-device")
	@PreAuthorize("hasAuthority('device_view')")
	public String syncDevice(Model model)
	{
		String message = null;
		try {
			message = cameraSync.syncDevice();
			model.addAttribute("message",message);
		} 
		catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message",message);
		}
		return "device/device_list";
	}
	
	@GetMapping("/device")
	@PreAuthorize("hasAuthority('device_view')")
	public String deviceList(Model model){
		return "device/device_list";
	}

	@RequestMapping(value = "/api/search/device", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('device_view')")
	public @ResponseBody PaginationDto<Device> search(int pageno, String sortField, String sortDir) {
		
		PaginationDto<Device> dtoList = deviceService.searchByField(pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value = "/get-transaction-by-date", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('transaction_by_date')")
	public @ResponseBody String getTransacionByDateRange(long id, String sDate, String eDate) {
		String message = deviceService.generateTransactionByDate(id, sDate, eDate);
		
		return message;
	}
	
	@GetMapping("/get-transaction-by-date/{id}")
	@PreAuthorize("hasAuthority('transaction_by_date')")
	public String getTransactionRecordByDateRange(@PathVariable(value = "id") long id, Model model) {

		model.addAttribute("id", id);
		return "transaction/syncTransaction";
		
	}
}
