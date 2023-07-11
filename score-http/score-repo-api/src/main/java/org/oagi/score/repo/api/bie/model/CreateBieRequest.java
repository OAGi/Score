package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CreateBieRequest extends Request {

    private WrappedAsbiep topLevelAsbiep;

    private String status;

    private String version;

    private List<BigInteger> bizCtxIds;

    private List<WrappedAsbie> asbieList;

    private List<WrappedBbie> bbieList;

    private List<WrappedBbieSc> bbieScList;

    private BigInteger sourceTopLevelAsbiepId;

    private String sourceAction;

    private LocalDateTime sourceTimestamp;

    public CreateBieRequest(ScoreUser requester) {
        super(requester);
    }

    public WrappedAsbiep getTopLevelAsbiep() {
        return topLevelAsbiep;
    }

    public void setTopLevelAsbiep(WrappedAsbiep topLevelAsbiep) {
        this.topLevelAsbiep = topLevelAsbiep;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<BigInteger> getBizCtxIds() {
        return (bizCtxIds != null) ? bizCtxIds : Collections.emptyList();
    }

    public void setBizCtxIds(List<BigInteger> bizCtxIds) {
        this.bizCtxIds = bizCtxIds;
    }

    public List<WrappedAsbie> getAsbieList() {
        return (asbieList != null) ? asbieList : Collections.emptyList();
    }

    public void setAsbieList(List<WrappedAsbie> asbieList) {
        this.asbieList = asbieList;
    }

    public List<WrappedBbie> getBbieList() {
        return (bbieList != null) ? bbieList : Collections.emptyList();
    }

    public void setBbieList(List<WrappedBbie> bbieList) {
        this.bbieList = bbieList;
    }

    public List<WrappedBbieSc> getBbieScList() {
        return (bbieScList != null) ? bbieScList : Collections.emptyList();
    }

    public void setBbieScList(List<WrappedBbieSc> bbieScList) {
        this.bbieScList = bbieScList;
    }

    public BigInteger getSourceTopLevelAsbiepId() {
        return sourceTopLevelAsbiepId;
    }

    public void setSourceTopLevelAsbiepId(BigInteger sourceTopLevelAsbiepId) {
        this.sourceTopLevelAsbiepId = sourceTopLevelAsbiepId;
    }

    public String getSourceAction() {
        return sourceAction;
    }

    public void setSourceAction(String sourceAction) {
        this.sourceAction = sourceAction;
    }

    public LocalDateTime getSourceTimestamp() {
        return sourceTimestamp;
    }

    public void setSourceTimestamp(LocalDateTime sourceTimestamp) {
        this.sourceTimestamp = sourceTimestamp;
    }
}
