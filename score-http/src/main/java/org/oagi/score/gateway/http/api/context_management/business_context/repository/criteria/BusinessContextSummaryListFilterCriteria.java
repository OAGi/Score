package org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.util.Collection;

/**
 * Criteria for filtering business context summaries.
 * This record includes filters for business context meaning, tenant status,
 * associated top-level ASBIEP, and a list of business context IDs.
 */
public record BusinessContextSummaryListFilterCriteria(String name,
                                                       boolean tenantEnabled,
                                                       TopLevelAsbiepId topLevelAsbiepId,
                                                       Collection<BusinessContextId> businessContextIdList) {
}
