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

    /** Whether the account {@code username} exists and is not enabled (disabled, or without the flag). */
    boolean isAccountDisabled(String username);

    /**
     * Whether the SSO account {@code oidcSub} of the identity provider {@code providerName} is linked to an account that
     * is not enabled. An SSO account that is not linked to an account yet (pending approval) is not disabled.
     */
    boolean isOidcAccountDisabled(String providerName, String oidcSub);

    /** The subs of the SSO accounts linked to the account {@code username}, one per identity provider. */
    List<String> getOidcSubs(String username);

    List<ScoreUser> getScoreUsers();

    List<ScoreUser> getScoreUsersByRole(ScoreRole role);

}
