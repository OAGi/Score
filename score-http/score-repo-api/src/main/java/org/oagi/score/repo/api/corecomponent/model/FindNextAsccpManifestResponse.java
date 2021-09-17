package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class FindNextAsccpManifestResponse extends Response {

    private final BigInteger nextAsccpManifestId;
    private final String releaseNum;

    public FindNextAsccpManifestResponse(BigInteger nextAsccpManifestId, String releaseNum) {
        this.nextAsccpManifestId = nextAsccpManifestId;
        this.releaseNum = releaseNum;
    }

    public BigInteger getNextAsccpManifestId() {
        return nextAsccpManifestId;
    }

    public String getReleaseNum() {
        return releaseNum;
    }
}
