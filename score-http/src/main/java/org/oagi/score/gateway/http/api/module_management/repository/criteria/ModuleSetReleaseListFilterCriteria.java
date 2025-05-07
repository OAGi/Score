package org.oagi.score.gateway.http.api.module_management.repository.criteria;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

public record ModuleSetReleaseListFilterCriteria(LibraryId libraryId, ReleaseId releaseId,
                                                 String name, Boolean isDefault,
                                                 Collection<String> updaterLoginIdSet,
                                                 DateRangeCriteria lastUpdatedTimestampRange) {
}
