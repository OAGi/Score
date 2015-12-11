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
	private String DataTypeTerm;
	private String Qualifier;
	private int BasedDTID;
	private String DEN;
	private String ContentComponentDEN;
	private String Definition;
	private String ContentComponentDefinition;
	private String RevisionDocumentation;
	private int State;
	private int CreatedByUserId;
	private int OwnerUserId;
	private int LastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private int RevisionNum;
	private int RevisionTrackingNum;
	private boolean RevisionAction;
	private int ReleaseId;
	private int CurrentBdtId;
	private boolean Is_deprecated;
	
	
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
	
	public int getBasedDTID() {
		return BasedDTID;
	}
	
	public void setBasedDTID(int basedDTID) {
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
	
	public String getContentComponentDefinition() {
		return ContentComponentDefinition;
	}

	public void setContentComponentDefinition(String contentComponentDefinition) {
		ContentComponentDefinition = contentComponentDefinition;
	}
	
	public String getRevisionDocumentation() {
		return RevisionDocumentation;
	}
	
	public void setRevisionDocumentation(String revisionDocumentation) {
		RevisionDocumentation = revisionDocumentation;
	}
	
	public int getState() {
		return State;
	}
	
	public void setState(int state) {
		State = state;
	}
	
	public int getCreatedByUserId() {
		return CreatedByUserId;
	}
	
	public void setCreatedByUserId(int createdByUserId) {
		CreatedByUserId = createdByUserId;
	}
	
	public int getOwnerUserId() {
		return OwnerUserId;
	}
	
	public void setOwnerUserId(int ownerUserId) {
		OwnerUserId = ownerUserId;
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
	public int getRevisionNum() {
		return RevisionNum;
	}
	
	public void setRevisionNum(int revisionNum) {
		RevisionNum = revisionNum;
	}
	
	public int getRevisionTrackingNum() {
		return RevisionTrackingNum;
	}
	
	public void setRevisionTrackingNum(int revisionTrackingNum) {
		RevisionTrackingNum = revisionTrackingNum;
	}
	
	public boolean getRevisionAction() {
		return RevisionAction;
	}
	
	public void setRevisionAction(boolean revisionAction) {
		RevisionAction = revisionAction;
	}
	
	public int getReleaseId() {
		return ReleaseId;
	}
	
	public void setReleaseId(int releaseId) {
		ReleaseId = releaseId;
	}
	
	public int getCurrentBdtId() {
		return CurrentBdtId;
	}
	
	public void setCurrentBdtId(int currentBdtId) {
		CurrentBdtId = currentBdtId;
	}
	
	public boolean getIs_deprecated() {
		return Is_deprecated;
	}
	
	public void setIs_deprecated(boolean is_deprecated) {
		Is_deprecated = is_deprecated;
	}
}
