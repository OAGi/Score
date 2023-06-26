package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class AddBieForOasDocResponse extends Response {
    private final List<BigInteger> bieForOasDocIdList;

    public AddBieForOasDocResponse(List<BigInteger> bieForOasDocIdList) {
        this.bieForOasDocIdList = bieForOasDocIdList;
    }

    public List<BigInteger> getBieForOasDocIdList() {
        return bieForOasDocIdList;
    }
}
