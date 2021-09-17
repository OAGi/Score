package org.oagi.score.repo.api.base;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;

public class Request implements Serializable {

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    private ScoreUser requester;

    public Request() {
        this.requester = null;
    }

    public Request(ScoreUser requester) {
        if (requester == null) {
            throw new IllegalArgumentException();
        }

        this.requester = requester;
    }

    public ScoreUser getRequester() {
        return this.requester;
    }

}
