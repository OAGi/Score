package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

public class UpdateModuleSetReleaseResponse extends Response {

    private final ModuleSetRelease moduleSetRelease;

    public UpdateModuleSetReleaseResponse(ModuleSetRelease moduleSetRelease) {
        this.moduleSetRelease = moduleSetRelease;
    }

    public ModuleSetRelease getModuleSetRelease() {
        return moduleSetRelease;
    }
}
