package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class GetOasDocListRequest extends PaginationRequest<OasDoc> {
    private Collection<BigInteger> oasDocIdList;
    private String title;
    private String openAPIVersion;
    private String version;
    private String licenseName;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;

    public GetOasDocListRequest(ScoreUser requester) {
        super(requester, OasDoc.class);
    }

    public Collection<BigInteger> getOasDocIdList() {
        return oasDocIdList;
    }

    public void setOasDocIdList(Collection<BigInteger> oasDocIdList) {
        this.oasDocIdList = oasDocIdList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOpenAPIVersion() {
        return openAPIVersion;
    }

    public void setOpenAPIVersion(String openAPIVersion) {
        this.openAPIVersion = openAPIVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public Collection<String> getUpdaterUsernameList() {
        return updaterUsernameList;
    }

    public void setUpdaterUsernameList(Collection<String> updaterUsernameList) {
        this.updaterUsernameList = updaterUsernameList;
    }

    public LocalDateTime getUpdateStartDate() {
        return updateStartDate;
    }

    public void setUpdateStartDate(LocalDateTime updateStartDate) {
        this.updateStartDate = updateStartDate;
    }

    public LocalDateTime getUpdateEndDate() {
        return updateEndDate;
    }

    public void setUpdateEndDate(LocalDateTime updateEndDate) {
        this.updateEndDate = updateEndDate;
    }
}
