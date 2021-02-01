package org.oagi.score.repo.component.code_list_value;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class DeleteCodeListValueRepositoryRequest extends RepositoryRequest {

    private final BigInteger codeListValueManifestId;

    public DeleteCodeListValueRepositoryRequest(AuthenticatedPrincipal user,
                                                BigInteger codeListValueManifestId) {
        super(user);
        this.codeListValueManifestId = codeListValueManifestId;
    }

    public DeleteCodeListValueRepositoryRequest(AuthenticatedPrincipal user,
                                                LocalDateTime localDateTime,
                                                BigInteger codeListValueManifestId) {
        super(user, localDateTime);
        this.codeListValueManifestId = codeListValueManifestId;
    }

    public BigInteger getCodeListValueManifestId() {
        return codeListValueManifestId;
    }
}
