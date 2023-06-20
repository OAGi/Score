package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateBieForOasDocResponse extends Response {
    private final BigInteger bieForOasDocId;

    private final boolean changed;

    public UpdateBieForOasDocResponse(BigInteger bieForOasDocId, boolean changed) {
        this.bieForOasDocId = bieForOasDocId;
        this.changed = changed;
    }

    public BigInteger getBieForOasDocId() {
        return bieForOasDocId;
    }

    public boolean isChanged() {
        return changed;
    }
}
