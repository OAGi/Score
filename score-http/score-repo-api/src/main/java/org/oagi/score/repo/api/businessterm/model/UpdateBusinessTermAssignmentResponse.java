package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateBusinessTermAssignmentResponse extends Response {

    private final BigInteger assignedBizTermId;
    private final String bieType;
    private final boolean changed;

    public UpdateBusinessTermAssignmentResponse(BigInteger assignedBizTermId, String bieType, boolean changed) {
        this.assignedBizTermId = assignedBizTermId;
        this.bieType = bieType;
        this.changed = changed;
    }

    public BigInteger getAssignedBizTermId() {
        return assignedBizTermId;
    }

    public String getBieType() {
        return bieType;
    }

    public boolean isChanged() {
        return changed;
    }
}
