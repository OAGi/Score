package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasResponse;
import org.oagi.score.gateway.http.common.model.base.Response;

public class GetOasResponseTableResponse extends Response {
    private final OasResponse oasResponse;

    public GetOasResponseTableResponse(OasResponse oasResponse) {
        this.oasResponse = oasResponse;
    }

    public OasResponse getOasResponseTable() {
        return oasResponse;
    }
}
