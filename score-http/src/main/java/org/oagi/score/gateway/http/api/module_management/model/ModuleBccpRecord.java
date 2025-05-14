package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleBccpRecord(
        ModuleBccpManifestId moduleBccpManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        BccpManifestId bccpManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
