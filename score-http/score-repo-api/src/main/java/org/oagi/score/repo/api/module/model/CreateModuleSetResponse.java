package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class CreateModuleSetResponse extends Response {

    private final ModuleSet moduleSet;

    private BigInteger rootModuleId;

    public BigInteger getRootModuleId() {
        return rootModuleId;
    }

    public void setRootModuleId(BigInteger rootModuleId) {
        this.rootModuleId = rootModuleId;
    }

    public CreateModuleSetResponse(ModuleSet moduleSet) {
        this.moduleSet = moduleSet;
    }

    public ModuleSet getModuleSet() {
        return moduleSet;
    }
}
