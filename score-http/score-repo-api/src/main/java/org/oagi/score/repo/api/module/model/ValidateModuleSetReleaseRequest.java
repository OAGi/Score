package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class ValidateModuleSetReleaseRequest extends Request {

    private BigInteger moduleSetReleaseId;

    public ValidateModuleSetReleaseRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getModuleSetReleaseId() {
        return moduleSetReleaseId;
    }

    public void setModuleSetReleaseId(BigInteger moduleSetReleaseId) {
        this.moduleSetReleaseId = moduleSetReleaseId;
    }


}
