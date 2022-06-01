package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class ReviseDtRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;

    public ReviseDtRepositoryRequest(AuthenticatedPrincipal user,
                                     BigInteger dtManifestId) {
        super(user);
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
