package org.oagi.score.service.bie;

import lombok.Data;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

@Data
public class FindTargetAsccpManifestRequest {

    private ScoreUser requester;

    private BigInteger topLevelAsbiepId;

    private BigInteger targetReleaseId;

    private boolean includingBieDocument;

    public FindTargetAsccpManifestRequest() {
    }

    public FindTargetAsccpManifestRequest(ScoreUser requester) {
        this.requester = requester;
    }

    public FindTargetAsccpManifestRequest withTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.setTopLevelAsbiepId(topLevelAsbiepId);
        return this;
    }

    public FindTargetAsccpManifestRequest withTargetReleaseId(BigInteger targetReleaseId) {
        this.setTargetReleaseId(targetReleaseId);
        return this;
    }

    public FindTargetAsccpManifestRequest withIncludingBieDocument(boolean includingBieDocument) {
        this.setIncludingBieDocument(includingBieDocument);
        return this;
    }
}
