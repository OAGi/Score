package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

public class GetContextSchemeResponse extends Response {

    private final ContextScheme contextScheme;

    public GetContextSchemeResponse(ContextScheme contextScheme) {
        this.contextScheme = contextScheme;
    }

    public final ContextScheme getContextScheme() {
        return contextScheme;
    }
}
