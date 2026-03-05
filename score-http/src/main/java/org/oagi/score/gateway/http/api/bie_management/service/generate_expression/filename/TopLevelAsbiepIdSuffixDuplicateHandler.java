package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.springframework.stereotype.Component;

/**
 * Resolves duplicates by appending the top-level ASBIEP id when needed:
 * {@code name~{topLevelAsbiepId}}.
 */
@Component
public class TopLevelAsbiepIdSuffixDuplicateHandler implements DuplicateHandler {

    @Override
    public String resolve(String baseFilename,
                          TopLevelAsbiepId topLevelAsbiepId,
                          int occurrence,
                          int totalOccurrences) {
        if (totalOccurrences <= 1) {
            return baseFilename;
        }
        return baseFilename + "~" + topLevelAsbiepId.value();
    }

}
