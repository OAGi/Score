package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBieRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBieResponse;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

import java.util.Collection;

public interface BieCommandRepository {

    void deleteByTopLevelAsbiepIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

    CreateBieResponse createBie(CreateBieRequest request) throws ScoreDataAccessException;

}
