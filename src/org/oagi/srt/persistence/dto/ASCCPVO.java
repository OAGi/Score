package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Jaehun Lee
 */
public class ASCCPVO extends SRTObject {

	private int ASCCPID;
	private String ASCCPGUID;
	private String PropertyTerm;
	private String DEN;
	private String Definition;
	private int RoleOfACCID;
	private int CreatedByUserId;
	private int LastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private int State;
	private String Module;
	private boolean ReusableIndicator;
	
	public int getASCCPID() {
		return ASCCPID;
	}
	
	public void setASCCPID(int aSCCPID) {
		ASCCPID = aSCCPID;
	}
	
	public String getASCCPGUID() {
		return ASCCPGUID;
	}
	
	public void setASCCPGUID(String aSCCPGUID) {
		ASCCPGUID = aSCCPGUID;
	}
	
	public String getPropertyTerm() {
		return PropertyTerm;
	}
	
	public void setPropertyTerm(String propertyTerm) {
		PropertyTerm = propertyTerm;
	}
	
	public String getDEN() {
		return DEN;
	}
	
	public void setDEN(String dEN) {
		DEN = dEN;
	}
	
	public String getDefinition() {
		return Definition;
	}
	
	public void setDefinition(String definition) {
		Definition = definition;
	}
	
	public int getRoleOfACCID() {
		return RoleOfACCID;
	}
	
	public void setRoleOfACCID(int roleOfACCID) {
		RoleOfACCID = roleOfACCID;
	}

	public int getCreatedByUserId() {
		return CreatedByUserId;
	}
	
	public void setCreatedByUserId(int createdByUserId) {
		CreatedByUserId = createdByUserId;
	}
	
	public int getLastUpdatedByUserId() {
		return LastUpdatedByUserId;
	}
	
	public void setLastUpdatedByUserId(int lastUpdatedByUserId) {
		LastUpdatedByUserId = lastUpdatedByUserId;
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
	
	public int getState() {
		return State;
	}
	
	public void setState(int state) {
		State = state;
	}
	
	public String getModule() {
		return Module;
	}
	
	public void setModule(String module) {
		Module = module;
	}
	
	public boolean getReusableIndicator() {
		return ReusableIndicator;
	}
	
	public void setReusableIndicator(boolean reusableIndicator) {
		ReusableIndicator = reusableIndicator;
	}
	
}
