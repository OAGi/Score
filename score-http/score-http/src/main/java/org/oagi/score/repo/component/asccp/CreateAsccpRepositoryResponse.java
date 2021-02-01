package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class CreateAsccpRepositoryResponse {

    private final BigInteger asccpManifestId;

    public CreateAsccpRepositoryResponse(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }
}
