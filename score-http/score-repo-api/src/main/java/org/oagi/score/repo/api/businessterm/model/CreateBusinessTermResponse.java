package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Auditable;

import java.math.BigInteger;

public class CreateBusinessTermResponse extends Auditable {

    private final BigInteger businessTermId;

    public CreateBusinessTermResponse(BigInteger businessTermId) {
        this.businessTermId = businessTermId;
    }

    public BigInteger getBusinessTermId() {
        return businessTermId;
    }

}
