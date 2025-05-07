package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertAsbiepRequest;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertAsbiepArguments;

public interface AsbiepCommandRepository {

    AsbiepId insertAsbiep(InsertAsbiepArguments arguments);

    AsbiepNode.Asbiep upsertAsbiep(UpsertAsbiepRequest request);

}
