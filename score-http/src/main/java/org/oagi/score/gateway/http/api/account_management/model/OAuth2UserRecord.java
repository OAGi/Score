package org.oagi.score.gateway.http.api.account_management.model;

import java.util.Date;

public record OAuth2UserRecord(
        OAuth2UserId oAuth2UserId,
        UserId userId,
        String providerName,
        String sub,
        String name,
        String email,
        String nickname,
        String preferredUsername,
        String phoneNumber,
        Date creationTimestamp) {
}
