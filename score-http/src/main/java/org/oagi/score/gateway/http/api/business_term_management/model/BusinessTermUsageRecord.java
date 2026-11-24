package org.oagi.score.gateway.http.api.business_term_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.List;

/**
 * A top-level BIE that a business term is assigned in, with the nodes of it that the term is assigned to.
 */
public record BusinessTermUsageRecord(
        TopLevelAsbiepId topLevelAsbiepId,
        String propertyTerm,
        String version,
        BieState state,
        String releaseNum,
        UserSummaryRecord owner,
        WhoAndWhen lastUpdated,
        List<BusinessTermUsageNodeRecord> nodes) {
}
