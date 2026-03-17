package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

public class GetOasResponseTableRequest extends Request {

    private OasOperationId oasOperationId;

    public GetOasResponseTableRequest(ScoreUser requester) {
        super(requester);
    }

    public OasOperationId getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(OasOperationId oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public GetOasResponseTableRequest withOasOperationId(OasOperationId oasOperationId) {
        this.setOasOperationId(oasOperationId);
        return this;
    }

}
