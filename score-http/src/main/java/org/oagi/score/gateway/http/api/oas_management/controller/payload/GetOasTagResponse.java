package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasTag;
import org.oagi.score.gateway.http.common.model.base.Response;

public class GetOasTagResponse extends Response {
    private final OasTag oasTag;
    public GetOasTagResponse(OasTag oasTag) {
        this.oasTag = oasTag;
    }
    public OasTag getOasTag() {
        return oasTag;
    }
}
