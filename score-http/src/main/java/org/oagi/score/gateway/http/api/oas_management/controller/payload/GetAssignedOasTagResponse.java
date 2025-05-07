package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasTag;
import org.oagi.score.gateway.http.common.model.base.Response;

public class GetAssignedOasTagResponse extends Response {

    public static GetAssignedOasTagResponse EMPTY_INSTANCE = new GetAssignedOasTagResponse(null);

    private final OasTag oasTag;

    public GetAssignedOasTagResponse(OasTag oasTag) {
        this.oasTag = oasTag;
    }

    public OasTag getOasTag() {
        return oasTag;
    }

}
