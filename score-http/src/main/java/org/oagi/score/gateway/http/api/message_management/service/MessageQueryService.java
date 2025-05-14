package org.oagi.score.gateway.http.api.message_management.service;

import org.oagi.score.gateway.http.api.message_management.model.MessageDetailsRecord;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.model.MessageListEntryRecord;
import org.oagi.score.gateway.http.api.message_management.repository.MessageQueryRepository;
import org.oagi.score.gateway.http.api.message_management.repository.criteria.MessageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MessageQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private MessageQueryRepository query(ScoreUser requester) {
        return repositoryFactory.messageQueryRepository(requester);
    }

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public int countOfUnreadMessages(ScoreUser requester) {
        return query(requester).getCountOfUnreadMessages(
                requester.userId(), Collections.emptyList());
    }

    public MessageDetailsRecord getMessageDetails(ScoreUser requester, MessageId messageId) {

        MessageDetailsRecord messageDetails = query(requester).getMessageDetails(
                requester.userId(), messageId);

        Map<String, String> properties = new HashMap();
        properties.put("messageId", messageId.toString());
        simpMessagingTemplate.convertAndSend("/topic/message/" + requester.username(), properties);

        return messageDetails;
    }

    public ResultAndCount<MessageListEntryRecord> getMessageList(
            ScoreUser requester, MessageListFilterCriteria filterCriteria, PageRequest pageRequest) {

        if (requester == null) {
            return new ResultAndCount<>(Collections.emptyList(), 0);
        }

        return query(requester).getMessageList(
                requester.userId(), filterCriteria, pageRequest);
    }
}
