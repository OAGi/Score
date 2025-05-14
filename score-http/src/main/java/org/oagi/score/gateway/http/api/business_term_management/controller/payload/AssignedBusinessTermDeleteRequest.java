package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import org.oagi.score.gateway.http.api.business_term_management.model.AsbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BbieBusinessTermId;

import java.util.List;

public record AssignedBusinessTermDeleteRequest(
        List<AsbieBusinessTermId> assignedAsbieBizTermIdList,
        List<BbieBusinessTermId> assignedBbieBizTermIdList) {
}
