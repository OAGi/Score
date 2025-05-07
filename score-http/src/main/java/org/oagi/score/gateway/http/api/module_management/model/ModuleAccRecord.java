package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleAccRecord(
        ModuleAccManifestId moduleAccManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        AccManifestId accManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
