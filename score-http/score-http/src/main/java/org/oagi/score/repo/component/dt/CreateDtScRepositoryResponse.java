package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class CreateDtScRepositoryResponse {

    private final BigInteger dtScManifestId;

    public CreateDtScRepositoryResponse(BigInteger dtScManifestId) {
        this.dtScManifestId = dtScManifestId;
    }

    public BigInteger getDtScManifestId() {
        return dtScManifestId;
    }
}
