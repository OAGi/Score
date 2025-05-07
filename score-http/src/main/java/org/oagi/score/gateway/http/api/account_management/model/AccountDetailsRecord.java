package org.oagi.score.gateway.http.api.account_management.model;

public record AccountDetailsRecord(
        UserId userId,
        String loginId,
        String username,
        String organization,
        String email,

        boolean developer,
        boolean admin,
        boolean enabled,
        boolean emailVerified,
        boolean hasData,

        OAuth2UserId oAuth2UserId,
        String providerName,
        String sub,
        String oidcName,
        String oidcEmail,
        String nickname,
        String preferredUsername,
        String phoneNumber) {
}
