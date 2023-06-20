package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class BieForOasDocRequest extends Request {
    private BigInteger bieForOasDocId;
    private BigInteger topLevelAsbiepId;
    private BigInteger oasDocId;
    private BigInteger bizCtxId;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;

    public BieForOasDocRequest() {
    }

    public BieForOasDocRequest(BigInteger bieForOasDocId, BigInteger topLevelAsbiepId, BigInteger oasDocId, BigInteger bizCtxId, Collection<String> updaterUsernameList, LocalDateTime updateStartDate, LocalDateTime updateEndDate) {
        this.bieForOasDocId = bieForOasDocId;
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.oasDocId = oasDocId;
        this.bizCtxId = bizCtxId;
        this.updaterUsernameList = updaterUsernameList;
        this.updateStartDate = updateStartDate;
        this.updateEndDate = updateEndDate;
    }

    public BigInteger getBieForOasDocId() {
        return bieForOasDocId;
    }

    public void setBieForOasDocId(BigInteger bieForOasDocId) {
        this.bieForOasDocId = bieForOasDocId;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public BigInteger getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(BigInteger bizCtxId) {
        this.bizCtxId = bizCtxId;
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

    @Override
    public String toString() {
        return "BieForOasDocRequest{" +
                "bieForOasDocId=" + bieForOasDocId +
                ", topLevelAsbiepId=" + topLevelAsbiepId +
                ", oasDocId=" + oasDocId +
                ", bizCtxId=" + bizCtxId +
                ", updaterUsernameList=" + updaterUsernameList +
                ", updateStartDate=" + updateStartDate +
                ", updateEndDate=" + updateEndDate +
                '}';
    }
}
