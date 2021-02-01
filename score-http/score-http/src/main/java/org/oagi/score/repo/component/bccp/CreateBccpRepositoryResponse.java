package org.oagi.score.repo.component.bccp;

import java.math.BigInteger;

public class CreateBccpRepositoryResponse {

    private final BigInteger bccpManifestId;

    public CreateBccpRepositoryResponse(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
