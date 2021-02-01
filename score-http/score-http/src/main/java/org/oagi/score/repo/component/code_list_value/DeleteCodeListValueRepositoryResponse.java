package org.oagi.score.repo.component.code_list_value;

import java.math.BigInteger;

public class DeleteCodeListValueRepositoryResponse {

    private final BigInteger codeListValueManifestId;

    public DeleteCodeListValueRepositoryResponse(BigInteger codeListValueManifestId) {
        this.codeListValueManifestId = codeListValueManifestId;
    }

    public BigInteger getCodeListValueManifestId() {
        return codeListValueManifestId;
    }
}
