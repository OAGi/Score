package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class BDTPrimitiveRestrictionVO extends SRTObject {

	private int BDTPrimitiveRestrictionID;
	private int BDTID;
	private int CDTPrimitiveExpressionTypeMapID;
	private int CodeListID;
	private boolean isDefault;
	
	public int getBDTPrimitiveRestrictionID() {
		return BDTPrimitiveRestrictionID;
	}
	
	public void setBDTPrimitiveRestrictionID(int bdtPrimitiveRestrictionID) {
		BDTPrimitiveRestrictionID = bdtPrimitiveRestrictionID;
	}
	
	public int getBDTID() {
		return BDTID;
	}
	
	public void setBDTID(int bdtID) {
		BDTID = bdtID;
	}
	
	public int getCDTPrimitiveExpressionTypeMapID() {
		return CDTPrimitiveExpressionTypeMapID;
	}
	
	public void setCDTPrimitiveExpressionTypeMapID(int cdtPrimitiveExpressionTypeMapID) {
		CDTPrimitiveExpressionTypeMapID = cdtPrimitiveExpressionTypeMapID;
	}
	
	public int getCodeListID() {
		return CodeListID;
	}
	
	public void setCodeListID(int codeListID) {
		CodeListID = codeListID;
	}
	
	public boolean getisDefault() {
		return isDefault;
	}
	
	public void setisDefault(boolean isdefault) {
		isDefault = isdefault;
	}
	
}
