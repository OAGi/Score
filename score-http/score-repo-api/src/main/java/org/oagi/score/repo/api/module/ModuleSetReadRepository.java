package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.module.model.Module;

import java.math.BigInteger;
import java.util.List;

public interface ModuleSetReadRepository {

    GetModuleSetResponse getModuleSet(
            GetModuleSetRequest request) throws ScoreDataAccessException;

    GetModuleSetListResponse getModuleSetList(
            GetModuleSetListRequest request) throws ScoreDataAccessException;

    List<Module> getAllModules(BigInteger moduleSetId) throws  ScoreDataAccessException;

    List<Module> getToplevelModules(BigInteger moduleSetId) throws  ScoreDataAccessException;
}
