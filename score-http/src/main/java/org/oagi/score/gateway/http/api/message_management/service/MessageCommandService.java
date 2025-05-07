package org.oagi.score.gateway.http.api.message_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.repository.MessageCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private MessageCommandRepository command(ScoreUser requester) {
        return repositoryFactory.messageCommandRepository(requester);
    }

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Async
    @Transactional
    public CompletableFuture<Map<UserId, MessageId>> asyncSendMessage(ScoreUser requester,
                                                                      Collection<UserId> recipientIdList,
                                                                      String subject,
                                                                      String body,
                                                                      String bodyContentType) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UserId, MessageId> messageIdMap = command(requester).createMessages(
                    recipientIdList, subject, body, bodyContentType);
            for (Map.Entry<UserId, MessageId> resp : messageIdMap.entrySet()) {
                Map<String, String> properties = new HashMap();
                properties.put("messageId", resp.getValue().toString());

                ScoreUser recipient = sessionService.getScoreUserByUserId(resp.getKey());
                simpMessagingTemplate.convertAndSend("/topic/message/" + recipient.username(), properties);
            }
            return messageIdMap;
        });
    }

    public boolean discard(ScoreUser requester, MessageId messageId) {

        return discard(requester, Arrays.asList(messageId)) == 1;
    }

    public int discard(ScoreUser requester, Collection<MessageId> messageIdList) {
        if (messageIdList == null || messageIdList.isEmpty()) {
            return 0;
        }

        int numOfDeletedRecords = command(requester).deleteMessages(messageIdList);

        Map<String, String> properties = new HashMap();
        if (messageIdList.size() == 1) {
            properties.put("messageId", messageIdList.iterator().next().toString());
        } else {
            properties.put("messageIdList", messageIdList.stream()
                    .map(e -> e.toString()).collect(Collectors.joining(", ")));
        }
        simpMessagingTemplate.convertAndSend("/topic/message/" + requester.username(), properties);

        return numOfDeletedRecords;
    }
}
