package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieNode;

public class UpsertBbieRequest {

    private final TopLevelAsbiepId topLevelAsbiepId;
    private final BbieNode.Bbie bbie;

    public UpsertBbieRequest(TopLevelAsbiepId topLevelAsbiepId, BbieNode.Bbie bbie) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.bbie = bbie;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public BbieNode.Bbie getBbie() {
        return bbie;
    }
}
