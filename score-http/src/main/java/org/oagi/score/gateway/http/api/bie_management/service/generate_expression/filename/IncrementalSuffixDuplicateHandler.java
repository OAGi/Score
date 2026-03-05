package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.springframework.stereotype.Component;

/**
 * Resolves duplicates by appending an incremental numeric suffix:
 * {@code name}, {@code name-1}, {@code name-2}, ...
 */
@Component
public class IncrementalSuffixDuplicateHandler implements DuplicateHandler {

    @Override
    public String resolve(String baseFilename,
                          TopLevelAsbiepId topLevelAsbiepId,
                          int occurrence,
                          int totalOccurrences) {
        return (occurrence == 0) ? baseFilename : (baseFilename + "-" + occurrence);
    }

}
