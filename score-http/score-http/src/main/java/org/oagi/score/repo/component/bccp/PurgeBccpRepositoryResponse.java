package org.oagi.score.repo.component.bccp;

import java.math.BigInteger;

public class PurgeBccpRepositoryResponse {

    private final BigInteger bccpManifestId;

    private final Throwable error;

    public PurgeBccpRepositoryResponse(BigInteger bccpManifestId) {
        this(bccpManifestId, null);
    }

    public PurgeBccpRepositoryResponse(BigInteger bccpManifestId, Throwable error) {
        this.bccpManifestId = bccpManifestId;
        this.error = error;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }

    public Throwable getError() {
        return error;
    }

}
