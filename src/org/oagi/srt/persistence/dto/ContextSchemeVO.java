package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ContextSchemeVO extends SRTObject {

	private int ContextSchemeID;
	private String SchemeID;
	private String SchemeName;
	private String Description;
	private String SchemeAgencyID;
	private String SchemeAgencyName;
	private String SchemeVersion;
	private int ContextCategoryID;
	
	public int getContextSchemeID() {
		return ContextSchemeID;
	}
	public void setContextSchemeID(int contextSchemeID) {
		ContextSchemeID = contextSchemeID;
	}
	public String getSchemeID() {
		return SchemeID;
	}
	public void setSchemeID(String schemeID) {
		SchemeID = schemeID;
	}
	public String getSchemeName() {
		return SchemeName;
	}
	public void setSchemeName(String schemeName) {
		SchemeName = schemeName;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public String getSchemeAgencyID() {
		return SchemeAgencyID;
	}
	public void setSchemeAgencyID(String schemeAgencyID) {
		SchemeAgencyID = schemeAgencyID;
	}
	public String getSchemeAgencyName() {
		return SchemeAgencyName;
	}
	public void setSchemeAgencyName(String schemeAgencyName) {
		SchemeAgencyName = schemeAgencyName;
	}
	public String getSchemeVersion() {
		return SchemeVersion;
	}
	public void setSchemeVersion(String schemeVersion) {
		SchemeVersion = schemeVersion;
	}
	public int getContextCategoryID() {
		return ContextCategoryID;
	}
	public void setContextCategoryID(int contextCategoryID) {
		ContextCategoryID = contextCategoryID;
	}
	
}
