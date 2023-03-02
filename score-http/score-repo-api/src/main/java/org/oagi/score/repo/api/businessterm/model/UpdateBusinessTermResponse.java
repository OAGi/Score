package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateBusinessTermResponse extends Response {

    private final BigInteger businessTermId;
    private final boolean changed;

    public UpdateBusinessTermResponse(BigInteger businessTermId, boolean changed) {
        this.businessTermId = businessTermId;
        this.changed = changed;
    }

    public BigInteger getBusinessTermId() {
        return businessTermId;
    }

    public boolean isChanged() {
        return changed;
    }
}
