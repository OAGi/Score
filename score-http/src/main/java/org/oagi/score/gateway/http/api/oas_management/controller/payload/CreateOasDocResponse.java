package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.base.Auditable;

import java.math.BigInteger;

public class CreateOasDocResponse extends Auditable {
    private final BigInteger oasDocId;

    public CreateOasDocResponse(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }
}
