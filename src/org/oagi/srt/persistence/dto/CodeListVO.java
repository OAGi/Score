package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Jaehun Lee
*/


public class CodeListVO extends SRTObject {
	
	private int CodeListID;
	private String CodeListGUID;
	private String EnumerationTypeGUID;
	private String Name;
	private String ListID;
	private int AgencyID;
	private String VersionID;
	private String Definition;
	private String DefinitionSource;
	private int BasedCodeListID;
	private boolean ExtensibleIndicator;
	private int CreatedByUserID;
	private int LastUpdatedByUserID;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private String State;
	
	public String getState() {
		return State;
	}

	public void setState(String state) {
		State = state;
	}

	public int getCodeListID(){
		return CodeListID;
	}
	
	public void setCodeListID(int codeListID){
		CodeListID = codeListID;
	}
	
	public String getCodeListGUID(){
		return CodeListGUID;
	}
	
	public void setCodeListGUID(String codeListGUID){
		CodeListGUID = codeListGUID;
	}
	
	public String getEnumerationTypeGUID(){
		return EnumerationTypeGUID;
	}
	
	public void setEnumerationTypeGUID(String enumerationTypeGUID){
		EnumerationTypeGUID = enumerationTypeGUID;
	}
	
	public String getName(){
		return Name;
	}
	
	public void setName(String name){
		Name = name;
	}
	
	public String getListID(){
		return ListID;
	}
	
	public void setListID(String listID){
		ListID = listID;
	}
	
	public int getAgencyID(){
		return AgencyID;
	}
	
	public void setAgencyID(int agencyID){
		AgencyID = agencyID;
	}
	
	public String getVersionID(){
		return VersionID;
	}
	
	public void setVersionID(String versionID){
		VersionID = versionID;
	}
	
	public String getDefinition(){
		return Definition;
	}
	
	public void setDefinition(String definition){
		Definition = definition;
	}
	
	public String getDefinitionSource(){
		return DefinitionSource;
	}
	
	public void setDefinitionSource(String definitionSource){
		DefinitionSource = definitionSource;
	}
	
	public int getBasedCodeListID(){
		return BasedCodeListID;
	}
	
	public void setBasedCodeListID(int basedCodeListID){
		BasedCodeListID = basedCodeListID;
	}
	
	public boolean getExtensibleIndicator(){
		return ExtensibleIndicator;
	}
	
	public void setExtensibleIndicator(boolean extensibleIndicator){
		ExtensibleIndicator = extensibleIndicator;
	}
		
	public int getCreatedByUserID(){
		return CreatedByUserID;
	}
	
	public void setCreatedByUserID(int createdByUserID){
		CreatedByUserID = createdByUserID;
	}
	
	public int getLastUpdatedByUserID(){
		return LastUpdatedByUserID;
	}
	
	public void setLastUpdatedByUserID(int lastUpdatedByUserID){
		LastUpdatedByUserID = lastUpdatedByUserID;
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
	

}
