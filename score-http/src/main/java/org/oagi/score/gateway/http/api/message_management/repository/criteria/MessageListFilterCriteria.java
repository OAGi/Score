package org.oagi.score.gateway.http.api.message_management.repository.criteria;

import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

public record MessageListFilterCriteria(String subject,
                                        Collection<String> senderLoginIdSet,
                                        DateRangeCriteria createdTimestampRange) {

}
