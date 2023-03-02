package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class DeleteBusinessTermResponse extends Response {

    private final List<BigInteger> businessTermIdList;

    public DeleteBusinessTermResponse(List<BigInteger> businessTermIdList) {
        this.businessTermIdList = businessTermIdList;
    }

    public List<BigInteger> getBusinessTermIdList() {
        return businessTermIdList;
    }

    public boolean contains(BigInteger businessTermId) {
        return this.businessTermIdList.contains(businessTermId);
    }

    public boolean containsAll(List<BigInteger> businessTermIdList) {
        for (BigInteger businessTermId : businessTermIdList) {
            if (!this.businessTermIdList.contains(businessTermId)) {
                return false;
            }
        }
        return true;
    }
}
