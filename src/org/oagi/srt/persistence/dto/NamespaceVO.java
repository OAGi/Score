package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Jaehun Lee
 */
public class NamespaceVO extends SRTObject {

	private int NamespaceID;
	private String Uri;
	private String Prefix;
	private String Prescription;
	private int OwneruserID;
	private int CreatedByUserId;
	private int LastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	
	public int getNamespaceID() {
		return NamespaceID;
	}
	
	public void setNamespaceID(int namespaceID) {
		NamespaceID = namespaceID;
	}
	
	public String getUri() {
		return Uri;
	}
	
	public void setUri(String uri) {
		Uri = uri;
	}
	
	public String getPrefix() {
		return Prefix;
	}
	
	public void setPrefix(String prefix) {
		Prefix = prefix;
	}
	
	public String getPrescription() {
		return Prescription;
	}
	
	public void setPrescription(String prescription) {
		Prescription = prescription;
	}
	
	public int getOwneruserID() {
		return OwneruserID;
	}
	
	public void setOwneruserID(int owneruserID) {
		OwneruserID = owneruserID;
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
}
