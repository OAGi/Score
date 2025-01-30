package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CreateBiePackageRequest {

    private ScoreUser requester;

    private BigInteger libraryId;

    private String versionId;

    private String versionName;

    private String description;

    public CreateBiePackageRequest() {
    }

    public CreateBiePackageRequest(ScoreUser requester) {
        setRequester(requester);
    }

    public ScoreUser getRequester() {
        return requester;
    }

    public void setRequester(ScoreUser requester) {
        this.requester = requester;
    }

    public BigInteger getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(BigInteger libraryId) {
        this.libraryId = libraryId;
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

}
