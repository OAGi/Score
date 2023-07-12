package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.gateway.http.api.oas_management.data.BieForOasDocUpdateRequest;
import org.oagi.score.gateway.http.api.oas_management.data.BieForOasDocUpdateResponse;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.springframework.security.core.AuthenticatedPrincipal;

public interface BieForOasDocWriteRepository {

    AddBieForOasDocResponse assignBieForOasDoc(AuthenticatedPrincipal user,
                                               AddBieForOasDocRequest request) throws ScoreDataAccessException;

    //    todo
    UpdateBieForOasDocResponse updateBieForOasDoc(
            UpdateBieForOasDocRequest request) throws ScoreDataAccessException;

    DeleteBieForOasDocResponse deleteBieForOasDoc(
            DeleteBieForOasDocRequest request) throws ScoreDataAccessException;
}
