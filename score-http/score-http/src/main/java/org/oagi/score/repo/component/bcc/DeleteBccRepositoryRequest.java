package org.oagi.score.repo.component.bcc;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class DeleteBccRepositoryRequest extends RepositoryRequest {

    private final BigInteger bccManifestId;

    public DeleteBccRepositoryRequest(AuthenticatedPrincipal user,
                                      BigInteger bccManifestId) {
        super(user);
        this.bccManifestId = bccManifestId;
    }

    public DeleteBccRepositoryRequest(AuthenticatedPrincipal user,
                                      LocalDateTime localDateTime,
                                      BigInteger bccManifestId) {
        super(user, localDateTime);
        this.bccManifestId = bccManifestId;
    }

    public BigInteger getBccManifestId() {
        return bccManifestId;
    }
}
