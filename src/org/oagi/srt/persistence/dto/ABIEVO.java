package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ABIEVO extends SRTObject {

	private int ABIEID;
	private int BasedACCID;
	private int IsTopLevel;
	private int BusinessContextID;
	private String Definition;
	private int CreatedByUserID;
	private int LastUpdatedByUserID;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private int State;
	
	public int getABIEID(){
		return ABIEID;
	}
	
	public void setABIEID(int abieID){
		ABIEID = abieID;
	}
	
	public int getBasedACCID(){
		return BasedACCID;
	}
	
	public void setBasedACCID(int basedACCID){
		BasedACCID = basedACCID;
	}
	
	public int getIsTopLevel(){
		return IsTopLevel;
	}
	
	public void setIsTopLevel(int isTopLevel){
		IsTopLevel = isTopLevel;
	}
	
	public int getBusinessContextID(){
		return BusinessContextID;
	}
	
	public void setBusinessContextID(int businessContextID){
		BusinessContextID = businessContextID;
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
	
	public int getState(){
		return State;
	}
	
	public void setState(int state){
		State = state;
	}	

}
