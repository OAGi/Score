package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

public class GetOasTagResponse extends Response {
    private final OasTag oasTag;
    public GetOasTagResponse(OasTag oasTag) {
        this.oasTag = oasTag;
    }
    public OasTag getOasTag() {
        return oasTag;
    }
}
