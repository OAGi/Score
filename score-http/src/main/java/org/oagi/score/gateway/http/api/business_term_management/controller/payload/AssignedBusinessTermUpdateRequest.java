package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

public record AssignedBusinessTermUpdateRequest(
        String typeCode,
        Boolean primaryIndicator) {
}
