package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateContextSchemeResponse extends Response {

    private final BigInteger contextSchemeId;
    private final boolean changed;

    public UpdateContextSchemeResponse(BigInteger contextSchemeId, boolean changed) {
        this.contextSchemeId = contextSchemeId;
        this.changed = changed;
    }

    public BigInteger getContextSchemeId() {
        return contextSchemeId;
    }

    public boolean isChanged() {
        return changed;
    }
}
