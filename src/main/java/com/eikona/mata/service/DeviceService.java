package com.eikona.mata.service;


import java.security.Principal;
import java.util.List;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Device;


public interface DeviceService {
	/**
	 * Returns all device List, which are isDeleted false.
	 * @param
	 */
	List<Device> getAll();
	/**
	 * This function saves the device in database according to the respective object.  
	 * @param 
	 */
    void save(Device device,Principal principal);
    /**
	 * This function retrieves the device from database according to the respective id.  
	 * @param
	 */
    Device getById(long id);
    /**
	 * This function deletes the device from database according to the respective id.  
	 * @param
	 */
    void deleteById(long id);
    
	
	PaginationDto<Device> searchByField(String name, String ipAddress, String area,int pageno, String sortField, String sortDir, String orgName);
	
	void employeeSyncFromMataToDevice(long id, String orgName);
    
    String generateTransactionByDate(long id, String sDate, String eDate);
    
	
	
}
