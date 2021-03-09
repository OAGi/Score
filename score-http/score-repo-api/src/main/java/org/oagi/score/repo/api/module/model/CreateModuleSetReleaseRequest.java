package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CreateModuleSetReleaseRequest extends Request {

    private BigInteger moduleSetId;

    private BigInteger releaseId;

    private boolean isDefault;

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public CreateModuleSetReleaseRequest(ScoreUser requester) {
        super(requester);
    }
}
