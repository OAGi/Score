package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class BusinessContextVO extends SRTObject {

	private int BusinessContextID;
	private String Name;
	
	public int getBusinessContextID(){
		return BusinessContextID;
	}
	
	public void setBusinessContextID(int businessContextID){
		BusinessContextID = businessContextID;
	}
	
	public String getName(){
		return Name;
	}
	
	public void setName(String name){
		Name = name;
	}
	
}
