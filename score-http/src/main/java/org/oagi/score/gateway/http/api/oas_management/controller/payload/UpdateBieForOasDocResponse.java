package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.base.Response;

public class UpdateBieForOasDocResponse extends Response {
    private final OasDocId oasDocId;
    private final boolean changed;

    public UpdateBieForOasDocResponse(OasDocId oasDocId,  boolean changed) {
        this.oasDocId = oasDocId;
        this.changed = changed;
    }

    public boolean isChanged() {
        return changed;
    }

    public OasDocId getOasDocId() {
        return oasDocId;
    }
}
