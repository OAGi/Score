package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class PurgeAccRepositoryResponse {

    private final BigInteger accManifestId;

    private final Throwable error;

    public PurgeAccRepositoryResponse(BigInteger accManifestId) {
        this(accManifestId, null);
    }

    public PurgeAccRepositoryResponse(BigInteger accManifestId, Throwable error) {
        this.accManifestId = accManifestId;
        this.error = error;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }

    public Throwable getError() {
        return error;
    }
}
