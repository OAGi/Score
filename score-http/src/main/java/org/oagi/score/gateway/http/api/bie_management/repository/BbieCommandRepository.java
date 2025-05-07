package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertBbieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieNode;

public interface BbieCommandRepository {

    BbieNode.Bbie upsertBbie(UpsertBbieRequest request);

}
