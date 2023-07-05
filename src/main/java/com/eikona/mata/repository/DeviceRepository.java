package com.eikona.mata.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.mata.entity.Area;
import com.eikona.mata.entity.Device;


@Repository
public interface DeviceRepository extends DataTablesRepository<Device, Long> {

	List<Device> findAllByIsDeletedFalse();
	
	Device findByNameAndIsDeletedFalse(String device);

	Device findBySerialNoAndIsDeletedFalse(String serialno);

	Device findByCameraIdAndIsDeletedFalse(String camera_id);

	Device findByIdAndIsDeletedFalse(long id);
	
	@Query("select de from com.eikona.mata.entity.Device as de where de.area in :area and isDeleted=false")
	List<Device> findByAreaAndIsDeletedFalseCustom(List<Area> area);

}
