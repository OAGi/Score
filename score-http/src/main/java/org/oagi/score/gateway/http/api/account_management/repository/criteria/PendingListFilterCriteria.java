package org.oagi.score.gateway.http.api.account_management.repository.criteria;

import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

public record PendingListFilterCriteria(String preferredUsername,
                                        String email,
                                        String providerName,
                                        DateRangeCriteria createdTimestampRange) {

}
