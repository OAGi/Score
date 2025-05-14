package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleXbtRecord(
        ModuleXbtManifestId moduleXbtManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        XbtManifestId xbtManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
