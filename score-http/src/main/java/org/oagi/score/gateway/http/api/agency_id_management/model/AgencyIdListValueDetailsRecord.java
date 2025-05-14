package org.oagi.score.gateway.http.api.agency_id_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record AgencyIdListValueDetailsRecord(
        AgencyIdListValueManifestId agencyIdListValueManifestId,
        AgencyIdListValueId agencyIdListValueId,
        AgencyIdListManifestId agencyIdListManifestId,
        Guid guid,

        String value,
        String name,
        Definition definition,
        boolean deprecated,
        boolean isDeveloperDefault, boolean isUserDefault,
        boolean used,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        AgencyIdListValueManifestId prevAgencyIdListValueManifestId,
        AgencyIdListValueManifestId nextAgencyIdListValueManifestId) {

}
