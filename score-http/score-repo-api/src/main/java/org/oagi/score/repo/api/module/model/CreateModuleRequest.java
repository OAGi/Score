package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.businesscontext.model.BusinessContextValue;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Collection;

public class CreateModuleRequest extends Request {

    private BigInteger moduleDirId;

    private String name;

    private BigInteger namespaceId;

    private String versionNum;

    public BigInteger getModuleDirId() {
        return moduleDirId;
    }

    public void setModuleDirId(BigInteger moduleDirId) {
        this.moduleDirId = moduleDirId;
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
}
