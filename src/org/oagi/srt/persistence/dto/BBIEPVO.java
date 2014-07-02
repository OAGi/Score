package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class BBIEPVO extends SRTObject {
	private int BBIEPID;
	private String BBIEPGUID;
	private int BasedBCCPID;
	private String Definition;
	private int CreatedByUserID;
	private int LastUpdatedbyUserID;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	
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
