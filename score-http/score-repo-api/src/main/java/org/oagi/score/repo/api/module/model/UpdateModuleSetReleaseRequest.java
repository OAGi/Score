package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class UpdateModuleSetReleaseRequest extends Request {

    private BigInteger moduleSetReleaseId;

    private BigInteger moduleSetId;

    private BigInteger releaseId;

    private String moduleSetReleaseName;

    private boolean isDefault;

    public BigInteger getModuleSetReleaseId() {
        return moduleSetReleaseId;
    }

    public void setModuleSetReleaseId(BigInteger moduleSetReleaseId) {
        this.moduleSetReleaseId = moduleSetReleaseId;
    }

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

    public String getModuleSetReleaseName() {
        return moduleSetReleaseName;
    }

    public void setModuleSetReleaseName(String moduleSetReleaseName) {
        this.moduleSetReleaseName = moduleSetReleaseName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public UpdateModuleSetReleaseRequest(ScoreUser requester) {
        super(requester);
    }
}
