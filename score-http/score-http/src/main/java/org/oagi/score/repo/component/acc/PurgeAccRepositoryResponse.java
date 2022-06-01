package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class PurgeAccRepositoryResponse {

    private final BigInteger accManifestId;

    public PurgeAccRepositoryResponse(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
