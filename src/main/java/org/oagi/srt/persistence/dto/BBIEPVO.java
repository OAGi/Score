package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

import java.sql.Timestamp;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class BBIEPVO extends SRTObject {
	private int BBIEPID;
	private String BBIEPGUID;
	private int BasedBCCPID;
	private int CreatedByUserID;
	private int LastUpdatedbyUserID;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	
	private String definition;
	private String remark;
	private String businessTerm;
	
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getBusinessTerm() {
		return businessTerm;
	}
	public void setBusinessTerm(String businessTerm) {
		this.businessTerm = businessTerm;
	}
	public int getBBIEPID() {
		return BBIEPID;
	}
	public void setBBIEPID(int bBIEPID) {
		BBIEPID = bBIEPID;
	}
	public String getBBIEPGUID() {
		return BBIEPGUID;
	}
	public void setBBIEPGUID(String bBIEPGUID) {
		BBIEPGUID = bBIEPGUID;
	}
	public int getBasedBCCPID() {
		return BasedBCCPID;
	}
	public void setBasedBCCPID(int basedBCCPID) {
		BasedBCCPID = basedBCCPID;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public int getCreatedByUserID() {
		return CreatedByUserID;
	}
	public void setCreatedByUserID(int createdByUserID) {
		CreatedByUserID = createdByUserID;
	}
	public int getLastUpdatedbyUserID() {
		return LastUpdatedbyUserID;
	}
	public void setLastUpdatedbyUserID(int lastUpdatedbyUserID) {
		LastUpdatedbyUserID = lastUpdatedbyUserID;
	}
	public Timestamp getCreationTimestamp() {
		return CreationTimestamp;
	}
	public void setCreationTimestamp(Timestamp creationTimestamp) {
		CreationTimestamp = creationTimestamp;
	}
	public Timestamp getLastUpdateTimestamp() {
		return LastUpdateTimestamp;
	}
	public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {
		LastUpdateTimestamp = lastUpdateTimestamp;
	}
}
