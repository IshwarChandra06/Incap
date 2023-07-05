package com.eikona.mata.service;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Action;
import com.eikona.mata.entity.Device;
import com.eikona.mata.entity.Employee;

public interface ActionService {

	void save(Action action);

	Action getById(long id);

	void employeeAction(Employee employee, String type, String source);

	void employeeDeviceAction(Device device,Employee employee, String type, String source);

	PaginationDto<Action> searchByField(String sDate, String eDate, String employeeId, String name,
			int pageno, String sortField, String sortDir, String orgName);

}
