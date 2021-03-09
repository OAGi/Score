package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;

public interface ModuleSetReleaseReadRepository {

    GetModuleSetReleaseResponse getModuleSetRelease(
            GetModuleSetReleaseRequest request) throws ScoreDataAccessException;

    GetModuleSetReleaseListResponse getModuleSetReleaseList(
            GetModuleSetReleaseListRequest request) throws ScoreDataAccessException;

}
