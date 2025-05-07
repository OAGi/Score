package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieNode;

public class UpsertAsbieRequest {

    private final TopLevelAsbiepId topLevelAsbiepId;
    private final AsbieNode.Asbie asbie;

    public UpsertAsbieRequest(TopLevelAsbiepId topLevelAsbiepId, AsbieNode.Asbie asbie) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.asbie = asbie;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public AsbieNode.Asbie getAsbie() {
        return asbie;
    }
}
