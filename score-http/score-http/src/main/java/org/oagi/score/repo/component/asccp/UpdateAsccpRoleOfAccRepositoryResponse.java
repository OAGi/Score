package org.oagi.score.repo.component.asccp;

import java.math.BigInteger;

public class UpdateAsccpRoleOfAccRepositoryResponse {

    private final BigInteger asccpManifestId;
    private final String den;

    public UpdateAsccpRoleOfAccRepositoryResponse(BigInteger asccpManifestId, String den) {
        this.asccpManifestId = asccpManifestId;
        this.den = den;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }
    public String getDen() {
        return den;
    }
}
