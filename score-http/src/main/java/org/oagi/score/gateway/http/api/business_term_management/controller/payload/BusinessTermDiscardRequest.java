package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;

import java.util.List;

public record BusinessTermDiscardRequest(List<BusinessTermId> businessTermIdList) {
}
