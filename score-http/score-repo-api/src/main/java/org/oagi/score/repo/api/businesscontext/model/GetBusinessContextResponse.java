package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

public class GetBusinessContextResponse extends Response {

    private final BusinessContext businessContext;

    public GetBusinessContextResponse(BusinessContext businessContext) {
        this.businessContext = businessContext;
    }

    public final BusinessContext getBusinessContext() {
        return businessContext;
    }
}
