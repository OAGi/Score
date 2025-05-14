package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.WrappedAsbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.WrappedAsbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.WrappedBbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.WrappedBbieSc;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CreateBieRequest extends Request {

    private WrappedAsbiep topLevelAsbiep;

    private String status;

    private String version;

    private List<BusinessContextId> bizCtxIds;

    private List<WrappedAsbie> asbieList;

    private List<WrappedBbie> bbieList;

    private List<WrappedBbieSc> bbieScList;

    private TopLevelAsbiepId sourceTopLevelAsbiepId;

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

    public List<BusinessContextId> getBizCtxIds() {
        return (bizCtxIds != null) ? bizCtxIds : Collections.emptyList();
    }

    public void setBizCtxIds(List<BusinessContextId> bizCtxIds) {
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

    public TopLevelAsbiepId getSourceTopLevelAsbiepId() {
        return sourceTopLevelAsbiepId;
    }

    public void setSourceTopLevelAsbiepId(TopLevelAsbiepId sourceTopLevelAsbiepId) {
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
