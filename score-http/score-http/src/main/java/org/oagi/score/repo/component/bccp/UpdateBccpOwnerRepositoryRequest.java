package org.oagi.score.repo.component.bccp;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class UpdateBccpOwnerRepositoryRequest extends RepositoryRequest {

    private final BigInteger bccpManifestId;
    private final BigInteger ownerId;

    public UpdateBccpOwnerRepositoryRequest(AuthenticatedPrincipal user,
                                            BigInteger bccpManifestId,
                                            BigInteger ownerId) {
        super(user);
        this.bccpManifestId = bccpManifestId;
        this.ownerId = ownerId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }

    public BigInteger getOwnerId() {
        return ownerId;
    }
}
