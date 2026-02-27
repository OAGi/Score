package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

public class GetAssignedOasTagRequest extends Request {

    private OasOperationId oasOperationId;
    private String messageBodyType;

    public GetAssignedOasTagRequest(ScoreUser requester) {
        super(requester);
    }

    public GetAssignedOasTagRequest withOasOperationId(OasOperationId oasOperationId) {
        this.setOasOperationId(oasOperationId);
        return this;
    }

    public GetAssignedOasTagRequest withMessageBodyType(String messageBodyType) {
        this.setMessageBodyType(messageBodyType);
        return this;
    }

    public OasOperationId getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(OasOperationId oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public String getMessageBodyType() {
        return messageBodyType;
    }

    public void setMessageBodyType(String messageBodyType) {
        this.messageBodyType = messageBodyType;
    }
}
