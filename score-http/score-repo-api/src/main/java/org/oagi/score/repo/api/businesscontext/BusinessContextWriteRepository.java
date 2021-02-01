package org.oagi.score.repo.api.businesscontext;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.model.*;

public interface BusinessContextWriteRepository {

    CreateBusinessContextResponse createBusinessContext(
            CreateBusinessContextRequest request) throws ScoreDataAccessException;

    UpdateBusinessContextResponse updateBusinessContext(
            UpdateBusinessContextRequest request) throws ScoreDataAccessException;

    DeleteBusinessContextResponse deleteBusinessContext(
            DeleteBusinessContextRequest request) throws ScoreDataAccessException;
    
}
