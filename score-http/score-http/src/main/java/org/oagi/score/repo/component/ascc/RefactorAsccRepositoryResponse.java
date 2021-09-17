package org.oagi.score.repo.component.ascc;

import java.math.BigInteger;

public class RefactorAsccRepositoryResponse {

    private final BigInteger asccManifestId;

    public RefactorAsccRepositoryResponse(BigInteger asccManifestId) {
        this.asccManifestId = asccManifestId;
    }

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }
}
