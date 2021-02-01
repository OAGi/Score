package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateBusinessContextResponse extends Response {

    private final BigInteger businessContextId;
    private final boolean changed;

    public UpdateBusinessContextResponse(BigInteger businessContextId, boolean changed) {
        this.businessContextId = businessContextId;
        this.changed = changed;
    }

    public BigInteger getBusinessContextId() {
        return businessContextId;
    }

    public boolean isChanged() {
        return changed;
    }
}
