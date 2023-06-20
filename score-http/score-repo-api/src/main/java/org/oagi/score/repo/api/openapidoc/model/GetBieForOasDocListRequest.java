package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class GetBieForOasDocListRequest extends PaginationRequest<BieForOasDoc> {
    private Collection<BigInteger> topLevelAsbiepIdList;
    private BigInteger oasDocId;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;

    public GetBieForOasDocListRequest(ScoreUser requester) {
        super(requester, BieForOasDoc.class);
    }

    public GetBieForOasDocListRequest(ScoreUser requester, Class<BieForOasDoc> type) {
        super(requester, type);
    }

    public Collection<BigInteger> getTopLevelAsbiepIdList() {
        return topLevelAsbiepIdList;
    }

    public void setTopLevelAsbiepIdList(Collection<BigInteger> topLevelAsbiepIdList) {
        this.topLevelAsbiepIdList = topLevelAsbiepIdList;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
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
