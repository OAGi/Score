package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class FindNextAsccpManifestRequest extends Request {

    private BigInteger asccpManifestId;

    private BigInteger topLevelAsbiepId;

    private BigInteger nextReleaseId;

    public FindNextAsccpManifestRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public void setAsccpManifestId(BigInteger asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
    }

    public FindNextAsccpManifestRequest withAsccpManifestId(BigInteger asccpManifestId) {
        setAsccpManifestId(asccpManifestId);
        return this;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public FindNextAsccpManifestRequest withTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        setTopLevelAsbiepId(topLevelAsbiepId);
        return this;
    }

    public BigInteger getNextReleaseId() {
        return nextReleaseId;
    }

    public void setNextReleaseId(BigInteger nextReleaseId) {
        this.nextReleaseId = nextReleaseId;
    }

    public FindNextAsccpManifestRequest withNextReleaseId(BigInteger nextReleaseId) {
        setNextReleaseId(nextReleaseId);
        return this;
    }
}
