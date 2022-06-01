package org.oagi.score.repo.component.dt_sc;

import java.math.BigInteger;

public class DeleteDtScRepositoryResponse {

    private final BigInteger dtScManifestId;

    public DeleteDtScRepositoryResponse(BigInteger dtScManifestId) {
        this.dtScManifestId = dtScManifestId;
    }

    public BigInteger getDtScManifestId() {
        return dtScManifestId;
    }
}
