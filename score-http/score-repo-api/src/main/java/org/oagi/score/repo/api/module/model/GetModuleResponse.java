package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

public class GetModuleResponse extends Response {

    private final Module module;

    public GetModuleResponse(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}
