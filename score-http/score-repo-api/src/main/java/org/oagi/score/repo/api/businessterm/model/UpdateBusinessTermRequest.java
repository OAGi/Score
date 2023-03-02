package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

public class UpdateBusinessTermRequest extends Request {

    private String businessTermId;

    private String businessTerm;

    private String definition;

    private String externalReferenceUri;

    private String externalReferenceId;

    private String comment;

    public UpdateBusinessTermRequest(ScoreUser requester) {
        super(requester);
    }

    public String getBusinessTermId() {
        return businessTermId;
    }

    public void setBusinessTermId(String businessTermId) {
        this.businessTermId = businessTermId;
    }

    public UpdateBusinessTermRequest withBusinessTermId(String businessTermId) {
        this.setBusinessTermId(businessTermId);
        return this;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "UpdateBusinessTermRequest{" +
                "businessTermId=" + businessTermId +
                ", businessTerm='" + businessTerm + '\'' +
                ", definition='" + definition + '\'' +
                ", externalReferenceUri='" + externalReferenceUri + '\'' +
                ", externalReferenceId='" + externalReferenceId + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
