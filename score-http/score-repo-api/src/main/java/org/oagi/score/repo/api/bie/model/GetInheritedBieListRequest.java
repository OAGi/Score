package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetInheritedBieListRequest extends Request {

    private BigInteger topLevelAsbiepId;

    public GetInheritedBieListRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public GetInheritedBieListRequest withTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        setTopLevelAsbiepId(topLevelAsbiepId);
        return this;
    }
}
