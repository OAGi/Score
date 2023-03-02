package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class UpdateBusinessTermAssignmentRequest extends Request {

    private BigInteger assignedBizTermId;

    private BigInteger bieId;

    private String bieType;

    private String typeCode;

    private boolean primary;

    public UpdateBusinessTermAssignmentRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getAssignedBizTermId() {
        return assignedBizTermId;
    }

    public void setAssignedBizTermId(BigInteger assignedBizTermId) {
        this.assignedBizTermId = assignedBizTermId;
    }

    public UpdateBusinessTermAssignmentRequest withAssignedBizTermId(BigInteger assignedBizTermId) {
        this.setAssignedBizTermId(assignedBizTermId);
        return this;
    }

    public String getBieType() {
        return bieType;
    }

    public void setBieType(String bieType) {
        this.bieType = bieType;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public BigInteger getBieId() {
        return bieId;
    }

    public void setBieId(BigInteger bieId) {
        this.bieId = bieId;
    }

    @Override
    public String toString() {
        return "UpdateBusinessTermAssignmentRequest{" +
                "assignedBizTermId=" + assignedBizTermId +
                ", bieId=" + bieId +
                ", bieType='" + bieType + '\'' +
                ", typeCode='" + typeCode + '\'' +
                ", primary='" + primary + '\'' +
                '}';
    }
}
