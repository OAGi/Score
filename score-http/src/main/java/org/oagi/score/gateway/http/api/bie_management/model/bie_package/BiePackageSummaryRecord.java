package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record BiePackageSummaryRecord(
        BiePackageId biePackageId,
        Guid guid,
        LibraryId libraryId,
        String name,
        String versionId,
        String versionName,
        String description,
        String revisionReason,
        List<ReleaseSummaryRecord> releases,
        BieState state,
        UserSummaryRecord owner,
        BiePackageId prevBiePackageId) {

    public Guid versionGuid() {
        return new Guid(UUID.nameUUIDFromBytes(
                String.join("/", Arrays.asList(this.name, this.versionName)).getBytes()
        ).toString().replace("-", ""));
    }

}
