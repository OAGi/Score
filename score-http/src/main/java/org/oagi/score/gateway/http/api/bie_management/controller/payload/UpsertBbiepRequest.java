package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepNode;

public class UpsertBbiepRequest {

    private final TopLevelAsbiepId topLevelAsbiepId;
    private final BbiepNode.Bbiep bbiep;

    public UpsertBbiepRequest(TopLevelAsbiepId topLevelAsbiepId, BbiepNode.Bbiep bbiep) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.bbiep = bbiep;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public BbiepNode.Bbiep getBbiep() {
        return bbiep;
    }
}
