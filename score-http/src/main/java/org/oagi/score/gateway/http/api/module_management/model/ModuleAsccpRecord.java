package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleAsccpRecord(
        ModuleAsccpManifestId moduleAsccpManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        AsccpManifestId asccpManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
