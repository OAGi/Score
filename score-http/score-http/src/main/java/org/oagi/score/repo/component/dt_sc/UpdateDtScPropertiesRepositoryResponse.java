package org.oagi.score.repo.component.dt_sc;

import java.math.BigInteger;

public class UpdateDtScPropertiesRepositoryResponse {

    private final BigInteger dtScManifestId;

    public UpdateDtScPropertiesRepositoryResponse(BigInteger dtScManifestId) {
        this.dtScManifestId = dtScManifestId;
    }

    public BigInteger getDtScManifestId() {
        return dtScManifestId;
    }
}
