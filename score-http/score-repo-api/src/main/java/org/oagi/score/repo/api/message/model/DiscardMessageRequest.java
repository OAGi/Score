package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.List;

public class DiscardMessageRequest extends Request {

    private final List<BigInteger> messageIdList;

    public DiscardMessageRequest(ScoreUser requester, List<BigInteger> messageIdList) {
        super(requester);
        this.messageIdList = messageIdList;
    }

    public List<BigInteger> getMessageIdList() {
        return messageIdList;
    }

}
