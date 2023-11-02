package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateOasDocResponse extends Response {
    private final BigInteger oasDocId;
    private final boolean changed;

    public UpdateOasDocResponse(BigInteger oasDocId, boolean changed) {
        this.oasDocId = oasDocId;
        this.changed = changed;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public boolean isChanged() {
        return changed;
    }
}
