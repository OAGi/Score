package org.oagi.score.gateway.http.api.account_management.controller.payload;

import org.oagi.score.gateway.http.api.account_management.model.UserId;

public record UpdateAccountRequest(
        UserId userId,
        boolean admin,
        String username,
        String organization,
        String newPassword) {

    public UpdateAccountRequest withUserId(UserId userId) {
        return new UpdateAccountRequest(userId, admin, username, organization, newPassword);
    }

}
