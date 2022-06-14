package org.oagi.score.repo.component.bccp;

import java.math.BigInteger;

public class PurgeBccpRepositoryResponse {

    private final BigInteger bccpManifestId;

    public PurgeBccpRepositoryResponse(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
