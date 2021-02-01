package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class DeleteCodeListRepositoryResponse {

    private final BigInteger codeListManifestId;

    public DeleteCodeListRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
