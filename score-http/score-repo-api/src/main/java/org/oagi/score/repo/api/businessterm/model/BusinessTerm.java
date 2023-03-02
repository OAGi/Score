package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

public class BusinessTerm extends Auditable {

    private BigInteger businessTermId;

    private String businessTerm;

    private String definition;

    private String comment;

    private String externalReferenceUri;

    private String externalReferenceId;

    private String guid;

    private Date lastUpdateTimestamp;

    private ScoreUser createdBy;
    private ScoreUser lastUpdatedBy;


    public BigInteger getBusinessTermId() {
        return businessTermId;
    }

    public void setBusinessTermId(BigInteger businessTermId) {
        this.businessTermId = businessTermId;
    }

    public String getBusinessTerm() {
        return businessTerm;
    }

    public void setBusinessTerm(String businessTerm) {
        this.businessTerm = businessTerm;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExternalReferenceUri() {
        return externalReferenceUri;
    }

    public void setExternalReferenceUri(String externalReferenceUri) {
        this.externalReferenceUri = externalReferenceUri;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }


    public void setCreatedBy(ScoreUser createdBy) {
        this.createdBy = createdBy;
    }

    public ScoreUser getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(ScoreUser lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public ScoreUser getCreatedBy() {
        return createdBy;
    }

    @Override
    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    @Override
    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public BusinessTerm(BigInteger businessTermId, String businessTerm, String definition, String comment, String externalReferenceUri, String externalReferenceId, String guid, Date lastUpdateTimestamp, ScoreUser createdBy, ScoreUser lastUpdatedBy) {
        this.businessTermId = businessTermId;
        this.businessTerm = businessTerm;
        this.definition = definition;
        this.comment = comment;
        this.externalReferenceUri = externalReferenceUri;
        this.externalReferenceId = externalReferenceId;
        this.guid = guid;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public BusinessTerm() {
    }

    @Override
    public String toString() {
        return "BusinessTerm{" +
                "businessTermId=" + businessTermId +
                ", businessTerm='" + businessTerm + '\'' +
                ", definition='" + definition + '\'' +
                ", comment='" + comment + '\'' +
                ", externalReferenceUri='" + externalReferenceUri + '\'' +
                ", externalReferenceId='" + externalReferenceId + '\'' +
                ", guid='" + guid + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
