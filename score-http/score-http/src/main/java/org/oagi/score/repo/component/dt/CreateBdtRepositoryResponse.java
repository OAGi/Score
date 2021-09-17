package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class CreateBdtRepositoryResponse {

    private final BigInteger bdtManifestId;

    public CreateBdtRepositoryResponse(BigInteger bdtManifestId) {
        this.bdtManifestId = bdtManifestId;
    }

    public BigInteger getBdtManifestId() {
        return bdtManifestId;
    }
}
