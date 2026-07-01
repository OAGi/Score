package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

/**
 * Per-row outcome of a batch import. {@code outcome} is one of {@code CREATED}, {@code UPDATED}
 * (upsert keyed by external reference URI), or {@code FAILED}; {@code message} carries the
 * validation/error detail for failed rows.
 */
public record BusinessTermBatchImportRowResult(
        int rowIndex,
        String businessTerm,
        String externalReferenceUri,
        String outcome,
        String message) {

    public static final String OUTCOME_CREATED = "CREATED";
    public static final String OUTCOME_UPDATED = "UPDATED";
    public static final String OUTCOME_FAILED = "FAILED";
}
