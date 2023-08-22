package org.oagi.score.repo.api.user.model;

import org.oagi.score.repo.api.base.Response;

import java.util.List;

public class GetScoreUsersResponse extends Response {

    private final List<ScoreUser> users;

    public GetScoreUsersResponse(List<ScoreUser> users) {
        this.users = users;
    }

    public List<ScoreUser> getUsers() {
        return users;
    }

}
