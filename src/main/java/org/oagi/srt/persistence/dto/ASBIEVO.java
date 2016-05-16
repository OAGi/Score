package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

import java.sql.Timestamp;

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
	private int CardinalityMin;
	private int CardinalityMax;
	
	private String asbieGuid;
	private String definition;
	private String ccDefinition;
	private int nillable;
	private String remark;
	private int createdByUserId;
	private int lastUpdatedByUserId;
	private Timestamp creationTimestamp;
	private Timestamp lastUpdateTimestamp;
	private double sequencingKey;
	private boolean used;

	public String getCcDefinition() {
		return ccDefinition;
	}

	public void setCcDefinition(String ccDefinition) {
		this.ccDefinition = ccDefinition;
	}

	public double getSequencingKey() {
		return sequencingKey;
	}

	public void setSequencingKey(double sequencingKey) {
		this.sequencingKey = sequencingKey;
	}

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

	public String getAsbieGuid() {
		return asbieGuid;
	}

	public void setAsbieGuid(String asbieGuid) {
		this.asbieGuid = asbieGuid;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public int getNillable() {
		return nillable;
	}

	public void setNillable(int nillable) {
		this.nillable = nillable;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(int createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public int getLastUpdatedByUserId() {
		return lastUpdatedByUserId;
	}

	public void setLastUpdatedByUserId(int lastUpdatedByUserId) {
		this.lastUpdatedByUserId = lastUpdatedByUserId;
	}

	public Timestamp getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(Timestamp creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public Timestamp getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}
	
}