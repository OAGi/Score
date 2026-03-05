package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

/**
 * Resolves filename collisions after a strategy has produced base filenames.
 */
public interface DuplicateHandler {

    /**
     * Resolves a possibly duplicated base filename.
     *
     * @param baseFilename    candidate base filename
     * @param topLevelAsbiepId current top-level ASBIEP id for deterministic suffixes
     * @param occurrence      zero-based occurrence index for the same base filename
     * @param totalOccurrences total number of candidates that share the same base filename
     * @return resolved base filename
     */
    String resolve(String baseFilename,
                   TopLevelAsbiepId topLevelAsbiepId,
                   int occurrence,
                   int totalOccurrences);

}
