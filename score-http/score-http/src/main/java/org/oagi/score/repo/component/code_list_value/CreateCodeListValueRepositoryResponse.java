package org.oagi.score.repo.component.code_list_value;

import java.math.BigInteger;

public class CreateCodeListValueRepositoryResponse {

    private final BigInteger codeListValueManifestId;

    public CreateCodeListValueRepositoryResponse(BigInteger codeListValueManifestId) {
        this.codeListValueManifestId = codeListValueManifestId;
    }

    public BigInteger getCodeListValueManifestId() {
        return codeListValueManifestId;
    }
}
