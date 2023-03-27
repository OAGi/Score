package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class CreateDtScRepositoryRequest extends RepositoryRequest {

    private final BigInteger ownerDdtManifestId;


    public CreateDtScRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger ownerDdtManifestId) {
        super(user);
        this.ownerDdtManifestId = ownerDdtManifestId;
    }

    public BigInteger getOwnerDdtManifestId() {
        return ownerDdtManifestId;
    }
}
