package org.oagi.score.repo.component.code_list;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class ReviseCodeListRepositoryRequest extends RepositoryRequest {

    private final BigInteger codeListManifestId;

    public ReviseCodeListRepositoryRequest(AuthenticatedPrincipal user,
                                           BigInteger codeListManifestId,
                                           LocalDateTime localDateTime) {
        super(user, localDateTime);
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }
}
