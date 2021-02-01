package org.oagi.score.repo.component.bcc;

import java.math.BigInteger;

public class UpdateBccPropertiesRepositoryResponse {

    private final BigInteger bccManifestId;

    public UpdateBccPropertiesRepositoryResponse(BigInteger bccManifestId) {
        this.bccManifestId = bccManifestId;
    }

    public BigInteger getBccManifestId() {
        return bccManifestId;
    }
}
