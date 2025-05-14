package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasRequest;
import org.oagi.score.gateway.http.common.model.base.Response;

public class GetOasRequestTableResponse extends Response {
    private final OasRequest oasRequest;

    public GetOasRequestTableResponse(OasRequest oasRequest) {
        this.oasRequest = oasRequest;
    }

    public OasRequest getOasRequestTable() {
        return oasRequest;
    }
}
