package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import java.util.List;

/**
 * Body of {@code POST /business-terms/batch}: the rows the user selected (and possibly edited) in
 * the import dialog's preview step.
 */
public record BusinessTermBatchImportRequest(
        List<BusinessTermImportRow> rows) {
}
