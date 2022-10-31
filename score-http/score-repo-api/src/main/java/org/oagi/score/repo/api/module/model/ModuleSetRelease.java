package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;

public class ModuleSetRelease extends Auditable implements Comparable<ModuleSetRelease>, Serializable {

    private BigInteger moduleSetReleaseId;

    private BigInteger moduleSetId;

    private String moduleSetReleaseName;

    private String moduleSetReleaseDescription;

    private String moduleSetName;

    private BigInteger releaseId;

    private String releaseNum;

    private boolean isDefault;

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

    public String getModuleSetName() {
        return moduleSetName;
    }

    public void setModuleSetName(String moduleSetName) {
        this.moduleSetName = moduleSetName;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public int compareTo(ModuleSetRelease o) {
        return this.moduleSetReleaseId.compareTo(o.getModuleSetReleaseId());
    }
}
