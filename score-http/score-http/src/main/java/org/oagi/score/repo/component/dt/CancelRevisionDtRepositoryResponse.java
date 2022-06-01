package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class CancelRevisionDtRepositoryResponse {

    private final BigInteger dtManifestId;

    public CancelRevisionDtRepositoryResponse(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
