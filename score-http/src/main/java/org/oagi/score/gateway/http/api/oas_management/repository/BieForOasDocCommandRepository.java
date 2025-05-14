package org.oagi.score.gateway.http.api.oas_management.repository;

import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

public interface BieForOasDocCommandRepository {

    AddBieForOasDocResponse assignBieForOasDoc(
                                               AddBieForOasDocRequest request) throws ScoreDataAccessException;

    //    todo
    UpdateBieForOasDocResponse updateBieForOasDoc(
                                                  UpdateBieForOasDocRequest request) throws ScoreDataAccessException;

    DeleteBieForOasDocResponse deleteBieForOasDoc(
            DeleteBieForOasDocRequest request) throws ScoreDataAccessException;
}
