package org.oagi.srt.persistence.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Jaehun Lee
 */
public class ASCCPVO extends SRTObject implements Serializable {
	
	private static final long serialVersionUID = -3150693005373031742L;

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

	private int NamespaceId;
	private int OwnerUserId;
	private int RevisionNum;
	private int RevisionTrackingNum;
	private int RevisionAction;
	private int ReleaseId;
	private int CurrentAsccpId;
	private boolean Is_deprecated;
	
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
	public int getNamespaceId() {
		return NamespaceId;
	}
	
	public void setNamespaceId(int namespaceId) {
		NamespaceId = namespaceId;
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
	
	public int getRevisionAction() {
		return RevisionAction;
	}
	
	public void setRevisionAction(int revisionAction) {
		RevisionAction = revisionAction;
	}
	
	public int getReleaseId() {
		return ReleaseId;
	}
	
	public void setReleaseId(int releaseId) {
		ReleaseId = releaseId;
	}
	
	public int getCurrentAsccpId() {
		return CurrentAsccpId;
	}
	
	public void setCurrentAsccpId(int currentAsccpId) {
		CurrentAsccpId = currentAsccpId;
	}
	
	public boolean getIs_deprecated() {
		return Is_deprecated;
	}
	
	public void setIs_deprecated(boolean is_deprecated) {
		Is_deprecated = is_deprecated;
	}
}
