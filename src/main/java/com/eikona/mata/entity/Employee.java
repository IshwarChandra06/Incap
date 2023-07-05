package com.eikona.mata.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity(name = "employee")
public class Employee  extends Auditable<String> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;
	
	@Column(name = "emp_id")
	private String empId;
	
	@Column(name = "unique_id")
	private String uniqueId;
	
	@ManyToOne
	@JoinColumn(name="department_id")
	private Department department;
	
	@ManyToOne
	@JoinColumn(name="designation_id")
	private Designation designation;
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	
	@Column
	private String syncDeviceKey;
	
	@Column
	private String poi;
	
	@ManyToOne
	@JoinColumn
	private Shift shift;
	
	@Column
	private String employeeType;
	
	@ManyToMany
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinTable(name = "employee_area",
    joinColumns = @JoinColumn(name = "employee_id"),
    inverseJoinColumns = @JoinColumn(name = "area_id"),
    indexes = {
	        @Index(name = "idx_area_id", columnList = "area_id"),
	        @Index(name = "idx_employee_id", columnList = "employee_id")
	}
	    )
	private List<Area> area;
	
	@Column(name = "crop_image")
	private byte[] cropImage;
	
	@Column(name = "is_sync")
	private boolean isSync;
	
	@Column(name = "is_face_sync")
	private boolean isFaceSync;
	
	@Column
	private boolean isDeleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getEmployeeType() {
		return employeeType;
	}

	public void setEmployeeType(String employeeType) {
		this.employeeType = employeeType;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Shift getShift() {
		return shift;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getPoi() {
		return poi;
	}

	public void setPoi(String poi) {
		this.poi = poi;
	}

	public List<Area> getArea() {
		return area;
	}

	public void setArea(List<Area> area) {
		this.area = area;
	}

	public byte[] getCropImage() {
		return cropImage;
	}

	public void setCropImage(byte[] cropImage) {
		this.cropImage = cropImage;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public boolean isFaceSync() {
		return isFaceSync;
	}

	public void setFaceSync(boolean isFaceSync) {
		this.isFaceSync = isFaceSync;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getSyncDeviceKey() {
		return syncDeviceKey;
	}

	public void setSyncDeviceKey(String syncDeviceKey) {
		this.syncDeviceKey = syncDeviceKey;
	}

	public Designation getDesignation() {
		return designation;
	}

	public void setDesignation(Designation designation) {
		this.designation = designation;
	}

}
