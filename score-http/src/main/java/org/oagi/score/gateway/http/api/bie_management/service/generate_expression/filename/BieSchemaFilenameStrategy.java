package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;

/**
 * Strategy for building a base schema filename (without extension) for a top-level ASBIEP.
 */
public interface BieSchemaFilenameStrategy {

    /**
     * Builds a base filename for the given top-level ASBIEP.
     *
     * @param requester      current user context used by repository lookups
     * @param topLevelAsbiep target top-level ASBIEP
     * @return base filename without file extension
     */
    String buildBaseFilename(ScoreUser requester,
                             TopLevelAsbiepSummaryRecord topLevelAsbiep);

    /**
     * Builds a base filename for a schema file shared by the schema files of the generation run,
     * such as {@code CommonCodeList}. A BIE package prefixes it with the package name and version ID,
     * as the schema files of the package are.
     *
     * @param commonName name of the shared file, without file extension
     * @return base filename without file extension
     */
    String buildCommonFilename(String commonName);

    String resolveDuplicateFilename(String baseFilename,
                                    TopLevelAsbiepId topLevelAsbiepId,
                                    int occurrence,
                                    int totalOccurrences);

}
