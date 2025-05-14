package org.oagi.score.gateway.http.api.agency_id_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;

public record CreateAgencyIdListResponse(AgencyIdListManifestId agencyIdListManifestId,
                                         String status, String statusMessage) {
}
