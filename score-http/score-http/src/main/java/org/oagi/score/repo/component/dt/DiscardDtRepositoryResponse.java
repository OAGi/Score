package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class DiscardDtRepositoryResponse {

    private final BigInteger dtManifestId;

    public DiscardDtRepositoryResponse(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
