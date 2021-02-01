package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class UpdateAsccpStateRepositoryResponse {

    private final BigInteger asccpManifestId;

    public UpdateAsccpStateRepositoryResponse(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }
}
