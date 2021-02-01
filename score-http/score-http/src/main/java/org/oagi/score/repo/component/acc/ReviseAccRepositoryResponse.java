package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class ReviseAccRepositoryResponse {

    private final BigInteger accManifestId;

    public ReviseAccRepositoryResponse(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
