package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

public class OasDoc extends Auditable {
    private BigInteger oasDocId;
    private String guid;
    private String openAPIVersion;
    private String title;
    private String description;
    private String termsOfService;
    private String version;
    private String contactName;
    private String contactUrl;
    private String contactEmail;
    private String licenseName;
    private String licenseUrl;
    private String ownerUserId;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private ScoreUser createdBy;
    private ScoreUser lastUpdatedBy;

    public OasDoc() {
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getOpenAPIVersion() {
        return openAPIVersion;
    }

    public void setOpenAPIVersion(String openAPIVersion) {
        this.openAPIVersion = openAPIVersion;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTermsOfService() {
        return termsOfService;
    }

    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    @Override
    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    @Override
    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    @Override
    public ScoreUser getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(ScoreUser createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public ScoreUser getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Override
    public void setLastUpdatedBy(ScoreUser lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public OasDoc(BigInteger oasDocId, String guid, String openAPIVersion, String title, String description, String termsOfService, String version, String contactName, String contactUrl, String contactEmail, String licenseName, String licenseUrl, Date lastUpdateTimestamp, Date creationTimestamp, ScoreUser createdBy, ScoreUser lastUpdatedBy) {
        this.oasDocId = oasDocId;
        this.guid = guid;
        this.openAPIVersion = openAPIVersion;
        this.title = title;
        this.description = description;
        this.termsOfService = termsOfService;
        this.version = version;
        this.contactName = contactName;
        this.contactUrl = contactUrl;
        this.contactEmail = contactEmail;
        this.licenseName = licenseName;
        this.licenseUrl = licenseUrl;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.creationTimestamp = creationTimestamp;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public String toString() {
        return "OasDoc{" +
                "oasDocId=" + oasDocId +
                ", guid='" + guid + '\'' +
                ", openAPIVersion='" + openAPIVersion + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", termsOfService='" + termsOfService + '\'' +
                ", version='" + version + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactUrl='" + contactUrl + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", licenseName='" + licenseName + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
