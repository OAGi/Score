package org.oagi.score.gateway.http.api.message_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record MessageDetailsRecord(MessageId messageId,
                                   String subject,
                                   String body,
                                   String bodyContentType,
                                   boolean read,
                                   UserSummaryRecord recipient,
                                   WhoAndWhen created) {
}
