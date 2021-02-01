package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetCcPackageRequest extends Request {

    private BigInteger asccpManifestId;

    public GetCcPackageRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public void setAsccpManifestId(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public GetCcPackageRequest withAsccpManifestId(BigInteger asccpManifestId) {
        setAsccpManifestId(asccpManifestId);
        return this;
    }

}
