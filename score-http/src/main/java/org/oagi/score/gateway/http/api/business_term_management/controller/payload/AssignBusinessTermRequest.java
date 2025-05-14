package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import org.oagi.score.gateway.http.api.business_term_management.model.BieToAssign;

import java.util.List;

public record AssignBusinessTermRequest(
        List<BieToAssign> biesToAssign,
        boolean primaryIndicator,
        String typeCode) {
}
