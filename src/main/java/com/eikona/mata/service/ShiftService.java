package com.eikona.mata.service;


import java.util.List;

import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Shift;



public interface ShiftService 
{

	List<Shift> getAll();

	Shift save(Shift shift);

	Shift getById(long id);

	void deleteById(long id);


	PaginationDto<Shift> searchByField(Long id, String name, int pageno, String sortField, String sortDir);
}
