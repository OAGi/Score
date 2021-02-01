package org.oagi.score.repo.component.asbie;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpsertAsbieRequest extends RepositoryRequest {

    private final BigInteger topLevelAsbiepId;
    private final AsbieNode.Asbie asbie;

    public UpsertAsbieRequest(AuthenticatedPrincipal user, LocalDateTime localDateTime,
                              BigInteger topLevelAsbiepId, AsbieNode.Asbie asbie) {
        super(user, localDateTime);
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.asbie = asbie;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public AsbieNode.Asbie getAsbie() {
        return asbie;
    }
}
