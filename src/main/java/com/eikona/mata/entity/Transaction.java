package com.eikona.mata.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

@Entity(name="transaction")
public class Transaction implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "id")
	private Long id;
	
	@Column
	private String empId;
	
	@Column
	private String uniqueId;
	
	@Column
	private String name;
	
	@Column
	private String area;
	
	@Column
	private String organization;
	
	@Column
	private String department;
	
	@Column
	private String designation;

	@JoinColumn
	@ManyToOne
	private Shift shift;
	
	@JoinColumn
	@ManyToOne
	private Device device;
	
	@Column
	private Integer logId;
	
	@Column
	private Boolean wearingMask;
	
	@Column
	private String temperature;
	
	@Column
	private String accessType;
	
	@Column
	private Date punchDate;
	
	@Column
	private Date punchTime;
	
	@Column
	private String punchDateStr;
	
	@Column
	private String punchTimeStr;
	
	@Column
	private String deviceType;
	
	@Column
	private String watchlistName;
	
	@Column
	private String watchlistId;
	
	@Column
	private String eventId;
	
	@Column
	private String appearanceId;
	
	@Column
	private Double poiConfidence;
	
	@Column
	private String poiId;
	
	@Column
	private String maskStatus;
	
	@Column
	private Double maskedScore;
	
	@Column
	private boolean isSync;
	
	@Column
	private boolean isFailed;
	
	@Column
	private String age;
	
	@Column
	private String gender;
	
	@Column
	private String searchScore;
	
	@Column
	private String livenessScore;
	
	@Column
	private String permissionStatus;
	
	@Column
	private Long totalCount;
	
	@Column
	private byte[] cropImageByte;
	
	@Column
	private String cropimagePath;
	
	public byte[] getCropImageByte() {
		return cropImageByte;
	}

	public void setCropImageByte(byte[] cropImageByte) {
		this.cropImageByte = cropImageByte;
	}

	public String getCropimagePath() {
		return cropimagePath;
	}

	public void setCropimagePath(String cropimagePath) {
		this.cropimagePath = cropimagePath;
	}

	

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}



	

	public void setPoiConfidence(Double poiConfidence) {
		this.poiConfidence = poiConfidence;
	}

	public Double getMaskedScore() {
		return maskedScore;
	}

	public void setMaskedScore(Double maskedScore) {
		this.maskedScore = maskedScore;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Integer getLogId() {
		return logId;
	}

	public void setLogId(Integer logId) {
		this.logId = logId;
	}

	public Boolean getWearingMask() {
		return wearingMask;
	}

	public void setWearingMask(Boolean wearingMask) {
		this.wearingMask = wearingMask;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public Date getPunchDate() {
		return punchDate;
	}

	public void setPunchDate(Date punchDate) {
		this.punchDate = punchDate;
	}

	public Date getPunchTime() {
		return punchTime;
	}

	public void setPunchTime(Date punchTime) {
		this.punchTime = punchTime;
	}

	public String getPunchDateStr() {
		return punchDateStr;
	}

	public void setPunchDateStr(String punchDateStr) {
		this.punchDateStr = punchDateStr;
	}

	public String getPunchTimeStr() {
		return punchTimeStr;
	}

	public void setPunchTimeStr(String punchTimeStr) {
		this.punchTimeStr = punchTimeStr;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getWatchlistName() {
		return watchlistName;
	}

	public void setWatchlistName(String watchlistName) {
		this.watchlistName = watchlistName;
	}

	public String getWatchlistId() {
		return watchlistId;
	}

	public void setWatchlistId(String watchlistId) {
		this.watchlistId = watchlistId;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getAppearanceId() {
		return appearanceId;
	}

	public void setAppearanceId(String appearanceId) {
		this.appearanceId = appearanceId;
	}

	public String getPoiId() {
		return poiId;
	}

	public void setPoiId(String poiId) {
		this.poiId = poiId;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSearchScore() {
		return searchScore;
	}

	public void setSearchScore(String searchScore) {
		this.searchScore = searchScore;
	}

	public String getLivenessScore() {
		return livenessScore;
	}

	public void setLivenessScore(String livenessScore) {
		this.livenessScore = livenessScore;
	}

	public Long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public Double getPoiConfidence() {
		return poiConfidence;
	}

	public String getMaskStatus() {
		return maskStatus;
	}

	public void setMaskStatus(String maskStatus) {
		this.maskStatus = maskStatus;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed(boolean isFailed) {
		this.isFailed = isFailed;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
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

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getPermissionStatus() {
		return permissionStatus;
	}

	public void setPermissionStatus(String permissionStatus) {
		this.permissionStatus = permissionStatus;
	}
	
	
}
