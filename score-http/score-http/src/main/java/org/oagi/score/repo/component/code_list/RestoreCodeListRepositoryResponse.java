package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class RestoreCodeListRepositoryResponse {

    private final BigInteger codeListManifestId;

    public RestoreCodeListRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
