package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

public class CreateBusinessTermRequest extends Request {

    private String businessTerm;

    private String comment;

    private String definition;

    private String externalReferenceUri;

    private String externalReferenceId;

    public CreateBusinessTermRequest(ScoreUser requester) {
        super(requester);
    }

    public CreateBusinessTermRequest() {
    }

    public String getBusinessTerm() {
        return businessTerm;
    }

    public void setBusinessTerm(String businessTerm) {
        this.businessTerm = businessTerm;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
}
