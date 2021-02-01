package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class CancelRevisionAsccpRepositoryResponse {

    private final BigInteger asccpManifestId;

    public CancelRevisionAsccpRepositoryResponse(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }
}
