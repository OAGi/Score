package org.oagi.score.repo.api.user;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.user.model.GetScoreUserRequest;
import org.oagi.score.repo.api.user.model.GetScoreUserResponse;
import org.oagi.score.repo.api.user.model.GetScoreUsersRequest;
import org.oagi.score.repo.api.user.model.GetScoreUsersResponse;

public interface ScoreUserReadRepository {

    GetScoreUserResponse getScoreUser(
            GetScoreUserRequest request) throws ScoreDataAccessException;

    GetScoreUsersResponse getScoreUsers(
            GetScoreUsersRequest request) throws ScoreDataAccessException;

}
