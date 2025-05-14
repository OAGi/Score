package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleCodeListRecord(
        ModuleCodeListManifestId moduleCodeListManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        CodeListManifestId codeListManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
