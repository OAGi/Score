package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

public class GetModuleSetMetadataResponse extends Response {

    private final ModuleSetMetadata moduleSetMetadata;

    public GetModuleSetMetadataResponse(ModuleSetMetadata moduleSetMetadata) {
        this.moduleSetMetadata = moduleSetMetadata;
    }

    public ModuleSetMetadata getModuleSetMetadata() {
        return moduleSetMetadata;
    }
}
