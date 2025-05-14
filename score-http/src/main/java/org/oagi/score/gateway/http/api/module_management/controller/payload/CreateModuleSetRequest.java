package org.oagi.score.gateway.http.api.module_management.controller.payload;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public record CreateModuleSetRequest(
        LibraryId libraryId,
        String name,
        String description,
        boolean createModuleSetRelease,
        ReleaseId targetReleaseId,
        ModuleSetReleaseId targetModuleSetReleaseId) {

}
