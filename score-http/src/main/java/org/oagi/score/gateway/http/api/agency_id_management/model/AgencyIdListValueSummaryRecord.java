package org.oagi.score.gateway.http.api.agency_id_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.common.model.Guid;

public record AgencyIdListValueSummaryRecord(
        AgencyIdListValueManifestId agencyIdListValueManifestId,
        AgencyIdListValueId agencyIdListValueId,
        AgencyIdListManifestId agencyIdListManifestId,
        Guid guid,
        String value,
        String name,
        Definition definition,
        boolean deprecated,
        boolean isDeveloperDefault, boolean isUserDefault) {
}
