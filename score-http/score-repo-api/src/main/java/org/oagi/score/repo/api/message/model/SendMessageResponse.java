package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.Response;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public class SendMessageResponse extends Response {

    private Map<ScoreUser, BigInteger> messageIds;

    public SendMessageResponse(Map<ScoreUser, BigInteger> messageIds) {
        this.messageIds = Collections.unmodifiableMap(messageIds);
    }

    public Map<ScoreUser, BigInteger> getMessageIds() {
        return messageIds;
    }
}
