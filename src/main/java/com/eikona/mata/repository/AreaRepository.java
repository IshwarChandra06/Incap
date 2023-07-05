package com.eikona.mata.repository;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.eikona.mata.entity.Area;
import com.eikona.mata.entity.Organization;

@Repository
public interface AreaRepository extends DataTablesRepository<Area, Long> {
	
	List<Area> findAllByIsDeletedFalse();
	
	Area findByIdAndIsDeletedFalse(long id);
	

	Area findByNameAndIsDeletedFalse(String area);

	List<Area> findByOrganizationAndIsDeletedFalse(Organization organization);




}
