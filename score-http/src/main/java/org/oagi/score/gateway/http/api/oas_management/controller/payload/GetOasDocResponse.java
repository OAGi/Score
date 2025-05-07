package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDoc;
import org.oagi.score.gateway.http.common.model.base.Response;

public class GetOasDocResponse extends Response {
    private final OasDoc oasDoc;

    public GetOasDocResponse(OasDoc oasDoc) {
        this.oasDoc = oasDoc;
    }

    public final OasDoc getOasDoc() {
        return oasDoc;
    }
}
