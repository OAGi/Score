package org.oagi.score.repo.component.asccp;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class UpdateAsccpOwnerRepositoryRequest extends RepositoryRequest {

    private final BigInteger asccpManifestId;
    private final BigInteger ownerId;

    public UpdateAsccpOwnerRepositoryRequest(AuthenticatedPrincipal user,
                                             BigInteger asccpManifestId,
                                             BigInteger ownerId) {
        super(user);
        this.asccpManifestId = asccpManifestId;
        this.ownerId = ownerId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public BigInteger getOwnerId() {
        return ownerId;
    }
}
