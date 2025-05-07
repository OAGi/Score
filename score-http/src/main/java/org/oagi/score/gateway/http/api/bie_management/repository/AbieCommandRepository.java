package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertAbieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieNode;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertAbieArguments;

public interface AbieCommandRepository {

    AbieId insertAbie(InsertAbieArguments arguments);

    AbieNode.Abie upsertAbie(UpsertAbieRequest request);

}
