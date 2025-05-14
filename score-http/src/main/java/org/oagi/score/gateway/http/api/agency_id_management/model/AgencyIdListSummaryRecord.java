package org.oagi.score.gateway.http.api.agency_id_management.model;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.List;

public record AgencyIdListSummaryRecord(
        AgencyIdListManifestId agencyIdListManifestId,
        AgencyIdListId agencyIdListId,
        Guid guid, String enumTypeGuid,
        String name, String listId, String versionId,
        Definition definition,
        NamespaceId namespaceId,
        @Nullable AgencyIdListValueManifestId agencyIdListValueManifestId,
        @Nullable String agencyIdListValueName,
        boolean deprecated,
        CcState state,

        UserSummaryRecord owner,

        AgencyIdListManifestId prevAgencyIdListManifestId,
        AgencyIdListManifestId nextAgencyIdListManifestId,

        List<AgencyIdListValueSummaryRecord> valueList) {
}
