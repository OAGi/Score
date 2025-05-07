package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

public class UpliftBiePackageRequest {

    private ScoreUser requester;

    private BiePackageId biePackageId;

    private ReleaseId targetReleaseId;

    public UpliftBiePackageRequest() {

    }

    public UpliftBiePackageRequest(ScoreUser requester) {
        setRequester(requester);
    }

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public BiePackageId getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BiePackageId biePackageId) {
        this.biePackageId = biePackageId;
    }

    public ReleaseId getTargetReleaseId() {
        return targetReleaseId;
    }

    public void setTargetReleaseId(ReleaseId targetReleaseId) {
        this.targetReleaseId = targetReleaseId;
    }
}
