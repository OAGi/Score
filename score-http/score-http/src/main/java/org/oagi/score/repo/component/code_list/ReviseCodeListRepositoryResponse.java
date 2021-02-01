package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class ReviseCodeListRepositoryResponse {

    private final BigInteger codeListManifestId;

    public ReviseCodeListRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
