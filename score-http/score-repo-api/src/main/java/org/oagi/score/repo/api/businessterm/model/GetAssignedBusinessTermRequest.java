package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetAssignedBusinessTermRequest extends Request {

    private BigInteger assignedBizTermId;
    private String bieType;

    public GetAssignedBusinessTermRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getAssignedBizTermId() {
        return assignedBizTermId;
    }

    public void setAssignedBizTermId(BigInteger assignedBizTermId) {
        this.assignedBizTermId = assignedBizTermId;
    }

    public String getBieType() {
        return bieType;
    }

    public void setBieType(String bieType) {
        this.bieType = bieType;
    }

    public GetAssignedBusinessTermRequest withAssignedBizTermId(BigInteger assignedBizTermId) {
        this.setAssignedBizTermId(assignedBizTermId);
        return this;
    }

    public GetAssignedBusinessTermRequest withBieType(String bieType) {
        this.setBieType(bieType);
        return this;
    }
}
