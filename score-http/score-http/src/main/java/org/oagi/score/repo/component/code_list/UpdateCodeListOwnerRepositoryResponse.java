package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class UpdateCodeListOwnerRepositoryResponse {

    private final BigInteger codeListManifestId;

    public UpdateCodeListOwnerRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
