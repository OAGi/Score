package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class Context_CategoryVO extends SRTObject {

	private int ContextCategoryID;
	private String Name;
	private String Description;
	
	public int getContextCategoryID(){
		return ContextCategoryID;
	}
	
	public void setContextCategoryID(int contextCategoryID){
		ContextCategoryID = contextCategoryID;
	}
	
	public String getName(){
		return Name;
	}
	
	public void setName(String name){
		Name = name;
	}
	
	public String getDescription(){
		return Description;
	}
	
	public void setDescription(String description){
		Description = description;
	}

}
