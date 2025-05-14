package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.base.Response;

import java.math.BigInteger;

public class UpdateBieForOasDocResponse extends Response {
    private final BigInteger oasDocId;
    private final boolean changed;

    public UpdateBieForOasDocResponse(BigInteger oasDocId,  boolean changed) {
        this.oasDocId = oasDocId;
        this.changed = changed;
    }
    public boolean isChanged() {
        return changed;
    }
    public BigInteger getOasDocId() {
        return oasDocId;
    }
}
