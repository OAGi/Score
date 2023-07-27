package org.oagi.score.repo.api.user.model;

import org.oagi.score.repo.api.base.Request;

public class GetScoreUsersRequest extends Request {

    private ScoreRole role;

    public GetScoreUsersRequest() {
        super();
    }

    public ScoreRole getRole() {
        return role;
    }

    public void setRole(ScoreRole role) {
        this.role = role;
    }

    public GetScoreUsersRequest withRole(ScoreRole role) {
        setRole(role);
        return this;
    }

}
