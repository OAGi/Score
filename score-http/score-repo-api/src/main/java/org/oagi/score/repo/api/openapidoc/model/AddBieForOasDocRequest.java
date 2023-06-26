package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class AddBieForOasDocRequest extends Request {
    private BigInteger topLevelAsbiepId;
    private BigInteger oasDocId;
    private BigInteger oasTagId;
    private BigInteger oasMessageBodyId;
    private BigInteger oasResourceId;
    private BigInteger oasOperationId;
    private BigInteger oasRequestId;
    private BigInteger oasResponseId;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;

    public AddBieForOasDocRequest() {
    }

    public AddBieForOasDocRequest(ScoreUser requester) {
        super(requester);
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

    public BigInteger getOasTagId() {
        return oasTagId;
    }

    public void setOasTagId(BigInteger oasTagId) {
        this.oasTagId = oasTagId;
    }

    public BigInteger getOasMessageBodyId() {
        return oasMessageBodyId;
    }

    public void setOasMessageBodyId(BigInteger oasMessageBodyId) {
        this.oasMessageBodyId = oasMessageBodyId;
    }

    public BigInteger getOasResourceId() {
        return oasResourceId;
    }

    public void setOasResourceId(BigInteger oasResourceId) {
        this.oasResourceId = oasResourceId;
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(BigInteger oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public BigInteger getOasRequestId() {
        return oasRequestId;
    }

    public void setOasRequestId(BigInteger oasRequestId) {
        this.oasRequestId = oasRequestId;
    }

    public BigInteger getOasResponseId() {
        return oasResponseId;
    }

    public void setOasResponseId(BigInteger oasResponseId) {
        this.oasResponseId = oasResponseId;
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
        return "AddBieForOasDocRequest{" +
                "topLevelAsbiepId=" + topLevelAsbiepId +
                ", oasDocId=" + oasDocId +
                ", oasTagId=" + oasTagId +
                ", oasMessageBodyId=" + oasMessageBodyId +
                ", oasResourceId=" + oasResourceId +
                ", oasOperationId=" + oasOperationId +
                ", oasRequestId=" + oasRequestId +
                ", oasResponseId=" + oasResponseId +
                ", updaterUsernameList=" + updaterUsernameList +
                ", updateStartDate=" + updateStartDate +
                ", updateEndDate=" + updateEndDate +
                '}';
    }
}
