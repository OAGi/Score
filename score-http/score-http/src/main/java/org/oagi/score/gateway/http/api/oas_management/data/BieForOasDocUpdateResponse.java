package org.oagi.score.gateway.http.api.oas_management.data;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class BieForOasDocUpdateResponse extends Response {
    private final BigInteger oasDocId;
    private List<BigInteger> topLevelAsbiepIds;
    private final boolean changed;

    public BieForOasDocUpdateResponse(BigInteger oasDocId, List<BigInteger> topLevelAsbiepIds, boolean changed) {
        this.oasDocId = oasDocId;
        this.topLevelAsbiepIds = topLevelAsbiepIds;
        this.changed = changed;
    }

    public boolean isChanged() {
        return changed;
    }

    public List<BigInteger> getTopLevelAsbiepIds() {
        return topLevelAsbiepIds;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }
}
