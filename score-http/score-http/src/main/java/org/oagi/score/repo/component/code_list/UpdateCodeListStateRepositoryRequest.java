package org.oagi.score.repo.component.code_list;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.service.common.data.CcState;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateCodeListStateRepositoryRequest extends RepositoryRequest {

    private final BigInteger codeListManifestId;
    private final CcState state;

    public UpdateCodeListStateRepositoryRequest(AuthenticatedPrincipal user,
                                                BigInteger codeListManifestId,
                                                CcState state) {
        super(user);
        this.codeListManifestId = codeListManifestId;
        this.state = state;
    }

    public UpdateCodeListStateRepositoryRequest(AuthenticatedPrincipal user,
                                                LocalDateTime localDateTime,
                                                BigInteger codeListManifestId,
                                                CcState state) {
        super(user, localDateTime);
        this.codeListManifestId = codeListManifestId;
        this.state = state;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }

    public CcState getState() {
        return state;
    }
}
