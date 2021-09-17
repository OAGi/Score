package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class UpdateBccpOwnerRepositoryResponse {

    private final BigInteger bccpManifestId;

    public UpdateBccpOwnerRepositoryResponse(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
