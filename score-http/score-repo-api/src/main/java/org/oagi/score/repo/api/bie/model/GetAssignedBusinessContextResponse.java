package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class GetAssignedBusinessContextResponse extends Response {

    private final List<BigInteger> businessContextIdList;

    public GetAssignedBusinessContextResponse(List<BigInteger> businessContextIdList) {
        this.businessContextIdList = businessContextIdList;
    }

    public List<BigInteger> getBusinessContextIdList() {
        return businessContextIdList;
    }
}
