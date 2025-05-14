package org.oagi.score.gateway.http.api.agency_id_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.List;

public record AgencyIdListDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        AgencyIdListManifestId agencyIdListManifestId,
        AgencyIdListId agencyIdListId,
        Guid guid,
        String enumTypeGuid,
        AgencyIdListSummaryRecord based,
        String name, String listId, String versionId,
        AgencyIdListValueSummaryRecord agencyIdListValue,
        Definition definition,
        String remark,
        NamespaceSummaryRecord namespace,
        boolean deprecated,
        boolean newComponent,
        CcState state,
        AccessPrivilege access,

        LogSummaryRecord log,
        String module,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        AgencyIdListManifestId prevAgencyIdListManifestId,
        AgencyIdListManifestId nextAgencyIdListManifestId,

        List<AgencyIdListValueDetailsRecord> valueList) {

}
