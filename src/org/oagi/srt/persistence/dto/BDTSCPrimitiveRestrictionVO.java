package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class BDTSCPrimitiveRestrictionVO extends SRTObject {

	private int BDTSCPrimitiveRestrictionID;
	private int BDTSCID;
	private int CDTSCAllowedPrimitiveExpressionTypeMapID;
	private int CodeListID;
	private boolean isDefault;
	private int AgencyIDListID;
	
	public int getBDTSCPrimitiveRestrictionID() {
		return BDTSCPrimitiveRestrictionID;
	}
	
	public void setBDTSCPrimitiveRestrictionID(int bdtscPrimitiveRestrictionID) {
		BDTSCPrimitiveRestrictionID = bdtscPrimitiveRestrictionID;
	}
	
	public int getBDTSCID() {
		return BDTSCID;
	}
	
	public void setBDTSCID(int bdtscID) {
		BDTSCID = bdtscID;
	}
	
	public int getCDTSCAllowedPrimitiveExpressionTypeMapID() {
		return CDTSCAllowedPrimitiveExpressionTypeMapID;
	}
	
	public void setCDTSCAllowedPrimitiveExpressionTypeMapID(int cdtscAllowedPrimitiveExpressionTypeMapID) {
		CDTSCAllowedPrimitiveExpressionTypeMapID = cdtscAllowedPrimitiveExpressionTypeMapID;
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
	
	public int getAgencyIDListID() {
		return AgencyIDListID;
	}
	
	public void setAgencyIDListID(int agencyIDListID) {
		AgencyIDListID = agencyIDListID;
	}
	
}
