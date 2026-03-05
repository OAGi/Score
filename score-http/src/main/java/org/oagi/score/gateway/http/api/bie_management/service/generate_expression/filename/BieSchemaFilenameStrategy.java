package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.common.model.ScoreUser;

/**
 * Strategy for building a base schema filename (without extension) for a top-level ASBIEP.
 * <p>
 * Implementations focus only on base-name composition rules. Duplicate resolution is delegated to
 * {@link #duplicateHandler()}.
 */
public interface BieSchemaFilenameStrategy {

    /**
     * Builds a base filename for the given top-level ASBIEP.
     *
     * @param requester      current user context used by repository lookups
     * @param topLevelAsbiep target top-level ASBIEP
     * @param option         generation options that affect filename composition
     * @return base filename without file extension
     */
    String buildBaseFilename(ScoreUser requester,
                             TopLevelAsbiepSummaryRecord topLevelAsbiep,
                             GenerateExpressionOption option);

    /**
     * Returns the duplicate-resolution policy paired with this strategy.
     *
     * @return duplicate filename handler
     */
    DuplicateHandler duplicateHandler();

}
