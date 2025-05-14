package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieNode;

public class UpsertAbieRequest {

    private final TopLevelAsbiepId topLevelAsbiepId;
    private final AbieNode.Abie abie;

    public UpsertAbieRequest(TopLevelAsbiepId topLevelAsbiepId, AbieNode.Abie abie) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.abie = abie;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public AbieNode.Abie getAbie() {
        return abie;
    }
}
