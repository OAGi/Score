package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.base.Response;

import java.math.BigInteger;
import java.util.List;

public class DeleteOasDocResponse extends Response {
    private final List<BigInteger> oasDocIdList;

    public DeleteOasDocResponse(List<BigInteger> oasDocIdList) {
        this.oasDocIdList = oasDocIdList;
    }

    public List<BigInteger> getOasDocIdList() {
        return oasDocIdList;
    }

    public boolean contains(BigInteger oasDocId) {
        return this.oasDocIdList.contains(oasDocId);
    }

    public boolean containsAll(List<BigInteger> oasDocIdList) {
        for (BigInteger oasDocId : oasDocIdList) {
            if (!this.oasDocIdList.contains(oasDocId)) {
                return false;
            }
        }
        return true;
    }
}
