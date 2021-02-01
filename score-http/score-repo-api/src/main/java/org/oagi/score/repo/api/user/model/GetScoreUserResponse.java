package org.oagi.score.repo.api.user.model;

import org.oagi.score.repo.api.base.Response;

public class GetScoreUserResponse extends Response {

    private final ScoreUser user;

    public GetScoreUserResponse(ScoreUser user) {
        this.user = user;
    }

    public ScoreUser getUser() {
        return user;
    }

}
