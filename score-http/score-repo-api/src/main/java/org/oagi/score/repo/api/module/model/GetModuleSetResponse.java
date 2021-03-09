package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

public class GetModuleSetResponse extends Response {

    private final ModuleSet moduleSet;

    public GetModuleSetResponse(ModuleSet moduleSet) {
        this.moduleSet = moduleSet;
    }

    public ModuleSet getModuleSet() {
        return moduleSet;
    }
}
