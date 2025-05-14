package org.oagi.score.gateway.http.api.cc_management.controller.payload;

public record CcVerifyAppendResponse(
        boolean warn,
        String message) {
}
