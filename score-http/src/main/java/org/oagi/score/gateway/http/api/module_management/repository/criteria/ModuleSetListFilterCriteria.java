package org.oagi.score.gateway.http.api.module_management.repository.criteria;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

public record ModuleSetListFilterCriteria(LibraryId libraryId,
                                          String name, String description,
                                          Collection<String> updaterLoginIdSet,
                                          DateRangeCriteria lastUpdatedTimestampRange) {
}
