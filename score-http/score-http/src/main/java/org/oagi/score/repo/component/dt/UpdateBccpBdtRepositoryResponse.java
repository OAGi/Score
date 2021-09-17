package org.oagi.score.repo.component.dt;

import java.math.BigInteger;

public class UpdateBccpBdtRepositoryResponse {

    private final BigInteger bccpManifestId;

    private final String den;

    public UpdateBccpBdtRepositoryResponse(BigInteger bccpManifestId, String den) {
        this.bccpManifestId = bccpManifestId;
        this.den = den;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }

    public String getDen() {
        return den;
    }
}
