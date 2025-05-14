package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.List;

public record BiePackageDetailsRecord(
        BiePackageId biePackageId,
        LibraryId libraryId,
        String versionId,
        String versionName,
        String description,
        List<ReleaseSummaryRecord> releases,
        BieState state,
        AccessPrivilege access,

        BiePackageSummaryRecord source,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {
}
