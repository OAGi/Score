package org.oagi.score.repo.component.acc;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.gateway.http.api.cc_management.data.CcId;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateSeqKeyRequest extends RepositoryRequest {

    private final BigInteger accManifestId;
    private final Pair<CcId, CcId> itemAfterPair;

    public UpdateSeqKeyRequest(AuthenticatedPrincipal user,
                               BigInteger accManifestId,
                               Pair<CcId, CcId> itemAfterPair) {
        super(user);
        this.accManifestId = accManifestId;
        this.itemAfterPair = itemAfterPair;
    }

    public UpdateSeqKeyRequest(AuthenticatedPrincipal user,
                               LocalDateTime localDateTime,
                               BigInteger accManifestId,
                               Pair<CcId, CcId> itemAfterPair) {
        super(user, localDateTime);
        this.accManifestId = accManifestId;
        this.itemAfterPair = itemAfterPair;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }

    public CcId getItem() {
        return this.itemAfterPair.getLeft();
    }

    public CcId getAfter() {
        return this.itemAfterPair.getRight();
    }
}
