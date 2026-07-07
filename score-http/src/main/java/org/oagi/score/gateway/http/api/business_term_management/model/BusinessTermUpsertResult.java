package org.oagi.score.gateway.http.api.business_term_management.model;

/**
 * Result of an upsert-by-external-reference-URI on the {@code business_term} catalog: the id of the
 * row that was written, plus whether it was newly {@code created} (as opposed to an existing
 * same-URI row being updated).
 *
 * <p>Returning the created/updated signal here lets the batch import classify a row WITHOUT issuing a
 * separate existence query — the repository already probes the URI to decide insert-vs-update, so it
 * knows the outcome first-hand.</p>
 */
public record BusinessTermUpsertResult(BusinessTermId businessTermId, boolean created) {
}
