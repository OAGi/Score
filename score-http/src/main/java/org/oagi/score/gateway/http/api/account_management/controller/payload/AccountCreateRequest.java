package org.oagi.score.gateway.http.api.account_management.controller.payload;

import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserId;

public record AccountCreateRequest(
        String loginId,
        String password,
        String name,
        String organization,
        boolean developer,
        boolean admin,
        OAuth2UserId oAuth2UserId,
        String sub) {
}
