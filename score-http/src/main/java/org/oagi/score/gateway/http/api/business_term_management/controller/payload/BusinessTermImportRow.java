package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

/**
 * One user-approved, possibly inline-edited row from the Business Term import dialog.
 * {@code rowIndex} is the 1-based source position, echoed back in the result so the UI can map a
 * per-row outcome to the row the user saw.
 */
public record BusinessTermImportRow(
        int rowIndex,
        String businessTerm,
        String externalReferenceId,
        String externalReferenceUri,
        String definition,
        String comment) {
}
