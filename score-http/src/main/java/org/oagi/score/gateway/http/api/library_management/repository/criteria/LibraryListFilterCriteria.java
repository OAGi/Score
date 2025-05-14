package org.oagi.score.gateway.http.api.library_management.repository.criteria;

import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

public record LibraryListFilterCriteria(String type, String name, String organization, String description,
                                        String domain, String state,
                                        Collection<String> updaterLoginIdSet,
                                        DateRangeCriteria lastUpdatedTimestampRange) {

}
