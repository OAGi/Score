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
	private int DTSCPrimitiveRestrictionID;
	private int codeListId;
	private int AgencyIdListId;
	private int MinCardinality;
	private int MaxCardinality;
	
	private String DefaultText;
	private String FixedValue;
	private String Definition;
	private String Remark;
	private String BusinessTerm;
	
	private boolean used;
	
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
	public int getCodeListId() {
		return codeListId;
	}
	public void setCodeListId(int codeListId) {
		this.codeListId = codeListId;
	}
	public int getAgencyIdListId() {
		return AgencyIdListId;
	}
	public void setAgencyIdListId(int agencyIdListId) {
		this.AgencyIdListId = agencyIdListId;
	}
	public String getDefaultText() {
		return DefaultText;
	}
	public void setDefaultText(String defaultText) {
		this.DefaultText = defaultText;
	}
	public String getFixedValue() {
		return FixedValue;
	}
	public void setFixedValue(String fixedValue) {
		this.FixedValue = fixedValue;
	}
	public String getDefinition() {
		return Definition;
	}
	public void setDefinition(String definition) {
		this.Definition = definition;
	}
	public String getRemark() {
		return Remark;
	}
	public void setRemark(String remark) {
		this.Remark = remark;
	}
	public String getBusinessTerm() {
		return BusinessTerm;
	}
	public void setBusinessTerm(String businessTerm) {
		this.BusinessTerm = businessTerm;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}
}
