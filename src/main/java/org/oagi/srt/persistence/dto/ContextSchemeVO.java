package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ContextSchemeVO extends SRTObject implements Serializable {

	private static final long serialVersionUID = -3353563030747879464L;
	
	private int contextSchemeID;
	private String schemeID;
	private String schemeGUID;
	private String schemeName;
	private String description;
	private String schemeAgencyID;
	private String schemeAgencyName;
	private String schemeVersion;
	private int contextCategoryID;
	private String contextCategoryStr;
	private int createdByUserId;
	private int lastUpdatedByUserId;
	private Timestamp CreationTimestamp;
	private Timestamp LastUpdateTimestamp;
	private String Classification_ctx_schemecol;
	
	public int getLastUpdatedByUserId() {
		return lastUpdatedByUserId;
	}
	public void setLastUpdatedByUserId(int lastUpdatedByUserId) {
		this.lastUpdatedByUserId = lastUpdatedByUserId;
	}
	public int getCreatedByUserId() {
		return createdByUserId;
	}
	public void setCreatedByUserId(int createdByUserId) {
		this.createdByUserId = createdByUserId;
	}
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
	public String getSchemeGUID() {
		return schemeGUID;
	}
	public void setSchemeGUID(String schemeGUID) {
		this.schemeGUID = schemeGUID;
	}
	public String getContextCategoryStr() {
		return contextCategoryStr;
	}
	public void setContextCategoryStr(String contextCategoryStr) {
		this.contextCategoryStr = contextCategoryStr;
	}
	public Timestamp getCreationTimestamp() {
		return CreationTimestamp;
	}
	public void setCreationTimestamp(Timestamp creationTimestamp) {
		CreationTimestamp = creationTimestamp;
	}
	public Timestamp getLastUpdateTimestamp() {
		return LastUpdateTimestamp;
	}
	public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {
		LastUpdateTimestamp = lastUpdateTimestamp;
	}
	public String getClassification_ctx_schemecol() {
		return Classification_ctx_schemecol;
	}
	public void setClassification_ctx_schemecol(String classification_ctx_schemecol) {
		Classification_ctx_schemecol = classification_ctx_schemecol;
	}
	
}
