package org.oagi.score.repo.component.code_list;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class CreateCodeListRepositoryRequest extends RepositoryRequest {

    private final BigInteger basedCodeListManifestId;
    private final BigInteger releaseId;

    private String initialName = "Code List";

    public CreateCodeListRepositoryRequest(AuthenticatedPrincipal user,
                                           LocalDateTime localDateTime,
                                           BigInteger basedCodeListManifestId,
                                           BigInteger releaseId) {
        super(user, localDateTime);
        this.basedCodeListManifestId = basedCodeListManifestId;
        this.releaseId = releaseId;
    }

    public String getInitialName() {
        return initialName;
    }

    public BigInteger getBasedCodeListManifestId() {
        return this.basedCodeListManifestId;
    }

    public BigInteger getReleaseId() {
        return this.releaseId;
    }

    public void setInitialName(String initialName) {
        this.initialName = initialName;
    }
}
