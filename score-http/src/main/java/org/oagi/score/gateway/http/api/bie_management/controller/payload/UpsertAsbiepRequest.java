package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepNode;

public class UpsertAsbiepRequest {

    private final TopLevelAsbiepId topLevelAsbiepId;
    private final AsbiepNode.Asbiep asbiep;

    private AbieId roleOfAbieId;
    private TopLevelAsbiepId refTopLevelAsbiepId;
    private boolean refTopLevelAsbiepIdNull;

    public UpsertAsbiepRequest(TopLevelAsbiepId topLevelAsbiepId, AsbiepNode.Asbiep asbiep) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.asbiep = asbiep;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public AsbiepNode.Asbiep getAsbiep() {
        return asbiep;
    }

    public AbieId getRoleOfAbieId() {
        return roleOfAbieId;
    }

    public void setRoleOfAbieId(AbieId roleOfAbieId) {
        this.roleOfAbieId = roleOfAbieId;
    }

    public TopLevelAsbiepId getRefTopLevelAsbiepId() {
        return refTopLevelAsbiepId;
    }

    public void setRefTopLevelAsbiepId(TopLevelAsbiepId refTopLevelAsbiepId) {
        this.refTopLevelAsbiepId = refTopLevelAsbiepId;
    }

    public boolean isRefTopLevelAsbiepIdNull() {
        return refTopLevelAsbiepIdNull;
    }

    public void setRefTopLevelAsbiepIdNull(boolean refTopLevelAsbiepIdNull) {
        this.refTopLevelAsbiepIdNull = refTopLevelAsbiepIdNull;
    }
}
