package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class UpdateAsccpPropertiesRepositoryResponse {

    private final BigInteger asccpManifestId;

    public UpdateAsccpPropertiesRepositoryResponse(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }
}
