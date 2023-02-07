package com.eikona.mata.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.GenericGenerator;

@Entity(name = "device")
public class Device  extends Auditable<String>  implements Serializable {

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
	//@NotBlank(message = "Please provide a valid name")
	private String name;
	
	@Column(name = "camera_id")
	private String cameraId;
	
	@Column(name = "access_type")
	private String accessType;
	

	@Column(unique = true,name = "serial_no")
	@NotBlank(message = "Please provide a unique serial no")
	private String serialNo;

	
	@Column(name = "ip_address")
	private String ipAddress;
	
	@Column
	@NotBlank(message = "Please select a  device type")
	private String model;

	@Column(name = "is_deleted")
	private boolean isDeleted;
	
	@ElementCollection
	@CollectionTable(name = "watchlist")
	private List<String> watchlist;

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

	public String getCameraId() {
		return cameraId;
	}

	public void setCameraId(String cameraId) {
		this.cameraId = cameraId;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public List<String> getWatchlist() {
		return watchlist;
	}

	public void setWatchlist(List<String> watchlist) {
		this.watchlist = watchlist;
	}

	
}
