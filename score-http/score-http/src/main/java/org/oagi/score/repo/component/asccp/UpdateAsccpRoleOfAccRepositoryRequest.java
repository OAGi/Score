package org.oagi.score.repo.component.asccp;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateAsccpRoleOfAccRepositoryRequest extends RepositoryRequest {

    private final BigInteger asccpManifestId;
    private final BigInteger roleOfAccManifestId;

    public UpdateAsccpRoleOfAccRepositoryRequest(AuthenticatedPrincipal user,
                                                 BigInteger asccpManifestId,
                                                 BigInteger roleOfAccManifestId) {
        super(user);
        this.asccpManifestId = asccpManifestId;
        this.roleOfAccManifestId = roleOfAccManifestId;
    }

    public UpdateAsccpRoleOfAccRepositoryRequest(AuthenticatedPrincipal user,
                                                 LocalDateTime localDateTime,
                                                 BigInteger asccpManifestId,
                                                 BigInteger roleOfAccManifestId) {
        super(user, localDateTime);
        this.asccpManifestId = asccpManifestId;
        this.roleOfAccManifestId = roleOfAccManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public BigInteger getRoleOfAccManifestId() {
        return roleOfAccManifestId;
    }
}
