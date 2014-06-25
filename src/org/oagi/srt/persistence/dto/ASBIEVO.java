package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class ASBIEVO extends SRTObject {

	private int ASBIEID;
	private int AssocFromABIEID;
	private int AssocToASBIEPID;
	private int BasedASCC;
	private String AssocDescription;
	private String PropertyDescription;
	private int CardinalityMin;
	private int CardinalityMax;

	public int getASBIEID() {
		return ASBIEID;
	}
	
	public void setASBIEID(int aSBIEID) {
		ASBIEID = aSBIEID;
	}
	
	
	public int getAssocFromABIEID() {
		return AssocFromABIEID;
	}
	
	public void setAssocFromABIEID(int assocFromABIEID) {
		AssocFromABIEID = assocFromABIEID;
	}
	
	public int getAssocToASBIEPID() {
		return AssocToASBIEPID;
	}
	
	public void setAssocToASBIEPID(int assocToASBIEPID) {
		AssocToASBIEPID = assocToASBIEPID;
	}

	
	public int getBasedASCC() {
		return BasedASCC;
	}
	
	public void setBasedASCC (int basedASCC) {
		BasedASCC = basedASCC;
	}
	
	public String getAssocDescription() {
		return AssocDescription;
	}
	
	public void setAssocDescription(String assocDescription) {
		AssocDescription = assocDescription;
	}
	
	public String getPropertyDescription() {
		return PropertyDescription;
	}
	
	public void setPropertyDescription(String propertyDescription) {
		PropertyDescription = propertyDescription;
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
	
	
}