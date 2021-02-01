package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class UpdateAccBasedAccRepositoryResponse {

    private final BigInteger accManifestId;

    public UpdateAccBasedAccRepositoryResponse(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
