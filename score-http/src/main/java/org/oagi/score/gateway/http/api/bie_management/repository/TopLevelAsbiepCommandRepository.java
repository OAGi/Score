package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpdateTopLevelAsbiepRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertBizCtxAssignmentArguments;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertTopLevelAsbiepArguments;

public interface TopLevelAsbiepCommandRepository {

    TopLevelAsbiepId insertTopLevelAsbiep(InsertTopLevelAsbiepArguments arguments);

    void insertBizCtxAssignments(InsertBizCtxAssignmentArguments arguments);

    void updateTopLevelAsbiep(UpdateTopLevelAsbiepRequest request);

    void updateAsbiepId(AsbiepId asbiepId, TopLevelAsbiepId topLevelAsbiepId);

}
