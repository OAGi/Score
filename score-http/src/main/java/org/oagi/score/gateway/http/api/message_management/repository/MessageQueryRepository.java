package org.oagi.score.gateway.http.api.message_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.message_management.model.MessageDetailsRecord;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.model.MessageListEntryRecord;
import org.oagi.score.gateway.http.api.message_management.repository.criteria.MessageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;

public interface MessageQueryRepository {

    int getCountOfUnreadMessages(UserId requesterId, Collection<UserId> senderIdList);

    MessageDetailsRecord getMessageDetails(UserId requesterId, MessageId messageId);

    ResultAndCount<MessageListEntryRecord> getMessageList(UserId requesterId,
                                                          MessageListFilterCriteria filterCriteria,
                                                          PageRequest pageRequest);

}
