package org.oagi.score.gateway.http.api.account_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.util.List;

public interface ScoreUserQueryRepository {

    ScoreUser getScoreUser(UserId userId);

    ScoreUser getScoreUserByUsername(String username);

    /**
     * The user linked to the SSO account {@code oidcSub} of the identity provider {@code providerName}; {@code sub} is
     * unique only within one provider. A {@code null} provider name matches the account of any provider.
     */
    ScoreUser getScoreUserByOidcSub(String providerName, String oidcSub);

    List<ScoreUser> getScoreUsers();

    List<ScoreUser> getScoreUsersByRole(ScoreRole role);

}
