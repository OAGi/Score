package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class CDT_SC_Allowed_PrimitiveVO extends SRTObject {

	private int CDTSCAllowedPrimitiveID;
	private int CDTSCID;
	private int CDTPrimitiveID;
	private boolean isDefault;
	
	public int getCDTSCAllowedPrimitiveID() {
		return CDTSCAllowedPrimitiveID;
	}
	
	public void setCDTSCAllowedPrimitiveID(int cdtSCAllowedPrimitiveID) {
		CDTSCAllowedPrimitiveID = cdtSCAllowedPrimitiveID;
	}
	
	public int getCDTSCID() {
		return CDTSCID;
	}
	
	public void setCDTSCID(int cdtSCID) {
		CDTSCID = cdtSCID;
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
