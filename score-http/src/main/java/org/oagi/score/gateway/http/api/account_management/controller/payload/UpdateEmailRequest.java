package org.oagi.score.gateway.http.api.account_management.controller.payload;

import java.util.Collections;
import java.util.Map;

public record UpdateEmailRequest(
        String email,
        Map<String, Object> parameters) {

    public Map<String, Object> parameters() {
        if (parameters == null) {
            return Collections.emptyMap();
        }
        return parameters;
    }

}
