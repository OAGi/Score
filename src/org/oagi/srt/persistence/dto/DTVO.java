package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Yunsu Lee
 */
public class DTVO extends SRTObject {

	private int DTID;
	private String DTGUID;
	private int DTType;
	private String VersionNumber;
	private int PreviousVersionDTID;
	private int RevisionType;
	private String DataTypeTerm;
	private String Qualifier;
	private String BasedDTID;
	private String DEN;
	private String ContentComponentDEN;
	private String Definition;
	private String RevisionDocumentation;
	private int RevisionState;
	private int CreatedByUserId;
	private int LastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	
	public int getDTID() {
		return DTID;
	}
	
	public void setDTID(int dTID) {
		DTID = dTID;
	}
	
	public String getDTGUID() {
		return DTGUID;
	}
	
	public void setDTGUID(String dTGUID) {
		DTGUID = dTGUID;
	}
	
	public int getDTType() {
		return DTType;
	}
	
	public void setDTType(int dTType) {
		DTType = dTType;
	}
	
	public String getVersionNumber() {
		return VersionNumber;
	}
	
	public void setVersionNumber(String versionNumber) {
		VersionNumber = versionNumber;
	}
	
	public int getPreviousVersionDTID() {
		return PreviousVersionDTID;
	}
	
	public void setPreviousVersionDTID(int previousVersionDTID) {
		PreviousVersionDTID = previousVersionDTID;
	}
	
	public int getRevisionType() {
		return RevisionType;
	}
	
	public void setRevisionType(int revisionType) {
		RevisionType = revisionType;
	}
	
	public String getDataTypeTerm() {
		return DataTypeTerm;
	}
	
	public void setDataTypeTerm(String dataTypeTerm) {
		DataTypeTerm = dataTypeTerm;
	}
	
	public String getQualifier() {
		return Qualifier;
	}
	
	public void setQualifier(String qualifier) {
		Qualifier = qualifier;
	}
	
	public String getBasedDTID() {
		return BasedDTID;
	}
	
	public void setBasedDTID(String basedDTID) {
		BasedDTID = basedDTID;
	}
	
	public String getDEN() {
		return DEN;
	}
	
	public void setDEN(String dEN) {
		DEN = dEN;
	}
	
	public String getContentComponentDEN() {
		return ContentComponentDEN;
	}
	
	public void setContentComponentDEN(String contentComponentDEN) {
		ContentComponentDEN = contentComponentDEN;
	}
	
	public String getDefinition() {
		return Definition;
	}
	
	public void setDefinition(String definition) {
		Definition = definition;
	}
	
	public String getRevisionDocumentation() {
		return RevisionDocumentation;
	}
	
	public void setRevisionDocumentation(String revisionDocumentation) {
		RevisionDocumentation = revisionDocumentation;
	}
	
	public int getRevisionState() {
		return RevisionState;
	}
	
	public void setRevisionState(int revisionState) {
		RevisionState = revisionState;
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
