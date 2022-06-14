package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class UpdateDtStateRepositoryResponse {

    private final BigInteger dtManifestId;

    public UpdateDtStateRepositoryResponse(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
