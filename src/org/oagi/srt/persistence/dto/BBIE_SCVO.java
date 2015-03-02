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
	
	private int codeListId;
	private String defaultText;
	private String fixedValue;
	private String definition;
	private String remark;
	private String businessTerm;
	
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
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getBusinessTerm() {
		return businessTerm;
	}
	public void setBusinessTerm(String businessTerm) {
		this.businessTerm = businessTerm;
	}
}
