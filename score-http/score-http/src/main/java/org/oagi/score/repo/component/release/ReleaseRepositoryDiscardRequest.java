package org.oagi.score.repo.component.release;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class ReleaseRepositoryDiscardRequest extends RepositoryRequest {

    private final BigInteger releaseId;

    public ReleaseRepositoryDiscardRequest(AuthenticatedPrincipal user, BigInteger releaseId) {
        super(user);
        this.releaseId = releaseId;
    }

    public ReleaseRepositoryDiscardRequest(AuthenticatedPrincipal user,
                                           LocalDateTime localDateTime,
                                           BigInteger releaseId) {
        super(user, localDateTime);
        this.releaseId = releaseId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }
}
