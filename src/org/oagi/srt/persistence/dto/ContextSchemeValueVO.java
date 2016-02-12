package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ContextSchemeValueVO extends SRTObject {

	private int contextSchemeValueID;
	private String contextSchemeValueGUID;
	private String value;
	private String meaning;
	private int ownerContextSchemeID;
	
	public int getContextSchemeValueID(){
		return contextSchemeValueID;
	}
	
	public void setContextSchemeValueID(int contextSchemeValueID){
		this.contextSchemeValueID = contextSchemeValueID;
	}
	
	public String getValue(){
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getMeaning(){
		return meaning;
	}
	
	public void setMeaning(String meaning){
		this.meaning = meaning;
	}
	
	public int getOwnerContextSchemeID(){
		return ownerContextSchemeID;
	}
	
	public void setOwnerContextSchemeID(int ownerContextSchemeID){
		this.ownerContextSchemeID = ownerContextSchemeID;
	}

	public String getContextSchemeValueGUID() {
		return contextSchemeValueGUID;
	}

	public void setContextSchemeValueGUID(String contextSchemeValueGUID) {
		this.contextSchemeValueGUID = contextSchemeValueGUID;
	}
}
