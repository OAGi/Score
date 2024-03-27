package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class UpliftBiePackageRequest {

    private ScoreUser requester;

    private BigInteger biePackageId;

    private BigInteger targetReleaseId;

    public UpliftBiePackageRequest() {

    }

    public UpliftBiePackageRequest(ScoreUser requester) {
        setRequester(requester);
    }

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public BigInteger getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
    }

    public BigInteger getTargetReleaseId() {
        return targetReleaseId;
    }

    public void setTargetReleaseId(BigInteger targetReleaseId) {
        this.targetReleaseId = targetReleaseId;
    }
}
