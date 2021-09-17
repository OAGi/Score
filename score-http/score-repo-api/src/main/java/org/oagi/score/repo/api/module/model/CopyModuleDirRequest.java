package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CopyModuleDirRequest extends Request {

    public BigInteger getModuleDirId() {
        return moduleDirId;
    }

    public void setModuleDirId(BigInteger moduleDirId) {
        this.moduleDirId = moduleDirId;
    }

    private BigInteger moduleDirId;

    public Boolean getCopySubModules() {
        return copySubModules;
    }

    public void setCopySubModules(Boolean copySubModules) {
        this.copySubModules = copySubModules;
    }

    private Boolean copySubModules;

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    private BigInteger moduleSetId;

    private BigInteger copyPosDirId;

    public BigInteger getCopyPosDirId() {
        return copyPosDirId;
    }

    public void setCopyPosDirId(BigInteger copyPosDirId) {
        this.copyPosDirId = copyPosDirId;
    }
}
