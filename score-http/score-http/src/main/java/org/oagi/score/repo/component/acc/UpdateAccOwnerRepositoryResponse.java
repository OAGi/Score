package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class UpdateAccOwnerRepositoryResponse {

    private final BigInteger accManifestId;

    public UpdateAccOwnerRepositoryResponse(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
