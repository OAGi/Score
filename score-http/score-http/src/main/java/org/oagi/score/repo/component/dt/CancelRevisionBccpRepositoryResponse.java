package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class CancelRevisionBccpRepositoryResponse {

    private final BigInteger bccpManifestId;

    public CancelRevisionBccpRepositoryResponse(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
