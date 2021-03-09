package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

public class GetModuleSetReleaseResponse extends Response {

    private final ModuleSetRelease moduleSetRelease;

    public GetModuleSetReleaseResponse(ModuleSetRelease moduleSetRelease) {
        this.moduleSetRelease = moduleSetRelease;
    }

    public ModuleSetRelease getModuleSetRelease() {
        return moduleSetRelease;
    }
}
