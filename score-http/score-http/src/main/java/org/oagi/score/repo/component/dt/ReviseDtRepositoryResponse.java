package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class ReviseDtRepositoryResponse {

    private final BigInteger dtManifestId;

    public ReviseDtRepositoryResponse(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
