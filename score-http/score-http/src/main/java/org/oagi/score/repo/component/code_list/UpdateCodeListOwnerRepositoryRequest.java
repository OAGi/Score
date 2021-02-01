package org.oagi.score.repo.component.code_list;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class UpdateCodeListOwnerRepositoryRequest extends RepositoryRequest {

    private final BigInteger codeListManifestId;
    private final BigInteger ownerId;

    public UpdateCodeListOwnerRepositoryRequest(AuthenticatedPrincipal user,
                                                BigInteger codeListManifestId,
                                                BigInteger ownerId) {
        super(user);
        this.codeListManifestId = codeListManifestId;
        this.ownerId = ownerId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }

    public BigInteger getOwnerId() {
        return ownerId;
    }
}
