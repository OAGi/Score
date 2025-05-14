package org.oagi.score.gateway.http.api.agency_id_management.controller.payload;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;

public record UpdateAgencyIdListValueRequest(
        AgencyIdListValueManifestId agencyIdListValueManifestId,
        String value, String name,
        Definition definition,
        Boolean deprecated, Boolean developerDefault, Boolean userDefault) {

}
