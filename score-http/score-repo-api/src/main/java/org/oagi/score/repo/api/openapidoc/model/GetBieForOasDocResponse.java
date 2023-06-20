package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

public class GetBieForOasDocResponse extends Response {
    private final BieForOasDoc bieForOasDoc;

    public GetBieForOasDocResponse(BieForOasDoc bieForOasDoc) {
        this.bieForOasDoc = bieForOasDoc;
    }

    public final BieForOasDoc getBieForOasDoc() {
        return bieForOasDoc;
    }
}
