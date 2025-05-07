package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleDtRecord(
        ModuleDtManifestId moduleDtManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        DtManifestId dtManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
