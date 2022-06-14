package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class PurgeAsccpRepositoryResponse {

    private final BigInteger asccpManifestId;

    public PurgeAsccpRepositoryResponse(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }
}
