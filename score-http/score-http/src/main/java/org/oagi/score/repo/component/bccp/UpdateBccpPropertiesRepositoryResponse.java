package org.oagi.score.repo.component.bccp;

import java.math.BigInteger;

public class UpdateBccpPropertiesRepositoryResponse {

    private final BigInteger bccpManifestId;

    public UpdateBccpPropertiesRepositoryResponse(BigInteger bccpManifestId) {
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
