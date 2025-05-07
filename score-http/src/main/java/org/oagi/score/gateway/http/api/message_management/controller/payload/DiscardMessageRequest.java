package org.oagi.score.gateway.http.api.message_management.controller.payload;

import org.oagi.score.gateway.http.api.message_management.model.MessageId;

import java.util.Collection;

public record DiscardMessageRequest(
        Collection<MessageId> messageIdList) {

}
