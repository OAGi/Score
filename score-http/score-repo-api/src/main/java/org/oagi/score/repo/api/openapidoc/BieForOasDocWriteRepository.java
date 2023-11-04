package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.*;

public interface BieForOasDocWriteRepository {

    AddBieForOasDocResponse assignBieForOasDoc(
                                               AddBieForOasDocRequest request) throws ScoreDataAccessException;

    //    todo
    UpdateBieForOasDocResponse updateBieForOasDoc(
                                                  UpdateBieForOasDocRequest request) throws ScoreDataAccessException;

    DeleteBieForOasDocResponse deleteBieForOasDoc(
            DeleteBieForOasDocRequest request) throws ScoreDataAccessException;
}
