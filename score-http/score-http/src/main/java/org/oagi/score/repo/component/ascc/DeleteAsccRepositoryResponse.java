package org.oagi.score.repo.component.ascc;

import java.math.BigInteger;

public class DeleteAsccRepositoryResponse {

    private final BigInteger asccManifestId;

    public DeleteAsccRepositoryResponse(BigInteger asccManifestId) {
        this.asccManifestId = asccManifestId;
    }

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }
}
