package org.oagi.score.gateway.http.api.account_management.model;

import org.oagi.score.gateway.http.common.model.ScoreRole;

import java.util.Collection;

/**
 * DTO representing a summary of a user.
 * Includes essential user information such as the user ID, login ID, username,
 * and a collection of roles assigned to the user.
 */
public record UserSummaryRecord(UserId userId, String loginId, String username,
                                Collection<ScoreRole> roles) {

    public boolean isDeveloper() {
        return (roles == null || roles.isEmpty()) ? false : roles.contains(ScoreRole.DEVELOPER);
    }

    public boolean isAdministrator() {
        return (roles == null || roles.isEmpty()) ? false : roles.contains(ScoreRole.ADMINISTRATOR);
    }

}
