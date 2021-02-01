package org.oagi.score.repo.api.release.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetReleaseRequest extends Request {

    private BigInteger topLevelAsbiepId;

    private BigInteger releaseId;

    private String releaseNum;

    public GetReleaseRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public GetReleaseRequest withReleaseId(BigInteger releaseId) {
        setReleaseId(releaseId);
        return this;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public GetReleaseRequest withTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        setTopLevelAsbiepId(topLevelAsbiepId);
        return this;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

    public GetReleaseRequest withReleaseNum(String releaseNum) {
        setReleaseNum(releaseNum);
        return this;
    }
}
