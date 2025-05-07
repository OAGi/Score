package org.oagi.score.gateway.http.api.account_management.controller.payload;

public record UpdatePasswordRequest(String oldPassword, String newPassword) {
}
