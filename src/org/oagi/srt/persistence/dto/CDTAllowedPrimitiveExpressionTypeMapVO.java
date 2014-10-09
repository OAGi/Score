package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class CDTAllowedPrimitiveExpressionTypeMapVO extends SRTObject {
	
	private int CDTPrimitiveExpressionTypeMapID;
	private int CDTAllowedPrimitiveID;
	private int XSDBuiltInTypeID;
	
	public int getCDTPrimitiveExpressionTypeMapID() {
		return CDTPrimitiveExpressionTypeMapID;
	}
	
	public void setCDTPrimitiveExpressionTypeMapID(int cdtPrimitiveExpressionTypeMapID) {
		CDTPrimitiveExpressionTypeMapID = cdtPrimitiveExpressionTypeMapID;
	}
	
	public int getCDTAllowedPrimitiveID() {
		return CDTAllowedPrimitiveID;
	}
	
	public void setCDTAllowedPrimitiveID(int cdtAllowedPrimitiveID) {
		CDTAllowedPrimitiveID = cdtAllowedPrimitiveID;
	}
	
	public int getXSDBuiltInTypeID() {
		return XSDBuiltInTypeID;
	}
	
	public void setXSDBuiltInTypeID(int xsdBuiltInTypeID) {
		XSDBuiltInTypeID = xsdBuiltInTypeID;
	}

}
