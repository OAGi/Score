package org.oagi.score.repo.api.message;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.message.model.DiscardMessageRequest;
import org.oagi.score.repo.api.message.model.SendMessageRequest;
import org.oagi.score.repo.api.message.model.SendMessageResponse;

public interface MessageWriteRepository {

    SendMessageResponse sendMessage(
            SendMessageRequest request) throws ScoreDataAccessException;

    void discardMessage(
            DiscardMessageRequest request) throws ScoreDataAccessException;

}
