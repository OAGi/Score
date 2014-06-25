package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class CDT_Allowed_PrimitiveVO extends SRTObject {

	private int CDTAllowedPrimitiveID;
	private int CDTID;
	private int CDTPrimitiveID;
	private boolean isDefault;
	
	public int getCDTAllowedPrimitiveID() {
		return CDTAllowedPrimitiveID;
	}
	
	public void setCDTAllowedPrimitiveID(int cdtAllowedPrimitiveID) {
		CDTAllowedPrimitiveID = cdtAllowedPrimitiveID;
	}
	
	public int getCDTID() {
		return CDTID;
	}
	
	public void setCDTID(int cdtID) {
		CDTID = cdtID;
	}
	
	public int getCDTPrimitiveID() {
		return CDTPrimitiveID;
	}
	
	public void setCDTPrimitiveID(int cdtPrimitiveID) {
		CDTPrimitiveID = cdtPrimitiveID;
	}
	
	public boolean getisDefault() {
		return isDefault;
	}
	
	public void setisDefault(boolean isdefault) {
		isDefault = isdefault;
	}
	
}
