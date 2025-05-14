package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record TopLevelAsbiepSummaryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        TopLevelAsbiepId topLevelAsbiepId,
        TopLevelAsbiepId basedTopLevelAsbiepId,
        AsbiepId asbiepId,
        Guid guid,

        String den,
        String propertyTerm,
        String displayName,
        String version,
        String status,
        BieState state,
        boolean deprecated,
        boolean inverseMode,

        UserSummaryRecord owner,

        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
