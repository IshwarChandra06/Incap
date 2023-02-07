package com.eikona.mata.service.impl;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.mata.constants.ApplicationConstants;
import com.eikona.mata.constants.NumberConstants;
import com.eikona.mata.constants.ShiftConstants;
import com.eikona.mata.dto.PaginationDto;
import com.eikona.mata.entity.Shift;
import com.eikona.mata.repository.ShiftRepository;
import com.eikona.mata.service.ShiftService;
import com.eikona.mata.util.GeneralSpecificationUtil;

@Service
public class ShiftServiceImpl implements ShiftService {

	@Autowired
	private ShiftRepository shiftRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Shift> generalSpecification;

	@Override
	public List<Shift> getAll() {
		return shiftRepository.findAllByIsDeletedFalse();
	}

	@Override
	public Shift save(Shift shift) {
		shift.setDeleted(false);
		return this.shiftRepository.save(shift);
	}

	@Override
	public Shift getById(long id) {
		Optional<Shift> optional = shiftRepository.findById(id);
		Shift shift = null;
		if (optional.isPresent()) {
			shift = optional.get();
		} else {
			throw new RuntimeException(ShiftConstants.SHIFT_NOT_FOUND + id);
		}
		return shift;
	}

	@Override
	public void deleteById(long id) {
		Optional<Shift> optional = shiftRepository.findById(id);
		Shift shift = null;
		if (optional.isPresent()) {
			shift = optional.get();
			shift.setDeleted(true);
		} else {
			throw new RuntimeException(ShiftConstants.SHIFT_NOT_FOUND + id);
		}
		this.shiftRepository.save(shift);
	}
	

	

	@Override
	public PaginationDto<Shift> searchByField(Long id, String name, int pageno, String sortField, String sortDir) {

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField =  ApplicationConstants.ID;
		}
		Page<Shift> page = getShiftPage(id, name, pageno, sortField, sortDir);
		List<Shift> accessLevelList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Shift> dtoList = new PaginationDto<Shift>(accessLevelList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Shift> getShiftPage(Long id, String name, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Shift> isdeleted = generalSpecification.isDeletedSpecification();
		Specification<Shift> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<Shift> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		
		Page<Shift> page = shiftRepository.findAll(isdeleted.and(idSpc).and(nameSpc), pageable);
		return page;
	}
}



