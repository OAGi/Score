package org.oagi.srt.persistence.dto;

import java.io.Serializable;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ContextSchemeVO extends SRTObject implements Serializable {

	private static final long serialVersionUID = -3353563030747879464L;
	
	private int contextSchemeID;
	private String schemeID;
	private String schemeName;
	private String description;
	private String schemeAgencyID;
	private String schemeAgencyName;
	private String schemeVersion;
	private int contextCategoryID;
	
	public int getContextSchemeID() {
		return contextSchemeID;
	}
	public void setContextSchemeID(int contextSchemeID) {
		this.contextSchemeID = contextSchemeID;
	}
	public String getSchemeID() {
		return schemeID;
	}
	public void setSchemeID(String schemeID) {
		this.schemeID = schemeID;
	}
	public String getSchemeName() {
		return schemeName;
	}
	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSchemeAgencyID() {
		return schemeAgencyID;
	}
	public void setSchemeAgencyID(String schemeAgencyID) {
		this.schemeAgencyID = schemeAgencyID;
	}
	public String getSchemeAgencyName() {
		return schemeAgencyName;
	}
	public void setSchemeAgencyName(String schemeAgencyName) {
		this.schemeAgencyName = schemeAgencyName;
	}
	public String getSchemeVersion() {
		return schemeVersion;
	}
	public void setSchemeVersion(String schemeVersion) {
		this.schemeVersion = schemeVersion;
	}
	public int getContextCategoryID() {
		return contextCategoryID;
	}
	public void setContextCategoryID(int contextCategoryID) {
		this.contextCategoryID = contextCategoryID;
	}
	
}
