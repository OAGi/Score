package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CreateModuleSetReleaseRequest extends Request {

    private BigInteger moduleSetId;

    private BigInteger releaseId;

    private String moduleSetReleaseName;

    private String moduleSetReleaseDescription;

    private boolean isDefault;

    private BigInteger baseModuleSetReleaseId;

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

    public String getModuleSetReleaseDescription() {
        return moduleSetReleaseDescription;
    }

    public void setModuleSetReleaseDescription(String moduleSetReleaseDescription) {
        this.moduleSetReleaseDescription = moduleSetReleaseDescription;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public CreateModuleSetReleaseRequest() {
        super();
    }

    public BigInteger getBaseModuleSetReleaseId() {
        return baseModuleSetReleaseId;
    }

    public void setBaseModuleSetReleaseId(BigInteger baseModuleSetReleaseId) {
        this.baseModuleSetReleaseId = baseModuleSetReleaseId;
    }
}
