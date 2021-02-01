package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class CancelRevisionCodeListRepositoryResponse {

    private final BigInteger codeListManifestId;

    public CancelRevisionCodeListRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
