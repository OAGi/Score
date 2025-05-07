package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleAgencyIdListRecord(
        ModuleAgencyIdListManifestId moduleAgencyIdListManifestId,
        ModuleSetReleaseId moduleSetReleaseId,
        AgencyIdListManifestId agencyIdListManifestId,
        ModuleId moduleId,
        String modulePath,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
