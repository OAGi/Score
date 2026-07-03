package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.base.Response;

/**
 * Issue #1347: result of a document-level bulk error-response update.
 */
public class BulkUpdateErrorResponseResponse extends Response {

    private final OasDocId oasDocId;
    private final boolean changed;

    public BulkUpdateErrorResponseResponse(OasDocId oasDocId, boolean changed) {
        this.oasDocId = oasDocId;
        this.changed = changed;
    }

    public OasDocId getOasDocId() {
        return oasDocId;
    }

    public boolean isChanged() {
        return changed;
    }
}
