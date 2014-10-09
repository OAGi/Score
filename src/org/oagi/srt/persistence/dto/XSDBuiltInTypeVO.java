package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class XSDBuiltInTypeVO extends SRTObject {
	
	private int XSDBuiltInTypeID;
	private String Name;
	private String BuiltInType;
	private int subtypeOfXSDBuiltinTypeId;
	
	public int getXSDBuiltInTypeID() {
		return XSDBuiltInTypeID;
	}
	
	public void setXSDBuiltInTypeID(int xsdBuiltIntypeID) {
		XSDBuiltInTypeID = xsdBuiltIntypeID;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
	public String getBuiltInType() {
		return BuiltInType;
	}
	
	public void setBuiltInType(String builtInType) {
		BuiltInType = builtInType;
	}

	public int getSubtypeOfXSDBuiltinTypeId() {
		return subtypeOfXSDBuiltinTypeId;
	}

	public void setSubtypeOfXSDBuiltinTypeId(int subtypeOfXSDBuiltinTypeId) {
		this.subtypeOfXSDBuiltinTypeId = subtypeOfXSDBuiltinTypeId;
	}

}
