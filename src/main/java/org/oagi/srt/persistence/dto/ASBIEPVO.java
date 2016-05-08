package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ASBIEPVO extends SRTObject {
	private int ASBIEPID;
	private String ASBIEPGUID;
	private int BasedASCCPID;
	private int RoleOfABIEID;
	private String Definition;
	private int CreatedByUserID;
	private int LastUpdatedByUserID;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	
	private String remark;
	private String businessTerm;
	
	public int getASBIEPID() {
		return ASBIEPID;
	}
	public void setASBIEPID(int aSBIEPID) {
		ASBIEPID = aSBIEPID;
	}
	public String getASBIEPGUID() {
		return ASBIEPGUID;
	}
	public void setASBIEPGUID(String aSBIEPGUID) {
		ASBIEPGUID = aSBIEPGUID;
	}
	public int getBasedASCCPID() {
		return BasedASCCPID;
	}
	public void setBasedASCCPID(int basedASCCPID) {
		BasedASCCPID = basedASCCPID;
	}
	public int getRoleOfABIEID() {
		return RoleOfABIEID;
	}
	public void setRoleOfABIEID(int roleOfABIEID) {
		RoleOfABIEID = roleOfABIEID;
	}
	public String getDefinition() {
		return Definition;
	}
	public void setDefinition(String definition) {
		Definition = definition;
	}
	public int getCreatedByUserID() {
		return CreatedByUserID;
	}
	public void setCreatedByUserID(int createdByUserID) {
		CreatedByUserID = createdByUserID;
	}
	public int getLastUpdatedByUserID() {
		return LastUpdatedByUserID;
	}
	public void setLastUpdatedByUserID(int lastUpdatedByUserID) {
		LastUpdatedByUserID = lastUpdatedByUserID;
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
}
