package org.oagi.score.repo.component.ascc;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class DeleteAsccRepositoryRequest extends RepositoryRequest {

    private final BigInteger asccManifestId;

    public DeleteAsccRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger asccManifestId) {
        super(user);
        this.asccManifestId = asccManifestId;
    }

    public DeleteAsccRepositoryRequest(AuthenticatedPrincipal user,
                                       LocalDateTime localDateTime,
                                       BigInteger asccManifestId) {
        super(user, localDateTime);
        this.asccManifestId = asccManifestId;
    }

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }
}
