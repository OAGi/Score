package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

public class GetOasOperationResponse extends Response {
    private final OasOperation oasOperation;

    public GetOasOperationResponse(OasOperation oasOperation) {
        this.oasOperation = oasOperation;
    }

    public OasOperation getOasOperation() {
        return oasOperation;
    }
}
