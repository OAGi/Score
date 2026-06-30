package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;

import java.util.List;

/**
 * #1753 - L6: outcome of a CSV business-term import, distinguishing newly created rows from
 * existing rows that were updated (upsert-by-external-reference-URI), so the UI can report a
 * "created / updated" summary instead of only a coarse 204-vs-200 signal.
 */
public record BusinessTermCsvImportResult(
        List<BusinessTermId> businessTermIdList,
        int createdCount,
        int updatedCount) {

    public int totalCount() {
        return createdCount + updatedCount;
    }
}
