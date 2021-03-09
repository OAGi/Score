package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;

import java.math.BigInteger;

public class DeleteModuleRequest extends Request {

    public BigInteger getModuleId() {
        return moduleId;
    }

    public void setModuleId(BigInteger moduleId) {
        this.moduleId = moduleId;
    }

    private BigInteger moduleId;
}
