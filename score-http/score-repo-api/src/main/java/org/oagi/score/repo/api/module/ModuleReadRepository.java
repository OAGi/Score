package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.GetModuleListRequest;
import org.oagi.score.repo.api.module.model.GetModuleListResponse;
import org.oagi.score.repo.api.module.model.GetModuleRequest;
import org.oagi.score.repo.api.module.model.GetModuleResponse;

public interface ModuleReadRepository {

    GetModuleResponse getModule(
            GetModuleRequest request) throws ScoreDataAccessException;

    GetModuleListResponse getModuleList(
            GetModuleListRequest request) throws ScoreDataAccessException;

}
