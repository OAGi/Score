package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class ReviseAsccpRepositoryResponse {

    private final BigInteger asccpManifestId;

    public ReviseAsccpRepositoryResponse(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }
}
