package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CopyModuleRequest extends Request {

    private BigInteger moduleSetId;

    private BigInteger targetModuleId;

    public boolean isCopySubModules() {
        return copySubModules;
    }

    public void setCopySubModules(boolean copySubModules) {
        this.copySubModules = copySubModules;
    }

    private boolean copySubModules;

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public BigInteger getTargetModuleId() {
        return targetModuleId;
    }

    public void setTargetModuleId(BigInteger targetModuleId) {
        this.targetModuleId = targetModuleId;
    }

    public BigInteger getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(BigInteger parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    private BigInteger parentModuleId;

    public CopyModuleRequest() {
        super();
    }

    @Override
    public ScoreUser getRequester() {
        return requester;
    }

    @Override
    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    private ScoreUser requester;
}
