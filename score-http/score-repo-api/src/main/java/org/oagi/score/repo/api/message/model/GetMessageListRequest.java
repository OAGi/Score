package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public class GetMessageListRequest extends PaginationRequest<MessageList> {

    private Collection<String> senderUsernameList;
    private LocalDateTime createStartDate;
    private LocalDateTime createEndDate;

    public GetMessageListRequest(ScoreUser requester) {
        super(requester, MessageList.class);
    }

    public Collection<String> getSenderUsernameList() {
        return (senderUsernameList == null) ? Collections.emptyList() : senderUsernameList;
    }

    public void setSenderUsernameList(Collection<String> senderUsernameList) {
        this.senderUsernameList = senderUsernameList;
    }

    public LocalDateTime getCreateStartDate() {
        return createStartDate;
    }

    public void setCreateStartDate(LocalDateTime createStartDate) {
        this.createStartDate = createStartDate;
    }

    public LocalDateTime getCreateEndDate() {
        return createEndDate;
    }

    public void setCreateEndDate(LocalDateTime createEndDate) {
        this.createEndDate = createEndDate;
    }

}
