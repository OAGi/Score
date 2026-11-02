package org.oagi.score.gateway.http.api.message_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;

import java.util.Collection;
import java.util.Map;

public interface MessageCommandRepository {

    Map<UserId, MessageId> createMessages(Collection<UserId> recipientIdList,
                                          String subject,
                                          String body,
                                          String bodyContentType);

    int deleteMessages(Collection<MessageId> messageIdList);

    /**
     * Deletes every message the user has received, so that the account can be deleted: a message refers to its
     * recipient.
     */
    int deleteMessagesReceivedBy(UserId userId);

}
