package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.List;

public record BieListEntryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        TopLevelAsbiepId topLevelAsbiepId,
        AsbiepId asbiepId,
        Guid guid,

        String den,
        String propertyTerm,
        String displayName,
        String version,
        String status,
        String bizTerm,
        String remark,
        List<BusinessContextSummaryRecord> businessContextList,
        BieState state,
        AccessPrivilege access,

        boolean deprecated,
        String deprecatedReason,
        String deprecatedRemark,

        SourceTopLevelAsbiepRecord source,
        SourceTopLevelAsbiepRecord based,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {

    public boolean isOwnedByDeveloper() {
        return owner.isDeveloper();
    }

}
