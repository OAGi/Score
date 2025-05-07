package org.oagi.score.gateway.http.api.bie_management.repository.criteria;

import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

public record BiePackageListFilterCriteria(
        LibraryId libraryId,
        String versionId, String versionName, String description,
        String den, String businessTerm, String version, String remark,
        Collection<BieState> states, Collection<ReleaseId> releaseIds,
        Collection<BiePackageId> biePackageIds,
        Collection<String> ownerLoginIdList,
        Collection<String> updaterLoginIdList,
        DateRangeCriteria lastUpdatedTimestampRange) {

}
