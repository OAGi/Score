package org.oagi.score.repo.component.code_list;

import java.math.BigInteger;

public class UpdateCodeListPropertiesRepositoryResponse {

    private final BigInteger codeListManifestId;

    public UpdateCodeListPropertiesRepositoryResponse(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
