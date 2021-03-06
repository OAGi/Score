package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class CreateCodeListRepositoryResponse {

    private final BigInteger codeListManifestId;

    public CreateCodeListRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
