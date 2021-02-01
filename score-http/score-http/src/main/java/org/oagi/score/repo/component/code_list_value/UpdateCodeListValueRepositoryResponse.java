package org.oagi.score.repo.component.code_list_value;

import java.math.BigInteger;

public class UpdateCodeListValueRepositoryResponse {

    private final BigInteger codeListValueManifestId;

    public UpdateCodeListValueRepositoryResponse(BigInteger codeListValueManifestId) {
        this.codeListValueManifestId = codeListValueManifestId;
    }

    public BigInteger getBccpManifestId() {
        return codeListValueManifestId;
    }
}
