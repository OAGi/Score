package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class PurgeAsccpRepositoryResponse {

    private final BigInteger asccpManifestId;

    private final Throwable error;

    public PurgeAsccpRepositoryResponse(BigInteger asccpManifestId) {
        this(asccpManifestId, null);
    }

    public PurgeAsccpRepositoryResponse(BigInteger asccpManifestId, Throwable error) {
        this.asccpManifestId = asccpManifestId;
        this.error = error;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public Throwable getError() {
        return error;
    }
}
