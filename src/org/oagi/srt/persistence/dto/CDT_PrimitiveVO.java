package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class CDT_PrimitiveVO extends SRTObject {
	
	private int CDTPrimitiveID;
	private String Name;
	
	public int getCDTPrimitiveID() {
		return CDTPrimitiveID;
	}
	
	public void setCDTPrimitiveID(int cdtPrimitiveID) {
		CDTPrimitiveID = cdtPrimitiveID;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	

}
