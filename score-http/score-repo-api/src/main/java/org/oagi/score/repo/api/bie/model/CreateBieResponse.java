package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class CreateBieResponse extends Response {

    private BigInteger topLevelAsbiepId;

    public CreateBieResponse(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

}
