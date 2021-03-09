package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;

public interface ModuleSetWriteRepository {

    CreateModuleSetResponse createModuleSet(
            CreateModuleSetRequest request) throws ScoreDataAccessException;

    UpdateModuleSetResponse updateModuleSet(
            UpdateModuleSetRequest request) throws ScoreDataAccessException;

    DeleteModuleSetResponse deleteModuleSet(
            DeleteModuleSetRequest request) throws ScoreDataAccessException;
}
