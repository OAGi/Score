package org.oagi.score.repo.component.bcc;

import java.math.BigInteger;

public class RefactorBccRepositoryResponse {

    private final BigInteger bccManifestId;

    public RefactorBccRepositoryResponse(BigInteger bccManifestId) {
        this.bccManifestId = bccManifestId;
    }

    public BigInteger getBccManifestId() {
        return bccManifestId;
    }
}
