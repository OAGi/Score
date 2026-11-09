package org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload;

final class ContextSchemeText {

    private ContextSchemeText() {
    }

    static String trimOrNull(String value) {
        return value == null ? null : value.trim();
    }
}
