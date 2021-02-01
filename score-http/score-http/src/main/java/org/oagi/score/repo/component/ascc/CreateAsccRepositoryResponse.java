package org.oagi.score.repo.component.ascc;

import java.math.BigInteger;

public class CreateAsccRepositoryResponse {

    private final BigInteger asccManifestId;

    public CreateAsccRepositoryResponse(BigInteger asccManifestId) {
        this.asccManifestId = asccManifestId;
    }

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }
}
