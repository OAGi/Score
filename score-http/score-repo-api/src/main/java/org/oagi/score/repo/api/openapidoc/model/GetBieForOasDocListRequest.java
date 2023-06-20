package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.businessterm.model.BusinessTerm;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class GetBieForOasDocListRequest extends PaginationRequest<BieForOasDoc> {
    private Collection<BigInteger> bieForOasDocIdList;
    private String bizCtxName;
    private String propertyTerm;
    private String release;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;

    public GetBieForOasDocListRequest(ScoreUser requester) {
        super(requester, BieForOasDoc.class);
    }

    public GetBieForOasDocListRequest(ScoreUser requester, Class<BieForOasDoc> type) {
        super(requester, type);
    }
    public Collection<BigInteger> getBieForOasDocIdList() {
        return bieForOasDocIdList;
    }

    public void setBieForOasDocIdList(Collection<BigInteger> bieForOasDocIdList) {
        this.bieForOasDocIdList = bieForOasDocIdList;
    }

    public String getBizCtxName() {
        return bizCtxName;
    }

    public void setBizCtxName(String bizCtxName) {
        this.bizCtxName = bizCtxName;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
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
