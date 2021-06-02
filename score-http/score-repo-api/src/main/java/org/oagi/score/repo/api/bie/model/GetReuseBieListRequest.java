package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetReuseBieListRequest extends Request {

    private BigInteger topLevelAsbiepId;

    private boolean reusedBie;

    public GetReuseBieListRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId, boolean reusedBie) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.reusedBie = reusedBie;
    }

    public GetReuseBieListRequest withTopLevelAsbiepId(BigInteger topLevelAsbiepId, boolean reusedBie) {
        setTopLevelAsbiepId(topLevelAsbiepId, reusedBie);
        return this;
    }

    public boolean isReusedBie() {
        return reusedBie;
    }
}
