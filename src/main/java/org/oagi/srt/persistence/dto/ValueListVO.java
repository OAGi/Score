package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

import java.sql.Timestamp;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ValueListVO extends SRTObject {
	
	private int ValueListID;
	private int Type;
	private String ValueListGUID;
	private String Name;
	private String ListID;
	private String AgencyID;
	private String VersionID;
	private String Definition;
	private int BasedCodeListID;
	private int ExtensibleIndicator;
	private int CreatedByUserID;
	private int LastUpdatedByUserID;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private String DefinitionSource;

	public int getValueListID(){
		return ValueListID;
	}
	
	public void setValueListID(int valueListID){
		ValueListID = valueListID;
	}
	
	public int getType(){
		return Type;
	}
	
	public void setType(int type){
		Type = type;
	}
	
	public String getValueListGUID(){
		return ValueListGUID;
	}
	
	public void setValueListGUID(String valueListGUID){
		ValueListGUID = valueListGUID;
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
	
	public String getAgencyID(){
		return AgencyID;
	}
	
	public void setAgencyID(String agencyID){
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
	
	public int getBasedCodeListID(){
		return BasedCodeListID;
	}
	
	public void setBasedCodeListID(int basedCodeListID){
		BasedCodeListID = basedCodeListID;
	}
	
	public int getExtensibleIndicator(){
		return ExtensibleIndicator;
	}
	
	public void setExtensibleIndicator(int extensibleIndicator){
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
	
	public String getDefinitionSource(){
		return DefinitionSource;
	}
	
	public void setDefinitionSource(String definitionSource){
		DefinitionSource = definitionSource;
	}	
	
}
