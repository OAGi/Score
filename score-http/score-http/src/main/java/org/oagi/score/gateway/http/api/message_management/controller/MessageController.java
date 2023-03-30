package org.oagi.score.gateway.http.api.message_management.controller;

import org.oagi.score.gateway.http.api.message_management.data.CountOfUnreadMessages;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.message.model.GetMessageListRequest;
import org.oagi.score.repo.api.message.model.GetMessageListResponse;
import org.oagi.score.repo.api.message.model.GetMessageResponse;
import org.oagi.score.repo.api.message.model.MessageList;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class MessageController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/message/count_of_unread", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CountOfUnreadMessages countOfUnreadMessages(
            @AuthenticationPrincipal AuthenticatedPrincipal requester) {
        CountOfUnreadMessages countOfUnreadMessages = new CountOfUnreadMessages();
        if (requester == null) {
            countOfUnreadMessages.setCountOfUnreadMessages(-1);
        } else {
            countOfUnreadMessages.setCountOfUnreadMessages(
                    messageService.getCountOfUnreadMessagesRequest(
                            sessionService.asScoreUser(requester)));
        }
        return countOfUnreadMessages;
    }

    @RequestMapping(value = "/message_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<MessageList> getMessageList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "senderUsernameList", required = false) String senderUsernameList,
            @RequestParam(name = "createStart", required = false) String createStart,
            @RequestParam(name = "createEnd", required = false) String createEnd,
            @RequestParam(name = "sortActive", required = false) String sortActive,
            @RequestParam(name = "sortDirection", required = false) String sortDirection,
            @RequestParam(name = "pageIndex", defaultValue = "-1") int pageIndex,
            @RequestParam(name = "pageSize", defaultValue = "-1") int pageSize) {

        GetMessageListRequest request = new GetMessageListRequest(
                authenticationService.asScoreUser(requester));

        request.setSenderUsernameList(!StringUtils.hasLength(senderUsernameList) ? Collections.emptyList() :
                Arrays.asList(senderUsernameList.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        if (StringUtils.hasLength(createStart)) {
            request.setCreateStartDate(new Timestamp(Long.valueOf(createStart)).toLocalDateTime());
        }
        if (StringUtils.hasLength(createEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(createEnd));
            calendar.add(Calendar.DATE, 1);
            request.setCreateEndDate(new Timestamp(calendar.getTimeInMillis()).toLocalDateTime());
        }

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActive(sortActive);
        request.setSortDirection("asc".equalsIgnoreCase(sortDirection) ? ASC : DESC);

        GetMessageListResponse response = messageService.getMessageList(request);

        PageResponse<MessageList> pageResponse = new PageResponse<>();
        pageResponse.setList(response.getResults());
        pageResponse.setPage(response.getPage());
        pageResponse.setSize(response.getSize());
        pageResponse.setLength(response.getLength());
        return pageResponse;
    }

    @RequestMapping(value = "/message/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public GetMessageResponse getMessage(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger messageId) {
        return messageService.getMessage(
                sessionService.asScoreUser(requester),
                messageId);
    }

    @RequestMapping(value = "/message/{id:[\\d]+}", method = RequestMethod.DELETE)
    public ResponseEntity discardMessage(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger messageId) {
        messageService.discardMessage(
                sessionService.asScoreUser(requester),
                messageId);
        return ResponseEntity.accepted().build();
    }
}
