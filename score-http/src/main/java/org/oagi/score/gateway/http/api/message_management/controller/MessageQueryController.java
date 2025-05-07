package org.oagi.score.gateway.http.api.message_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.message_management.model.MessageDetailsRecord;
import org.oagi.score.gateway.http.api.message_management.model.MessageId;
import org.oagi.score.gateway.http.api.message_management.model.MessageListEntryRecord;
import org.oagi.score.gateway.http.api.message_management.repository.criteria.MessageListFilterCriteria;
import org.oagi.score.gateway.http.api.message_management.service.MessageQueryService;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Message - Queries", description = "API for retrieving message-related data")
@RequestMapping("/messages")
public class MessageQueryController {

    @Autowired
    private MessageQueryService messageQueryService;

    @Autowired
    private SessionService sessionService;

    @GetMapping(value = "/count-of-unread")
    public int countOfUnreadMessages(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        return messageQueryService.countOfUnreadMessages(sessionService.asScoreUser(user));
    }

    @GetMapping(value = "/{messageId:[\\d]+}")
    public MessageDetailsRecord getMessage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("messageId") MessageId messageId) {

        return messageQueryService.getMessageDetails(sessionService.asScoreUser(user), messageId);
    }

    @GetMapping()
    public PageResponse<MessageListEntryRecord> getMessageList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "subject", required = false) String subject,

            @RequestParam(name = "senderLoginIds", required = false) String senderLoginIds,

            @RequestParam(name = "createStart", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'createdOn'. " +
                            "Filter results to include only records updated after this timestamp (milliseconds since epoch).")
            String createStart,

            @RequestParam(name = "createEnd", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'createdOn'. " +
                            "Filter results to include only records updated before this timestamp (milliseconds since epoch).")
            String createEnd,

            @RequestParam(name = "createdOn", required = false)
            @Parameter(description = "Filter results by create timestamp range in epoch milliseconds. " +
                    "Format: `[after~before]`. Use `after` to specify the lower bound and `before` for the upper bound.")
            String createdOn,

            @RequestParam(name = "sortActive", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify the active sorting property.")
            String sortActive,

            @RequestParam(name = "sortDirection", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify sorting direction (ASC/DESC).")
            String sortDirection,

            @RequestParam(name = "orderBy", required = false)
            @Parameter(description = "Sorting criteria for the results. " +
                    "Supports multiple comma-separated properties with an optional '+' (ascending) or '-' (descending) prefix. " +
                    "If no prefix is specified, ascending order is applied by default. " +
                    "Example: `-releaseNum,+lastUpdateTimestamp,state` is equivalent to `releaseNum desc, lastUpdateTimestamp asc, state asc`.")
            String orderBy,

            @RequestParam(name = "pageIndex", required = false, defaultValue = "0")
            @Parameter(description = "Index of the page to retrieve (zero-based). " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageIndex,

            @RequestParam(name = "pageSize", required = false, defaultValue = "10")
            @Parameter(description = "Number of records per page. " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageSize) {

        MessageListFilterCriteria filterCriteria = new MessageListFilterCriteria(
                subject,
                separate(senderLoginIds).collect(toSet()),
                (hasLength(createdOn)) ?
                        DateRangeCriteria.create(createdOn) :
                        (hasLength(createStart) || hasLength(createEnd)) ?
                                DateRangeCriteria.create(
                                        hasLength(createStart) ? Long.valueOf(createStart) : null,
                                        hasLength(createEnd) ? Long.valueOf(createEnd) : null) : null
        );

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = messageQueryService.getMessageList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<MessageListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }
}
