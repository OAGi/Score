package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

public class GetOasRequestTableResponse extends Response {
    private final OasRequest oasRequest;

    public GetOasRequestTableResponse(OasRequest oasRequest) {
        this.oasRequest = oasRequest;
    }

    public OasRequest getOasRequestTable() {
        return oasRequest;
    }
}
