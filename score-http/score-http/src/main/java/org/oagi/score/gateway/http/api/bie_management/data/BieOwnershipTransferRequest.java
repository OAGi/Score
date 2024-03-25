package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class BieOwnershipTransferRequest {

    private ScoreUser requester;

    private ScoreUser targetUser;

    private BigInteger biePackageId;

    public BieOwnershipTransferRequest() {
    }

    public BieOwnershipTransferRequest(ScoreUser requester) {
        this.requester = requester;
    }

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public ScoreUser getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(ScoreUser targetUser) {
        this.targetUser = targetUser;
    }

    public BigInteger getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
    }
}
