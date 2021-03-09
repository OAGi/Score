package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;

public interface ModuleSetReadRepository {

    GetModuleSetResponse getModuleSet(
            GetModuleSetRequest request) throws ScoreDataAccessException;

    GetModuleSetListResponse getModuleSetList(
            GetModuleSetListRequest request) throws ScoreDataAccessException;

}
