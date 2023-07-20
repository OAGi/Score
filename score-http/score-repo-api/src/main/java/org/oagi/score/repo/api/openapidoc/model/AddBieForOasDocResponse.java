package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.base.PaginationResponse;
import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

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
