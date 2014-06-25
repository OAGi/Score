package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class BBIEVO extends SRTObject {

	private int BBIEID;
	private int BasedBCCID;
	private int CardinalityMin;
	private int CardinalityMax;
	private boolean isNillable;
	private String Fixedvalue;
	private int AssocFromABIEID;
	private int AssocToBBIEPID;
	private String Definition;
	
	public int getBBIEID() {
		return BBIEID;
	}
	
	public void setBBIEID(int bBIEID) {
		BBIEID = bBIEID;
	}
	
	public int getBasedBCCID() {
		return BasedBCCID;
	}
	
	public void setBasedBCCID (int basedBCCID) {
		BasedBCCID = basedBCCID;
	}

	public int getCardinalityMin() {
		return CardinalityMin;
	}
	
	public void setCardinalityMin(int cardinalityMin) {
		CardinalityMin = cardinalityMin;
	}
	
	public int getCardinalityMax() {
		return CardinalityMax;
	}
	
	public void setCardinalityMax(int cardinalityMax) {
		CardinalityMax = cardinalityMax;
	}
	
	public boolean getisNillable() {
		return isNillable;
	}
	
	public void setisNillable(boolean isnillable) {
		isNillable = isnillable;
	}
	
	public String getFixedvalue() {
		return Fixedvalue;
	}
	
	public void setFixedvalue(String fixedvalue) {
		Fixedvalue = fixedvalue;
	}
		
	public int getAssocFromABIEID() {
		return AssocFromABIEID;
	}
	
	public void setAssocFromABIEID(int assocFromABIEID) {
		AssocFromABIEID = assocFromABIEID;
	}
	
	public int getAssocToBBIEPID() {
		return AssocToBBIEPID;
	}
	
	public void setAssocToBBIEPID(int assocToBBIEPID) {
		AssocToBBIEPID = assocToBBIEPID;
	}
	
	public String getDefinition() {
		return Definition;
	}
	
	public void setDefinition(String definition) {
		Definition = definition;
	}
	
}