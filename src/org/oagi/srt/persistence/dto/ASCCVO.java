package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Nasif Sikder
*/
public class ASCCVO extends SRTObject {

	private int ASCCID;
	private String ASCCGUID;
	private int CardinalityMin;
	private int CardinalityMax;
	private int SequencingKey;
	private int AssocFromACCID;  
	private int AssocToASCCPID;
	private String DEN;
	private String Definition;
	
	private int CreatedByUserId;
	private int OwnerUserId;
	private int LastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private int State;
	private int RevisionNum;
	private int RevisionTrackingNum;
	private boolean RevisionAction;
	private int ReleaseId;
	private int CurrentAsccId;
	private boolean Is_deprecated;
	
	public int getASCCID(){
		return ASCCID;
	}
	
	public void setASCCID(int aSCCID){
		ASCCID = aSCCID;
	}
	
	public String getASCCGUID(){
		return ASCCGUID;
	}
	
	public void setASCCGUID(String aSCCGUID){
		ASCCGUID = aSCCGUID;
	}
	
	public int getCardinalityMin(){
		return CardinalityMin;
	}
	
	public void setCardinalityMin(int cardinalityMin){
		CardinalityMin = cardinalityMin;
	}
	
	public int getCardinalityMax(){
		return CardinalityMax;
	}
	
	public void setCardinalityMax(int cardinalityMax){
		CardinalityMax = cardinalityMax;
	}
	
	public int getSequencingKey(){
		return SequencingKey;
	}
	
	public void setSequencingKey(int sequencingKey){
		SequencingKey = sequencingKey;
	}

	public int getAssocFromACCID(){
		return AssocFromACCID;
	}
	
	public void setAssocFromACCID(int assocFromACCID){
		AssocFromACCID = assocFromACCID;
	}
	
	public int getAssocToASCCPID(){
		return AssocToASCCPID;
	}
	
	public void setAssocToASCCPID(int assocToASCCPID){
		AssocToASCCPID = assocToASCCPID;
	}
	
	public String getDEN(){
		return DEN;
	}
	
	public void setDEN(String dEN){
		DEN = dEN;
	}
	
	public String getDefinition(){
		return Definition;
	}
	
	public void setDefinition(String definition){
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
	
	public int getCurrentAsccId() {
		return CurrentAsccId;
	}
	
	public void setCurrentAsccId(int currentAsccId) {
		CurrentAsccId = currentAsccId;
	}
	
	public boolean getIs_deprecated() {
		return Is_deprecated;
	}
	
	public void setIs_deprecated(boolean is_deprecated) {
		Is_deprecated = is_deprecated;
	}
	
}
