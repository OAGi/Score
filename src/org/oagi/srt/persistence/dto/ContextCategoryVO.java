package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ContextCategoryVO extends SRTObject {

	private int contextCategoryID;
	private String contextCategoryGUID;
	private String name;
	private String description;
	
	public int getContextCategoryID(){
		return contextCategoryID;
	}
	
	public void setContextCategoryID(int contextCategoryID){
		this.contextCategoryID = contextCategoryID;
	}
	
	public String getContextCategoryGUID() {
		return contextCategoryGUID;
	}

	public void setContextCategoryGUID(String contextCategoryGUID) {
		this.contextCategoryGUID = contextCategoryGUID;
	}

	public String getName(){
		return name;
	}
	
	public void setName(String name){
		System.out.println("SET NAME:" + name);
		this.name = name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}

}
