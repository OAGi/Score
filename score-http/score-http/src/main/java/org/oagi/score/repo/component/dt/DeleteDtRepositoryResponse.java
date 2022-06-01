package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class DeleteDtRepositoryResponse {

    private final BigInteger dtManifestId;

    public DeleteDtRepositoryResponse(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
