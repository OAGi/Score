package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasOperation;
import org.oagi.score.gateway.http.common.model.base.Response;

public class GetOasOperationResponse extends Response {
    private final OasOperation oasOperation;

    public GetOasOperationResponse(OasOperation oasOperation) {
        this.oasOperation = oasOperation;
    }

    public OasOperation getOasOperation() {
        return oasOperation;
    }
}
