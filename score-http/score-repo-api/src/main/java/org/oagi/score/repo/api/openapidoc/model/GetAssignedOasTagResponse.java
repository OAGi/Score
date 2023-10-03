package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

public class GetAssignedOasTagResponse extends Response {
    private final OasTag oasTag;
    public GetAssignedOasTagResponse(OasTag oasTag) {
        this.oasTag = oasTag;
    }
    public OasTag getOasTag() {
        return oasTag;
    }

}
