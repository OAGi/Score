package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.util.HashSet;
import java.util.Set;

public class GetCountOfUnreadMessagesRequest extends Request {

    private Set<ScoreUser> senders = new HashSet();

    public GetCountOfUnreadMessagesRequest(ScoreUser requester) {
        super(requester);
    }

    public Set<ScoreUser> getSenders() {
        return senders;
    }

    public void addSender(ScoreUser sender) {
        this.senders.add(sender);
    }

    public GetCountOfUnreadMessagesRequest withSender(ScoreUser sender) {
        addSender(sender);
        return this;
    }

}
