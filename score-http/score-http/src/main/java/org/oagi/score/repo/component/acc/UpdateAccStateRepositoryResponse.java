package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class UpdateAccStateRepositoryResponse {

    private final BigInteger accManifestId;

    public UpdateAccStateRepositoryResponse(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
