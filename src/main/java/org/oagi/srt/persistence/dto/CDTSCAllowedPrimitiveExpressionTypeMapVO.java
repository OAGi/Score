package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class CDTSCAllowedPrimitiveExpressionTypeMapVO extends SRTObject {
	private int CTSCAllowedPrimitiveExpressionTypeMapID;
	private int CDTSCAllowedPrimitive;
	private int XSDBuiltInTypeID;
	
	public int getCTSCAllowedPrimitiveExpressionTypeMapID() {
		return CTSCAllowedPrimitiveExpressionTypeMapID;
	}
	public void setCTSCAllowedPrimitiveExpressionTypeMapID(
			int cTSCAllowedPrimitiveExpressionTypeMapID) {
		CTSCAllowedPrimitiveExpressionTypeMapID = cTSCAllowedPrimitiveExpressionTypeMapID;
	}
	public int getCDTSCAllowedPrimitive() {
		return CDTSCAllowedPrimitive;
	}
	public void setCDTSCAllowedPrimitive(int cDTSCAllowedPrimitive) {
		CDTSCAllowedPrimitive = cDTSCAllowedPrimitive;
	}
	public int getXSDBuiltInTypeID() {
		return XSDBuiltInTypeID;
	}
	public void setXSDBuiltInTypeID(int xSDBuiltInTypeID) {
		XSDBuiltInTypeID = xSDBuiltInTypeID;
	}
}
