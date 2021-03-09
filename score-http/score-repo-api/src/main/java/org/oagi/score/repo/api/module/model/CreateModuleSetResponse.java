package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

public class CreateModuleSetResponse extends Response {

    private final ModuleSet moduleSet;

    public CreateModuleSetResponse(ModuleSet moduleSet) {
        this.moduleSet = moduleSet;
    }

    public ModuleSet getModuleSet() {
        return moduleSet;
    }
}
