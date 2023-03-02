package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Auditable;

import java.math.BigInteger;
import java.util.List;

public class CreateBulkBusinessTermResponse extends Auditable {

    private final List<BigInteger> businessTermIds;

    public CreateBulkBusinessTermResponse(List<BigInteger> businessTermIds) {
        this.businessTermIds = businessTermIds;
    }

    public List<BigInteger> getBusinessTermIds() {
        return businessTermIds;
    }
}
