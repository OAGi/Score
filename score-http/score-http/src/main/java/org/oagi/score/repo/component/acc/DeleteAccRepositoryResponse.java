package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class DeleteAccRepositoryResponse {

    private final BigInteger accManifestId;

    public DeleteAccRepositoryResponse(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
