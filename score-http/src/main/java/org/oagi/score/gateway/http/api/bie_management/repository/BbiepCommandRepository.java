package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertBbiepRequest;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepNode;

public interface BbiepCommandRepository {

    BbiepNode.Bbiep upsertBbiep(UpsertBbiepRequest request);

}
