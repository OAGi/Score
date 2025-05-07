package org.oagi.score.gateway.http.api.business_term_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.math.BigInteger;
import java.util.List;

public record AsbieBbieListEntryRecord(
        String type,

        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        BigInteger bieId,
        Guid guid,
        TopLevelAsbiepId topLevelAsbiepId,

        String den,
        String propertyTerm,
        String version,
        String status,
        String bizTerm,
        String remark,
        List<BusinessContextSummaryRecord> businessContextList,
        BieState state,
        AccessPrivilege access,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {

    public boolean isOwnedByDeveloper() {
        return owner.isDeveloper();
    }

}
