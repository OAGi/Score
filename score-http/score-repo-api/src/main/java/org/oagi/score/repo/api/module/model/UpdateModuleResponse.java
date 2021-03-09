package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateModuleResponse extends Response {

    private final Module module;

    public UpdateModuleResponse(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}
