package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.base.Auditable;

import java.math.BigInteger;

public class AddBieForOasDocResponse extends Auditable {

    private final BigInteger oasRequestId;
    private final BigInteger oasResponseId;

    public AddBieForOasDocResponse(BigInteger oasRequestId, BigInteger oasResponseId) {
        this.oasRequestId = oasRequestId;
        this.oasResponseId = oasResponseId;
    }

    public BigInteger getOasRequestId() {
        return oasRequestId;
    }

    public BigInteger getOasResponseId() {
        return oasResponseId;
    }
}
