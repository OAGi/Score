package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasRequestId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResponseId;
import org.oagi.score.gateway.http.common.model.base.Auditable;

public class AddBieForOasDocResponse extends Auditable {

    private final OasRequestId oasRequestId;
    private final OasResponseId oasResponseId;

    public AddBieForOasDocResponse(OasRequestId oasRequestId, OasResponseId oasResponseId) {
        this.oasRequestId = oasRequestId;
        this.oasResponseId = oasResponseId;
    }

    public OasRequestId getOasRequestId() {
        return oasRequestId;
    }

    public OasResponseId getOasResponseId() {
        return oasResponseId;
    }
}
