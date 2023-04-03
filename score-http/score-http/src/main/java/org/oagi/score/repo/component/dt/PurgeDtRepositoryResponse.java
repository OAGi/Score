package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class PurgeDtRepositoryResponse {

    private final BigInteger dtManifestId;

    private final Throwable error;

    public PurgeDtRepositoryResponse(BigInteger dtManifestId) {
        this(dtManifestId, null);
    }

    public PurgeDtRepositoryResponse(BigInteger dtManifestId, Throwable error) {
        this.dtManifestId = dtManifestId;
        this.error = error;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }

    public Throwable getError() {
        return error;
    }

}
