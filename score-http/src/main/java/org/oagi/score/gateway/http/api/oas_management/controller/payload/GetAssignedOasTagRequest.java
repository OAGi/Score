package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.math.BigInteger;

public class GetAssignedOasTagRequest extends Request {
    private BigInteger oasOperationId;
    private String messageBodyType;

    public GetAssignedOasTagRequest(ScoreUser requester) {
        super(requester);
    }

    public GetAssignedOasTagRequest withOasOperationId(BigInteger oasOperationId) {
        this.setOasOperationId(oasOperationId);
        return this;
    }

    public GetAssignedOasTagRequest withMessageBodyType(String messageBodyType) {
        this.setMessageBodyType(messageBodyType);
        return this;
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(BigInteger oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public String getMessageBodyType() {
        return messageBodyType;
    }

    public void setMessageBodyType(String messageBodyType) {
        this.messageBodyType = messageBodyType;
    }
}
