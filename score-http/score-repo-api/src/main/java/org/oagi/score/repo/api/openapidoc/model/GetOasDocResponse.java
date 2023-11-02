package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

public class GetOasDocResponse extends Response {
    private final OasDoc oasDoc;

    public GetOasDocResponse(OasDoc oasDoc) {
        this.oasDoc = oasDoc;
    }

    public final OasDoc getOasDoc() {
        return oasDoc;
    }
}
