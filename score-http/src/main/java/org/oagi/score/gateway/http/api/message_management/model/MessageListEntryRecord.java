package org.oagi.score.gateway.http.api.message_management.model;

import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record MessageListEntryRecord(MessageId messageId,
                                     String subject,
                                     String body,
                                     boolean read,
                                     WhoAndWhen created) {
}
