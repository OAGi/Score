package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.base.Response;

import java.math.BigInteger;

public class UpdateOasDocResponse extends Response {
    private final BigInteger oasDocId;
    private final boolean changed;

    public UpdateOasDocResponse(BigInteger oasDocId, boolean changed) {
        this.oasDocId = oasDocId;
        this.changed = changed;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public boolean isChanged() {
        return changed;
    }
}
