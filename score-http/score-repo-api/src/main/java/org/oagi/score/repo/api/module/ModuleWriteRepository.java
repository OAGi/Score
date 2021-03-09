package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;

public interface ModuleWriteRepository {

    CreateModuleResponse createModule(
            CreateModuleRequest request) throws ScoreDataAccessException;

    UpdateModuleResponse updateModule(
            UpdateModuleRequest request) throws ScoreDataAccessException;

    DeleteModuleResponse deleteModule(
            DeleteModuleRequest request) throws ScoreDataAccessException;
}
