package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScNode;

public class UpsertBbieScRequest {

    private final TopLevelAsbiepId topLevelAsbiepId;
    private final BbieScNode.BbieSc bbieSc;

    public UpsertBbieScRequest(TopLevelAsbiepId topLevelAsbiepId, BbieScNode.BbieSc bbieSc) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.bbieSc = bbieSc;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public BbieScNode.BbieSc getBbieSc() {
        return bbieSc;
    }
}
