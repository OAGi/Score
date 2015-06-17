package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class BCCVO extends SRTObject {

	private int BCCID;
	private String BCCGUID;
	private int CardinalityMin;
	private int CardinalityMax;
	private int AssocToBCCPID;
	private int AssocFromACCID;
	private int sequencingKey;
	private int EntityType;
	private String DEN;
	
	public int getBCCID() {
		return BCCID;
	}
	
	public void setBCCID(int bccID) {
		BCCID = bccID;
	}

	public String getBCCGUID() {
		return BCCGUID;
	}
	
	public void setBCCGUID(String bccGUID) {
		BCCGUID = bccGUID;
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
	
	public int getAssocToBCCPID() {
		return AssocToBCCPID;
	}
	
	public void setAssocToBCCPID(int assocToBCCPID) {
		AssocToBCCPID = assocToBCCPID;
	}
	
	public int getAssocFromACCID() {
		return AssocFromACCID;
	}
	
	public void setAssocFromACCID(int assocFromACCID) {
		AssocFromACCID = assocFromACCID;
	}
	
	public int getSequencingKey() {
		return sequencingKey;
	}
	
	public void setSequencingKey(int sequencingKey) {
		this.sequencingKey = sequencingKey;
	}
	
	public int getEntityType() {
		return EntityType;
	}
	
	public void setEntityType(int entityType) {
		EntityType = entityType;
	}
	
	public String getDEN() {
		return DEN;
	}
	
	public void setDEN(String dEN) {
		DEN = dEN;
	}
}