package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class PurgeCodeListRepositoryResponse {

    private final BigInteger codeListManifestId;

    public PurgeCodeListRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
