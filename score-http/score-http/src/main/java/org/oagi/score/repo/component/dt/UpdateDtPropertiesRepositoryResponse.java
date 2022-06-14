package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class UpdateDtPropertiesRepositoryResponse {

    private final BigInteger dtManifestId;

    public UpdateDtPropertiesRepositoryResponse(BigInteger dtManifestId) {
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
