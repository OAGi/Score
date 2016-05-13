package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

import java.sql.Timestamp;

/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class BBIEVO extends SRTObject {

	private int BBIEID;
	private String bbieGuid;
	private int BasedBCCID;
	private int AssocFromABIEID;
	private int AssocToBBIEPID;
	private int bdtPrimitiveRestrictionId;
	private int codeListId;
	private int CardinalityMin;
	private int CardinalityMax;
	private String defaultText;
	private int Nillable;
	private String fixedValue;
	private int isNull;
	private String Definition;
	private String remark;
	private int createdByUserId;
	private int lastUpdatedByUserId;
	private Timestamp creationTimestamp;
	private Timestamp lastUpdateTimestamp;
	private double sequencing_key;
	private boolean used;
	
	public int getIsNull() {
		return isNull;
	}

	public void setIsNull(int isNull) {
		this.isNull = isNull;
	}

	public double getSequencing_key() {
		return sequencing_key;
	}

	public void setSequencing_key(double sequencing_key) {
		this.sequencing_key = sequencing_key;
	}

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
	
	public int getNillable() {
		return Nillable;
	}
	
	public void setNillable(int nillable) {
		Nillable = nillable;
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

	public String getBbieGuid() {
		return bbieGuid;
	}

	public void setBbieGuid(String bbieGuid) {
		this.bbieGuid = bbieGuid;
	}

	public int getBdtPrimitiveRestrictionId() {
		return bdtPrimitiveRestrictionId;
	}

	public void setBdtPrimitiveRestrictionId(int bdtPrimitiveRestrictionId) {
		this.bdtPrimitiveRestrictionId = bdtPrimitiveRestrictionId;
	}

	public int getCodeListId() {
		return codeListId;
	}

	public void setCodeListId(int codeListId) {
		this.codeListId = codeListId;
	}

	public String getDefaultText() {
		return defaultText;
	}

	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
	}

	public String getFixedValue() {
		return fixedValue;
	}

	public void setFixedValue(String fixedValue) {
		this.fixedValue = fixedValue;
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