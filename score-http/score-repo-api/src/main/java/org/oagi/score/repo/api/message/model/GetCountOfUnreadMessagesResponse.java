package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.Response;

public class GetCountOfUnreadMessagesResponse extends Response {

    private final int countOfUnreadMessages;

    public GetCountOfUnreadMessagesResponse(int countOfUnreadMessages) {
        this.countOfUnreadMessages = countOfUnreadMessages;
    }

    public int getCountOfUnreadMessages() {
        return countOfUnreadMessages;
    }

}
