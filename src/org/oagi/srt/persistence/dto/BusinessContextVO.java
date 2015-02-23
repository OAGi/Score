package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class BusinessContextVO extends SRTObject {

	private int businessContextID;
	private String businessContextGUID;
	private String name;
	
	public int getBusinessContextID(){
		return businessContextID;
	}
	
	public void setBusinessContextID(int businessContextID){
		this.businessContextID = businessContextID;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public String getBusinessContextGUID() {
		return businessContextGUID;
	}

	public void setBusinessContextGUID(String businessContextGUID) {
		this.businessContextGUID = businessContextGUID;
	}
}
