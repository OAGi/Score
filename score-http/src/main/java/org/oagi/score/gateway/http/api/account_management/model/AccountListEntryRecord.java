package org.oagi.score.gateway.http.api.account_management.model;

public record AccountListEntryRecord(
        UserId userId,
        String loginId,
        String username,
        String organization,

        boolean developer,
        boolean admin,
        boolean enabled,

        OAuth2UserId oAuth2UserId) {
}
