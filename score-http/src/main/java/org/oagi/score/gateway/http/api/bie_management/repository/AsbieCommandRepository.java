package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertAsbieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieNode;

public interface AsbieCommandRepository {

    AsbieNode.Asbie upsertAsbie(UpsertAsbieRequest request);

}
