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
	private String Module;
	private int NamespaceId;
	private int OwnerUserId;
	private int RevisionNum;
	private int RevisionTrackingNum;
	private boolean RevisionAction;
	private int ReleaseId;
	private int CurrentAccId;
	private boolean Is_deprecated;
	
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
	
	public void setState(int state) {
		State = state;
	}
	
	public String getModule() {
		return Module;
	}
	
	public void setModule(String module) {
		Module = module;
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
	
	public int getCurrentAccId() {
		return CurrentAccId;
	}
	
	public void setCurrentAccId(int currentAccId) {
		CurrentAccId = currentAccId;
	}
	
	public boolean getIs_deprecated() {
		return Is_deprecated;
	}
	
	public void setIs_deprecated(boolean is_deprecated) {
		Is_deprecated = is_deprecated;
	}
		
}
