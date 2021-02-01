package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetBiePackageRequest extends Request {

    private BigInteger topLevelAsbiepId;
    private boolean used;

    public GetBiePackageRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public GetBiePackageRequest withTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        setTopLevelAsbiepId(topLevelAsbiepId);
        return this;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public GetBiePackageRequest withUsed(boolean used) {
        setUsed(used);
        return this;
    }

}
