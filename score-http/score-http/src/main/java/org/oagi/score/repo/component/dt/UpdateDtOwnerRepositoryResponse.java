package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class UpdateDtOwnerRepositoryResponse {

    private final BigInteger dtManifestId;

    public UpdateDtOwnerRepositoryResponse(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
