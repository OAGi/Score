package org.oagi.score.gateway.http.api.agency_id_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;

import java.util.List;

public record UpdateAgencyIdListManifestWithMultipleIdListRequest(
        List<AgencyIdListManifestId> agencyIdListManifestIds) {
}
