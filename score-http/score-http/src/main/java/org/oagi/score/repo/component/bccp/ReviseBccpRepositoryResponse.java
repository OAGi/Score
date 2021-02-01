package org.oagi.score.repo.component.bccp;

import java.math.BigInteger;

public class ReviseBccpRepositoryResponse {

    private final BigInteger bccpManifestId;

    public ReviseBccpRepositoryResponse(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
