package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetMessageListResponse extends PaginationResponse<MessageList> {

    public GetMessageListResponse(List<MessageList> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
