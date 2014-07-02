package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class BBIE_SCVO extends SRTObject {

	private int BBIESCID;
	private int BBIEID;
	private int DTSCID;
	private int MinCardinality;
	private int MaxCardinality;
	private int DTSCPrimitiveRestrictionID;
	
	public int getBBIESCID() {
		return BBIESCID;
	}
	public void setBBIESCID(int bBIESCID) {
		BBIESCID = bBIESCID;
	}
	public int getBBIEID() {
		return BBIEID;
	}
	public void setBBIEID(int bBIEID) {
		BBIEID = bBIEID;
	}
	public int getDTSCID() {
		return DTSCID;
	}
	public void setDTSCID(int dTSCID) {
		DTSCID = dTSCID;
	}
	public int getMinCardinality() {
		return MinCardinality;
	}
	public void setMinCardinality(int minCardinality) {
		MinCardinality = minCardinality;
	}
	public int getMaxCardinality() {
		return MaxCardinality;
	}
	public void setMaxCardinality(int maxCardinality) {
		MaxCardinality = maxCardinality;
	}
	public int getDTSCPrimitiveRestrictionID() {
		return DTSCPrimitiveRestrictionID;
	}
	public void setDTSCPrimitiveRestrictionID(int dTSCPrimitiveRestrictionID) {
		DTSCPrimitiveRestrictionID = dTSCPrimitiveRestrictionID;
	}
	
}
