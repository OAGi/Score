package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasResourceId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

public class GetOasOperationRequest extends Request {
    private OasResourceId oasResourceId;
    public GetOasOperationRequest(ScoreUser requester){
        super(requester);
    }

    public OasResourceId getOasResourceId() {
        return oasResourceId;
    }

    public void setOasResourceId(OasResourceId oasResourceId) {
        this.oasResourceId = oasResourceId;
    }

    public GetOasOperationRequest withOasResourceId(OasResourceId oasResourceId){
        this.setOasResourceId(oasResourceId);
        return this;
    }
}
