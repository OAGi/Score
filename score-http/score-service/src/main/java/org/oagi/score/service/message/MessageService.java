package org.oagi.score.service.message;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.message.MessageReadRepository;
import org.oagi.score.repo.api.message.MessageWriteRepository;
import org.oagi.score.repo.api.message.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MessageService {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public int getCountOfUnreadMessagesRequest(ScoreUser requester) {
        MessageReadRepository messageReadRepository = scoreRepositoryFactory.createMessageReadRepository();
        return messageReadRepository.getCountOfUnreadMessages(new GetCountOfUnreadMessagesRequest(requester))
                .getCountOfUnreadMessages();
    }

    @Transactional // This transaction should be not a read-only to mark the message as read.
    public GetMessageResponse getMessage(ScoreUser requester, BigInteger messageId) {
        MessageReadRepository messageReadRepository = scoreRepositoryFactory.createMessageReadRepository();
        GetMessageResponse response = messageReadRepository.getMessage(new GetMessageRequest(requester, messageId));
        Map<String, String> properties = new HashMap();
        properties.put("messageId", messageId.toString());
        simpMessagingTemplate.convertAndSend("/topic/message/" + requester.getUsername(), properties);
        return response;
    }

    @Transactional
    public void discardMessages(ScoreUser requester, List<BigInteger> messageIdList) {
        if (messageIdList == null || messageIdList.isEmpty()) {
            return;
        }

        MessageWriteRepository messageWriteRepository = scoreRepositoryFactory.createMessageWriteRepository();
        messageWriteRepository.discardMessage(new DiscardMessageRequest(requester, messageIdList));
        Map<String, String> properties = new HashMap();
        if (messageIdList.size() == 1) {
            properties.put("messageId", messageIdList.get(0).toString());
        } else {
            properties.put("messageIdList", messageIdList.stream()
                    .map(e -> e.toString()).collect(Collectors.joining(", ")));
        }
        simpMessagingTemplate.convertAndSend("/topic/message/" + requester.getUsername(), properties);
    }

    @Transactional
    public void discardMessage(ScoreUser requester, BigInteger messageId) {
        discardMessages(requester, Arrays.asList(messageId));
    }

    @Async
    @Transactional
    public CompletableFuture<SendMessageResponse> asyncSendMessage(SendMessageRequest request) {
        MessageWriteRepository messageWriteRepository = scoreRepositoryFactory.createMessageWriteRepository();
        return CompletableFuture.supplyAsync(() -> {
            SendMessageResponse response = messageWriteRepository.sendMessage(request);
            for (Map.Entry<ScoreUser, BigInteger> resp : response.getMessageIds().entrySet()) {
                Map<String, String> properties = new HashMap();
                properties.put("messageId", resp.getValue().toString());
                simpMessagingTemplate.convertAndSend("/topic/message/" + resp.getKey().getUsername(), properties);
            }
            return response;
        });
    }

    public GetMessageListResponse getMessageList(GetMessageListRequest request) {
        GetMessageListResponse response =
                scoreRepositoryFactory.createMessageReadRepository()
                        .getMessageList(request);

        return response;
    }
}
