package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import java.util.List;

/**
 * Outcome of a best-effort batch import: aggregate counts plus a per-row result list. The endpoint
 * returns 200 even when some rows fail, so the dialog can render a created/updated/failed summary
 * and let the user retry only the rows that need fixing.
 */
public record BusinessTermBatchImportResult(
        int createdCount,
        int updatedCount,
        int failedCount,
        List<BusinessTermBatchImportRowResult> results) {
}
