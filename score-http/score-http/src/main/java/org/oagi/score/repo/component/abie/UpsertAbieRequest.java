package org.oagi.score.repo.component.abie;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpsertAbieRequest extends RepositoryRequest {

    private final BigInteger topLevelAsbiepId;
    private final AbieNode.Abie abie;

    public UpsertAbieRequest(AuthenticatedPrincipal user, LocalDateTime localDateTime,
                             BigInteger topLevelAsbiepId, AbieNode.Abie abie) {
        super(user, localDateTime);
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.abie = abie;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public AbieNode.Abie getAbie() {
        return abie;
    }
}
