package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class UpdateBieForOasDocResponse extends Response {
    private final BigInteger oasDocId;
    private final boolean changed;

    public UpdateBieForOasDocResponse(BigInteger oasDocId,  boolean changed) {
        this.oasDocId = oasDocId;
        this.changed = changed;
    }
    public boolean isChanged() {
        return changed;
    }
    public BigInteger getOasDocId() {
        return oasDocId;
    }
}
