package org.oagi.score.gateway.http.api.account_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.util.List;

public interface ScoreUserQueryRepository {

    ScoreUser getScoreUser(UserId userId);

    ScoreUser getScoreUserByUsername(String username);

    ScoreUser getScoreUserByOidcSub(String oidcSub);

    List<ScoreUser> getScoreUsers();

    List<ScoreUser> getScoreUsersByRole(ScoreRole role);

}
