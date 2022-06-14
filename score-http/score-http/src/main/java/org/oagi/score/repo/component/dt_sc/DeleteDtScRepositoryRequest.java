package org.oagi.score.repo.component.dt_sc;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class DeleteDtScRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtScManifestId;

    public DeleteDtScRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger dtScManifestId) {
        super(user);
        this.dtScManifestId = dtScManifestId;
    }

    public DeleteDtScRepositoryRequest(AuthenticatedPrincipal user,
                                       LocalDateTime localDateTime,
                                       BigInteger dtScManifestId) {
        super(user, localDateTime);
        this.dtScManifestId = dtScManifestId;
    }

    public BigInteger getDtScManifestId() {
        return dtScManifestId;
    }
}
