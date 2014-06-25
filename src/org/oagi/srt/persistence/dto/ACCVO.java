package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Jaehun Lee
 */
public class ACCVO extends SRTObject {

	private int ACCID;
	private String ACCGUID;
	private String ObjectClassTerm;
	private String DEN;
	private String Definition;
	private int BasedACCID;
	private String ObjectClassQualifier;
	private int OAGISComponentType;
	private int CreatedByUserId;
	private int LastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private int State;
	
	public int getACCID() {
		return ACCID;
	}
	
	public void setACCID(int aCCID) {
		ACCID = aCCID;
	}
	
	public String getACCGUID() {
		return ACCGUID;
	}
	
	public void setACCGUID(String aCCGUID) {
		ACCGUID = aCCGUID;
	}
	
	public String getObjectClassTerm() {
		return ObjectClassTerm;
	}
	
	public void setObjectClassTerm(String objectClassTerm) {
		ObjectClassTerm = objectClassTerm;
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
	
	public int getBasedACCID() {
		return BasedACCID;
	}
	
	public void setBasedACCID(int basedACCID) {
		BasedACCID = basedACCID;
	}
	
	public String getObjectClassQualifier() {
		return ObjectClassQualifier;
	}
	
	public void setObjectClassQualifier(String objectClassQualifier) {
		ObjectClassQualifier = objectClassQualifier;
	}
	
	public int getOAGISComponentType() {
		return OAGISComponentType;
	}
	
	public void setOAGISComponentType(int oAGISComponentType) {
		OAGISComponentType = oAGISComponentType;
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
	
	public void setStateD(int state) {
		State = state;
	}
	
}
