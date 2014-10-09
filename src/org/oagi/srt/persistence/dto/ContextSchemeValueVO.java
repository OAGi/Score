package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ContextSchemeValueVO extends SRTObject {

	private int ContextSchemeValueID;
	private String Value;
	private String Meaning;
	private int OwnerContextSchemeID;
	
	public int getContextSchemeValueID(){
		return ContextSchemeValueID;
	}
	
	public void setContextSchemeValueID(int contextSchemeValueID){
		ContextSchemeValueID = contextSchemeValueID;
	}
	
	public String getValue(){
		return Value;
	}
	
	public void setValue(String value){
		Value = value;
	}
	
	public String getMeaning(){
		return Meaning;
	}
	
	public void setMeaning(String meaning){
		Meaning = meaning;
	}
	
	public int getOwnerContextSchemeID(){
		return OwnerContextSchemeID;
	}
	
	public void setOwnerContextSchemeID(int ownerContextSchemeID){
		OwnerContextSchemeID = ownerContextSchemeID;
	}
	
}
