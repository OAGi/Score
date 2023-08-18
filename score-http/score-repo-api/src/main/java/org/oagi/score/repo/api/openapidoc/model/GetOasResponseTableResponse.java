package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

public class GetOasResponseTableResponse extends Response {
    private final OasResponse oasResponse;

    public GetOasResponseTableResponse(OasResponse oasResponse) {
        this.oasResponse = oasResponse;
    }

    public OasResponse getOasResponseTable() {
        return oasResponse;
    }
}
