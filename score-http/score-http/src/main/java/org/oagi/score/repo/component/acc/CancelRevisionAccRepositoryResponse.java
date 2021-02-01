package org.oagi.score.repo.component.acc;

import java.math.BigInteger;

public class CancelRevisionAccRepositoryResponse {

    private final BigInteger accManifestId;

    public CancelRevisionAccRepositoryResponse(BigInteger accManifestId) {
        this.accManifestId = accManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
