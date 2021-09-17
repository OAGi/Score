package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CreateModuleRequest extends Request {

    private BigInteger parentModuleId;

    private String name;

    private BigInteger namespaceId;

    private String versionNum;

    private BigInteger moduleSetId;

    private ModuleType moduleType;

    public BigInteger getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(BigInteger parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(BigInteger namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public CreateModuleRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }
}
