package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Jaehun Lee
*/


public class CodeListVO extends SRTObject {
	
	private int codeListID;
	private String codeListGUID;
	private String enumerationTypeGUID;
	private String name;
	private String listID;
	private int agencyID;
	private String versionID;
	private String definition;
	private String definitionSource;
	private int basedCodeListID;
	private boolean extensibleIndicator;
	private int createdByUserID;
	private int lastUpdatedByUserID;
	private Timestamp creationTimestamp;
	private Timestamp lastUpdateTimestamp;
	private String state;
	private String remark;
	private boolean editDisabled;
	private boolean deleteDisabled;
	private boolean discardDisabled;
	
	public boolean isDeleteDisabled() {
		return deleteDisabled;
	}

	public void setDeleteDisabled(boolean deleteDisabled) {
		this.deleteDisabled = deleteDisabled;
	}

	public boolean isDiscardDisabled() {
		return discardDisabled;
	}

	public void setDiscardDisabled(boolean discardDisabled) {
		this.discardDisabled = discardDisabled;
	}

	public boolean isEditDisabled() {
		return editDisabled;
	}

	public void setEditDisabled(boolean editDisabled) {
		this.editDisabled = editDisabled;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getCodeListID(){
		return codeListID;
	}
	
	public void setCodeListID(int codeListID){
		this.codeListID = codeListID;
	}
	
	public String getCodeListGUID(){
		return codeListGUID;
	}
	
	public void setCodeListGUID(String codeListGUID){
		this.codeListGUID = codeListGUID;
	}
	
	public String getEnumerationTypeGUID(){
		return enumerationTypeGUID;
	}
	
	public void setEnumerationTypeGUID(String enumerationTypeGUID){
		this.enumerationTypeGUID = enumerationTypeGUID;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getListID(){
		return listID;
	}
	
	public void setListID(String listID){
		this.listID = listID;
	}
	
	public int getAgencyID(){
		return agencyID;
	}
	
	public void setAgencyID(int agencyID){
		this.agencyID = agencyID;
	}
	
	public String getVersionID(){
		return versionID;
	}
	
	public void setVersionID(String versionID){
		this.versionID = versionID;
	}
	
	public String getDefinition(){
		return definition;
	}
	
	public void setDefinition(String definition){
		this.definition = definition;
	}
	
	public String getDefinitionSource(){
		return definitionSource;
	}
	
	public void setDefinitionSource(String definitionSource){
		this.definitionSource = definitionSource;
	}
	
	public int getBasedCodeListID(){
		return basedCodeListID;
	}
	
	public void setBasedCodeListID(int basedCodeListID){
		this.basedCodeListID = basedCodeListID;
	}
	
	public boolean getExtensibleIndicator(){
		return extensibleIndicator;
	}
	
	public void setExtensibleIndicator(boolean extensibleIndicator){
		this.extensibleIndicator = extensibleIndicator;
	}
		
	public int getCreatedByUserID(){
		return createdByUserID;
	}
	
	public void setCreatedByUserID(int createdByUserID){
		this.createdByUserID = createdByUserID;
	}
	
	public int getLastUpdatedByUserID(){
		return lastUpdatedByUserID;
	}
	
	public void setLastUpdatedByUserID(int lastUpdatedByUserID){
		this.lastUpdatedByUserID = lastUpdatedByUserID;
	}
	
	public Timestamp getCreationTimestamp(){
		return creationTimestamp;
	}
	
	public void setCreationTimestamp(Timestamp creationTimestamp){
		this.creationTimestamp = creationTimestamp;
	}
	
	public Timestamp getLastUpdateTimestamp(){
		return lastUpdateTimestamp;
	}
	
	public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp){
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}
	
}
