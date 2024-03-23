package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.bie.model.BiePackageState;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class UpdateBiePackageRequest {

    private ScoreUser requester;

    private BigInteger biePackageId;

    private String versionId;

    private String versionName;

    private String description;

    private BiePackageState state;

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public BigInteger getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BiePackageState getState() {
        return state;
    }

    public void setState(BiePackageState state) {
        this.state = state;
    }
}
