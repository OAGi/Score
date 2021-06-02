package org.oagi.score.repo.api.message;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.message.model.*;

public interface MessageReadRepository {

    GetCountOfUnreadMessagesResponse getCountOfUnreadMessages(
            GetCountOfUnreadMessagesRequest request) throws ScoreDataAccessException;

    GetMessageListResponse getMessageList(
            GetMessageListRequest request) throws ScoreDataAccessException;

    GetMessageResponse getMessage(
            GetMessageRequest request) throws ScoreDataAccessException;

}
