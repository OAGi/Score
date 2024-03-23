package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Response;

public class GetBieSetResponse extends Response {

    private final BieSet bieSet;

    public GetBieSetResponse(BieSet bieSet) {
        this.bieSet = bieSet;
    }

    public BieSet getBieSet() {
        return bieSet;
    }
}
