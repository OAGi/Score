package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;

public interface ModuleSetReleaseWriteRepository {

    CreateModuleSetReleaseResponse createModuleSetRelease(
            CreateModuleSetReleaseRequest request) throws ScoreDataAccessException;

    UpdateModuleSetReleaseResponse updateModuleSetRelease(
            UpdateModuleSetReleaseRequest request) throws ScoreDataAccessException;

    DeleteModuleSetReleaseResponse deleteModuleSetRelease(
            DeleteModuleSetReleaseRequest request) throws ScoreDataAccessException;

    void createModuleManifest(CreateModuleManifestRequest request) throws ScoreDataAccessException;

    void deleteModuleManifest(DeleteModuleManifestRequest request) throws ScoreDataAccessException;
}
