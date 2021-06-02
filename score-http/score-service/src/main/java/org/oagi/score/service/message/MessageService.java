package org.oagi.score.service.message;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
