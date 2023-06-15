package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;

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
