package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

import java.sql.Timestamp;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ABIEVO extends SRTObject {

	private int ABIEID;
	private String abieGUID;
	private int BasedACCID;
	private int IsTopLevel;
	private int BusinessContextID;
	private String businessContextName;
	private String Definition;
	private String ccDefinition;
	private int CreatedByUserID;
	private int LastUpdatedByUserID;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private int State;
	private int clientID;
	private String version;
	private String status;
	private String remark;
	private String businessTerm;
	
	public String getCcDefinition() {
		return ccDefinition;
	}

	public void setCcDefinition(String ccDefinition) {
		this.ccDefinition = ccDefinition;
	}

	public String getBusinessContextName() {
		return businessContextName;
	}

	public void setBusinessContextName(String businessContextName) {
		this.businessContextName = businessContextName;//Not Used
	}

	public int getABIEID(){
		return ABIEID;
	}
	
	public void setABIEID(int abieID){
		ABIEID = abieID;//Not Null
	}
	
	public int getBasedACCID(){
		return BasedACCID;
	}
	
	public void setBasedACCID(int basedACCID){
		BasedACCID = basedACCID;//Not Null
	}
	
	public int getIsTopLevel(){
		return IsTopLevel;
	}
	
	public void setIsTopLevel(int isTopLevel){
		IsTopLevel = isTopLevel;//Not Null
	}
	
	public int getBusinessContextID(){
		return BusinessContextID;
	}
	
	public void setBusinessContextID(int businessContextID){
		BusinessContextID = businessContextID;//Not Null
	}
	
	public String getDefinition(){
		return Definition;
	}
	
	public void setDefinition(String definition){
		
		
			Definition = definition;
		
	}
	
	public int getCreatedByUserID(){
		return CreatedByUserID;
	}
	
	public void setCreatedByUserID(int createdByUserID){
		CreatedByUserID = createdByUserID;// Not Null
	}
	
	public int getLastUpdatedByUserID(){
		return LastUpdatedByUserID;
	}
	
	public void setLastUpdatedByUserID(int lastUpdatedByUserID){
		LastUpdatedByUserID = lastUpdatedByUserID; // Not Null
	}
	
	public Timestamp getCreationTimestamp(){
		return CreationTimestamp;
	}
	
	public void setCreationTimestamp(Timestamp creationTimestamp){
		CreationTimestamp = creationTimestamp;
	}
	
	public Timestamp getLastUpdateTimestamp(){
		return LastUpdateTimestamp;
	}
	
	public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp){
		LastUpdateTimestamp = lastUpdateTimestamp;
	}
	
	public int getState(){
		return State;
	}
	
	public void setState(int state){
		State = state; // Not Null
	}

	public String getAbieGUID() {
		return abieGUID;
	}

	public void setAbieGUID(String abieGUID) {
		this.abieGUID = abieGUID; // Not Null
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		
			this.version = version;
		
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		
			this.status = status;
		
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		
			this.remark = remark;
		
	}

	public String getBusinessTerm() {
		return businessTerm;
	}

	public void setBusinessTerm(String businessTerm) {
		
		this.businessTerm = businessTerm;
		
	}	
	
}
