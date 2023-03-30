package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetModuleSetReleaseRequest extends Request {

    private BigInteger moduleSetReleaseId;

    public GetModuleSetReleaseRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getModuleSetReleaseId() {
        return moduleSetReleaseId;
    }

    public void setModuleSetReleaseId(BigInteger moduleSetReleaseId) {
        this.moduleSetReleaseId = moduleSetReleaseId;
    }

    public GetModuleSetReleaseRequest withModuleSetReleaseId(BigInteger moduleSetReleaseId) {
        setModuleSetReleaseId(moduleSetReleaseId);
        return this;
    }

}
