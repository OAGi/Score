package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class UpdateCodeListStateRepositoryResponse {

    private final BigInteger codeListManifestId;

    public UpdateCodeListStateRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
