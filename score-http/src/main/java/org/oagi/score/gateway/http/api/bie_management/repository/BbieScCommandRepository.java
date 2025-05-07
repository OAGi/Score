package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertBbieScRequest;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScNode;

public interface BbieScCommandRepository {

    BbieScNode.BbieSc upsertBbieSc(UpsertBbieScRequest request);

}
