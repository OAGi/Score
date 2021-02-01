package org.oagi.score.repo.component.code_list;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class CancelRevisionCodeListRepositoryRequest extends RepositoryRequest {

    private final BigInteger codeListManifestId;

    public CancelRevisionCodeListRepositoryRequest(AuthenticatedPrincipal user,
                                                   BigInteger codeListManifestId) {
        super(user);
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
