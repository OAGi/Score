package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class UpdateBccpStateRepositoryResponse {

    private final BigInteger bccpManifestId;

    public UpdateBccpStateRepositoryResponse(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
