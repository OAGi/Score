package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Jaehun Lee
 */
public class BCCPVO extends SRTObject {

	private int BCCPID;
	private String BCCPGUID;
	private String PropertyTerm;
	private String RepresentationTerm;
	private int BDTID;
	private String DEN;
	private String Definition;
	private int CreatedByUserId;
	private int LastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	
	private int OwnerUserId;
	private int State;
	private String Module;
	private int RevisionNum;
	private int RevisionTrackingNum;
	private boolean RevisionAction;
	private int ReleaseId;
	private int CurrentBccpId;
	private boolean Is_deprecated;
	
	public int getBCCPID() {
		return BCCPID;
	}
	
	public void setBCCPID(int bCCPID) {
		BCCPID = bCCPID;
	}
	
	public String getBCCPGUID() {
		return BCCPGUID;
	}
	
	public void setBCCPGUID(String bCCPGUID) {
		BCCPGUID = bCCPGUID;
	}
	
	public String getPropertyTerm() {
		return PropertyTerm;
	}
	
	public void setPropertyTerm(String propertyTerm) {
		PropertyTerm = propertyTerm;
	}
	
	public String getRepresentationTerm() {
		return RepresentationTerm;
	}
	
	public void setRepresentationTerm(String representationTerm) {
		RepresentationTerm = representationTerm;
	}
	
	public int getBDTID() {
		return BDTID;
	}
	
	public void setBDTID(int bDTID) {
		BDTID = bDTID;
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
	
	public int getOwnerUserId() {
		return OwnerUserId;
	}
	
	public void setOwnerUserId(int ownerUserId) {
		OwnerUserId = ownerUserId;
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
	
	public int getCurrentBccpId() {
		return CurrentBccpId;
	}
	
	public void setCurrentBccpId(int currentBccpId) {
		CurrentBccpId = currentBccpId;
	}
	
	public boolean getIs_deprecated() {
		return Is_deprecated;
	}
	
	public void setIs_deprecated(boolean is_deprecated) {
		Is_deprecated = is_deprecated;
	}
}
