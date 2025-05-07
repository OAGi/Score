package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.UpdateSetStep;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleManifestCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ModuleSetReleaseRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.MODULE_SET_RELEASE;

public class JooqModuleSetReleaseCommandRepository extends JooqBaseRepository implements ModuleSetReleaseCommandRepository {

    private final ModuleSetReleaseQueryRepository moduleSetReleaseQueryRepository;

    private final ModuleQueryRepository moduleQueryRepository;

    private final ModuleManifestCommandRepository moduleManifestCommandRepository;

    public JooqModuleSetReleaseCommandRepository(DSLContext dslContext, ScoreUser requester,
                                                 RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.moduleSetReleaseQueryRepository = repositoryFactory.moduleSetReleaseQueryRepository(requester);
        this.moduleQueryRepository = repositoryFactory.moduleQueryRepository(requester);
        this.moduleManifestCommandRepository = repositoryFactory.moduleManifestCommandRepository(requester);
    }

    @Override
    public ModuleSetReleaseId create(ModuleSetId moduleSetId, ReleaseId releaseId, String moduleSetReleaseName, String description, boolean isDefault) {

        LocalDateTime timestamp = LocalDateTime.now();

        InsertSetMoreStep<ModuleSetReleaseRecord> insertSetMoreStep =
                dslContext().insertInto(MODULE_SET_RELEASE)
                        .set(MODULE_SET_RELEASE.RELEASE_ID, valueOf(releaseId))
                        .set(MODULE_SET_RELEASE.MODULE_SET_ID, valueOf(moduleSetId))
                        .set(MODULE_SET_RELEASE.NAME, moduleSetReleaseName);

        String moduleSetReleaseDescription = description;
        if (StringUtils.hasLength(moduleSetReleaseDescription)) {
            insertSetMoreStep = insertSetMoreStep.set(MODULE_SET_RELEASE.DESCRIPTION, moduleSetReleaseDescription);
        }

        return new ModuleSetReleaseId(
                insertSetMoreStep
                        .set(MODULE_SET_RELEASE.IS_DEFAULT, isDefault ? (byte) 1 : 0)
                        .set(MODULE_SET_RELEASE.CREATED_BY, valueOf(requester().userId()))
                        .set(MODULE_SET_RELEASE.LAST_UPDATED_BY, valueOf(requester().userId()))
                        .set(MODULE_SET_RELEASE.CREATION_TIMESTAMP, timestamp)
                        .set(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                        .returning(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID)
                        .fetchOne().getModuleSetReleaseId().toBigInteger()
        );
    }

    @Override
    public void copyModuleCcManifest(ModuleSetReleaseId moduleSetReleaseId,
                                     ModuleSetReleaseId baseModuleSetReleaseId) {

        ModuleSetReleaseDetailsRecord moduleSetRelease =
                moduleSetReleaseQueryRepository.getModuleSetReleaseDetails(moduleSetReleaseId);

        ReleaseId releaseId = moduleSetRelease.release().releaseId();

        Map<String, ModuleId> targetModulePathIdMap =
                moduleQueryRepository.getModuleSummaryList(moduleSetRelease.moduleSet().moduleSetId()).stream()
                        .collect(Collectors.toMap(e -> e.path(), e -> e.moduleId()));

        // copy MODULE_ACC_MANIFEST
        moduleManifestCommandRepository.copyAccModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));

        // copy MODULE_ASCCP_MANIFEST
        moduleManifestCommandRepository.copyAsccpModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));

        // copy MODULE_BCCP_MANIFEST
        moduleManifestCommandRepository.copyBccpModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));

        // copy MODULE_CODE_LIST_MANIFEST
        moduleManifestCommandRepository.copyCodeListModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));

        // copy MODULE_AGENCY_ID_LIST_MANIFEST
        moduleManifestCommandRepository.copyAgencyIdListModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));

        // copy MODULE_BLOB_CONTENT_MANIFEST
        moduleManifestCommandRepository.copyBlobContentModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));

        // copy MODULE_XBT_MANIFEST
        moduleManifestCommandRepository.copyXbtModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));

        // copy MODULE_DT_MANIFEST
        moduleManifestCommandRepository.copyDtModuleManifest(
                moduleSetRelease.moduleSetReleaseId(),
                baseModuleSetReleaseId,
                releaseId,
                (path) -> targetModulePathIdMap.get(path));
    }

    @Override
    public void disableDefaultFlag(ReleaseId releaseId) {
        dslContext().update(MODULE_SET_RELEASE)
                .set(MODULE_SET_RELEASE.IS_DEFAULT, (byte) 0)
                .where(MODULE_SET_RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
    }

    @Override
    public boolean update(ModuleSetReleaseId moduleSetReleaseId,
                          String name, String description, boolean isDefault) {

        LocalDateTime timestamp = LocalDateTime.now();

        UpdateSetStep updateSetStep = dslContext().update(MODULE_SET_RELEASE)
//                .set(MODULE_SET_RELEASE.RELEASE_ID, valueOf(releaseId))
//                .set(MODULE_SET_RELEASE.MODULE_SET_ID, valueOf(moduleSetId))
                .set(MODULE_SET_RELEASE.NAME, name);

        if (StringUtils.hasLength(description)) {
            updateSetStep = updateSetStep.set(MODULE_SET_RELEASE.DESCRIPTION, description);
        } else {
            updateSetStep = updateSetStep.setNull(MODULE_SET_RELEASE.DESCRIPTION);
        }

        int numOfUpdatedRecords = updateSetStep.set(MODULE_SET_RELEASE.IS_DEFAULT, isDefault ? (byte) 1 : 0)
                .set(MODULE_SET_RELEASE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(ModuleSetReleaseId moduleSetReleaseId) {

        moduleManifestCommandRepository.deleteModuleManifests(Arrays.asList(moduleSetReleaseId));

        int numOfDeletedRecords = dslContext().deleteFrom(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .execute();
        return numOfDeletedRecords == 1;
    }
}
