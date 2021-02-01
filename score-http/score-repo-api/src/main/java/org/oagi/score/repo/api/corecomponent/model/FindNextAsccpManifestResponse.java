package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class FindNextAsccpManifestResponse extends Response {

    private final BigInteger nextAsccpManifestId;

    public FindNextAsccpManifestResponse(BigInteger nextAsccpManifestId) {
        this.nextAsccpManifestId = nextAsccpManifestId;
    }

    public BigInteger getNextAsccpManifestId() {
        return nextAsccpManifestId;
    }

}
