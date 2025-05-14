package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleManifestCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqModuleManifestCommandRepository extends JooqBaseRepository implements ModuleManifestCommandRepository {

    public JooqModuleManifestCommandRepository(DSLContext dslContext, ScoreUser requester,
                                               RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public void deleteModuleManifests(Collection<ModuleSetReleaseId> moduleSetReleaseIdList) {
        dslContext().deleteFrom(MODULE_ACC_MANIFEST)
                .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
        dslContext().deleteFrom(MODULE_ASCCP_MANIFEST)
                .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
        dslContext().deleteFrom(MODULE_BCCP_MANIFEST)
                .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
        dslContext().deleteFrom(MODULE_DT_MANIFEST)
                .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
        dslContext().deleteFrom(MODULE_CODE_LIST_MANIFEST)
                .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
        dslContext().deleteFrom(MODULE_AGENCY_ID_LIST_MANIFEST)
                .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
        dslContext().deleteFrom(MODULE_XBT_MANIFEST)
                .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
        dslContext().deleteFrom(MODULE_BLOB_CONTENT_MANIFEST)
                .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.in(valueOf(moduleSetReleaseIdList)))
                .execute();
    }

    @Override
    public void copyAccModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                      ModuleSetReleaseId baseModuleSetReleaseId,
                                      ReleaseId releaseId,
                                      Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().batch(
                dslContext().select(ACC_MANIFEST.as("acc_manifest_target").ACC_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_ACC_MANIFEST)
                        .join(ACC_MANIFEST).on(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(ACC.as("acc_target")).on(ACC.GUID.eq(ACC.as("acc_target").GUID))
                        .join(ACC_MANIFEST.as("acc_manifest_target")).on(and(
                                ACC.as("acc_target").ACC_ID.eq(ACC_MANIFEST.as("acc_manifest_target").ACC_ID),
                                ACC_MANIFEST.as("acc_manifest_target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId)))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_ACC_MANIFEST)
                                .set(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                                .set(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID, record.get(ACC_MANIFEST.as("acc_manifest_target").ACC_MANIFEST_ID))
                                .set(MODULE_ACC_MANIFEST.MODULE_ID, valueOf(pathToModuleId.apply(record.get(MODULE.PATH))))
                                .set(MODULE_ACC_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                                .set(MODULE_ACC_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                                .set(MODULE_ACC_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createAccModuleManifest(AccManifestId accManifestId,
                                        ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_ACC_MANIFEST)
                .set(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                .set(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID, valueOf(accManifestId))
                .set(MODULE_ACC_MANIFEST.MODULE_ID, valueOf(moduleId))
                .set(MODULE_ACC_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                .set(MODULE_ACC_MANIFEST.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_ACC_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    @Override
    public void deleteAccModuleManifest(AccManifestId accManifestId,
                                        ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        dslContext().deleteFrom(MODULE_ACC_MANIFEST)
                .where(and(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)),
                        MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_ACC_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .execute();
    }

    @Override
    public void copyAsccpModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                        ModuleSetReleaseId baseModuleSetReleaseId,
                                        ReleaseId releaseId,
                                        Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().batch(
                dslContext().select(ASCCP_MANIFEST.as("asccp_manifest_target").ASCCP_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_ASCCP_MANIFEST)
                        .join(ASCCP_MANIFEST).on(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(ASCCP.as("asccp_target")).on(ASCCP.GUID.eq(ASCCP.as("asccp_target").GUID))
                        .join(ASCCP_MANIFEST.as("asccp_manifest_target")).on(and(
                                ASCCP.as("asccp_target").ASCCP_ID.eq(ASCCP_MANIFEST.as("asccp_manifest_target").ASCCP_ID),
                                ASCCP_MANIFEST.as("asccp_manifest_target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(MODULE).on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId)))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_ASCCP_MANIFEST)
                                .set(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                                .set(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID, record.get(ASCCP_MANIFEST.as("asccp_manifest_target").ASCCP_MANIFEST_ID))
                                .set(MODULE_ASCCP_MANIFEST.MODULE_ID, valueOf(pathToModuleId.apply(record.get(MODULE.PATH))))
                                .set(MODULE_ASCCP_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                                .set(MODULE_ASCCP_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_ASCCP_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                                .set(MODULE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createAsccpModuleManifest(AsccpManifestId asccpManifestId,
                                          ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_ASCCP_MANIFEST)
                .set(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                .set(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID, valueOf(asccpManifestId))
                .set(MODULE_ASCCP_MANIFEST.MODULE_ID, valueOf(moduleId))
                .set(MODULE_ASCCP_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                .set(MODULE_ASCCP_MANIFEST.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_ASCCP_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    @Override
    public void deleteAsccpModuleManifest(AsccpManifestId asccpManifestId,
                                          ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        dslContext().deleteFrom(MODULE_ASCCP_MANIFEST)
                .where(and(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)),
                        MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_ASCCP_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .execute();
    }

    @Override
    public void copyBccpModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                       ModuleSetReleaseId baseModuleSetReleaseId,
                                       ReleaseId releaseId,
                                       Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().batch(
                dslContext().select(BCCP_MANIFEST.as("bccp_manifest_target").BCCP_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_BCCP_MANIFEST)
                        .join(BCCP_MANIFEST).on(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(BCCP.as("bccp_target")).on(BCCP.GUID.eq(BCCP.as("bccp_target").GUID))
                        .join(BCCP_MANIFEST.as("bccp_manifest_target")).on(and(
                                BCCP.as("bccp_target").BCCP_ID.eq(BCCP_MANIFEST.as("bccp_manifest_target").BCCP_ID),
                                BCCP_MANIFEST.as("bccp_manifest_target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(MODULE).on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId)))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_BCCP_MANIFEST)
                                .set(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                                .set(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID, record.get(BCCP_MANIFEST.as("bccp_manifest_target").BCCP_MANIFEST_ID))
                                .set(MODULE_BCCP_MANIFEST.MODULE_ID, valueOf(pathToModuleId.apply(record.get(MODULE.PATH))))
                                .set(MODULE_BCCP_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                                .set(MODULE_BCCP_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_BCCP_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                                .set(MODULE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createBccpModuleManifest(BccpManifestId bccpManifestId,
                                         ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_BCCP_MANIFEST)
                .set(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                .set(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID, valueOf(bccpManifestId))
                .set(MODULE_BCCP_MANIFEST.MODULE_ID, valueOf(moduleId))
                .set(MODULE_BCCP_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                .set(MODULE_BCCP_MANIFEST.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_BCCP_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    @Override
    public void deleteBccpModuleManifest(BccpManifestId bccpManifestId,
                                         ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        dslContext().deleteFrom(MODULE_BCCP_MANIFEST)
                .where(and(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)),
                        MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_BCCP_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .execute();
    }

    @Override
    public void copyDtModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                     ModuleSetReleaseId baseModuleSetReleaseId,
                                     ReleaseId releaseId,
                                     Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().batch(
                dslContext().select(DT_MANIFEST.as("dt_manifest_target").DT_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_DT_MANIFEST)
                        .join(DT_MANIFEST).on(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .join(DT.as("dt_target")).on(DT.GUID.eq(DT.as("dt_target").GUID))
                        .join(DT_MANIFEST.as("dt_manifest_target")).on(and(
                                DT.as("dt_target").DT_ID.eq(DT_MANIFEST.as("dt_manifest_target").DT_ID),
                                DT_MANIFEST.as("dt_manifest_target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(MODULE).on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId)))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_DT_MANIFEST)
                                .set(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                                .set(MODULE_DT_MANIFEST.DT_MANIFEST_ID, record.get(DT_MANIFEST.as("dt_manifest_target").DT_MANIFEST_ID))
                                .set(MODULE_DT_MANIFEST.MODULE_ID, valueOf(pathToModuleId.apply(record.get(MODULE.PATH))))
                                .set(MODULE_DT_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                                .set(MODULE_DT_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_DT_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                                .set(MODULE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createDtModuleManifest(DtManifestId dtManifestId,
                                       ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_DT_MANIFEST)
                .set(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                .set(MODULE_DT_MANIFEST.DT_MANIFEST_ID, valueOf(dtManifestId))
                .set(MODULE_DT_MANIFEST.MODULE_ID, valueOf(moduleId))
                .set(MODULE_DT_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                .set(MODULE_DT_MANIFEST.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_DT_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    @Override
    public void deleteDtModuleManifest(DtManifestId dtManifestId,
                                       ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        dslContext().deleteFrom(MODULE_DT_MANIFEST)
                .where(and(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)),
                        MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_DT_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .execute();
    }

    @Override
    public void copyCodeListModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                           ModuleSetReleaseId baseModuleSetReleaseId,
                                           ReleaseId releaseId,
                                           Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().batch(
                dslContext().select(CODE_LIST_MANIFEST.as("code_list_manifest_target").CODE_LIST_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_CODE_LIST_MANIFEST)
                        .join(CODE_LIST_MANIFEST).on(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                        .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .join(CODE_LIST.as("code_list_target")).on(CODE_LIST.GUID.eq(CODE_LIST.as("code_list_target").GUID))
                        .join(CODE_LIST_MANIFEST.as("code_list_manifest_target")).on(and(
                                CODE_LIST.as("code_list_target").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("code_list_manifest_target").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("code_list_manifest_target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId)))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_CODE_LIST_MANIFEST)
                                .set(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                                .set(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, record.get(CODE_LIST_MANIFEST.as("code_list_manifest_target").CODE_LIST_MANIFEST_ID))
                                .set(MODULE_CODE_LIST_MANIFEST.MODULE_ID, valueOf(pathToModuleId.apply(record.get(MODULE.PATH))))
                                .set(MODULE_CODE_LIST_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                                .set(MODULE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                                .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createCodeListModuleManifest(CodeListManifestId codeListManifestId,
                                             ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_CODE_LIST_MANIFEST)
                .set(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                .set(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, valueOf(codeListManifestId))
                .set(MODULE_CODE_LIST_MANIFEST.MODULE_ID, valueOf(moduleId))
                .set(MODULE_CODE_LIST_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                .set(MODULE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    @Override
    public void deleteCodeListModuleManifest(CodeListManifestId codeListManifestId,
                                             ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        dslContext().deleteFrom(MODULE_CODE_LIST_MANIFEST)
                .where(and(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)),
                        MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .execute();
    }

    @Override
    public void copyAgencyIdListModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                               ModuleSetReleaseId baseModuleSetReleaseId,
                                               ReleaseId releaseId,
                                               Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().batch(
                dslContext().select(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").AGENCY_ID_LIST_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_AGENCY_ID_LIST_MANIFEST)
                        .join(AGENCY_ID_LIST_MANIFEST).on(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .join(AGENCY_ID_LIST.as("agency_id_list_target")).on(AGENCY_ID_LIST.GUID.eq(AGENCY_ID_LIST.as("agency_id_list_target").GUID))
                        .join(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target")).on(and(
                                AGENCY_ID_LIST.as("agency_id_list_target").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId)))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_AGENCY_ID_LIST_MANIFEST)
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, record.get(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").AGENCY_ID_LIST_MANIFEST_ID))
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID, valueOf(pathToModuleId.apply(record.get(MODULE.PATH))))
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createAgencyIdListModuleManifest(AgencyIdListManifestId agencyIdListManifestId,
                                                 ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_AGENCY_ID_LIST_MANIFEST)
                .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                .set(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, valueOf(agencyIdListManifestId))
                .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID, valueOf(moduleId))
                .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    @Override
    public void deleteAgencyIdListModuleManifest(AgencyIdListManifestId agencyIdListManifestId,
                                                 ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        dslContext().deleteFrom(MODULE_AGENCY_ID_LIST_MANIFEST)
                .where(and(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)),
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .execute();

    }

    @Override
    public void copyXbtModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                      ModuleSetReleaseId baseModuleSetReleaseId,
                                      ReleaseId releaseId,
                                      Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().batch(
                dslContext().select(XBT_MANIFEST.as("xbt_manifest_target").XBT_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_XBT_MANIFEST)
                        .join(XBT_MANIFEST).on(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                        .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                        .join(XBT.as("xbt_target")).on(XBT.GUID.eq(XBT.as("xbt_target").GUID))
                        .join(XBT_MANIFEST.as("xbt_manifest_target")).on(and(
                                XBT.as("xbt_target").XBT_ID.eq(XBT_MANIFEST.as("xbt_manifest_target").XBT_ID),
                                XBT_MANIFEST.as("xbt_manifest_target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(MODULE).on(MODULE_XBT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId)))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_XBT_MANIFEST)
                                .set(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                                .set(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID, record.get(XBT_MANIFEST.as("xbt_manifest_target").XBT_MANIFEST_ID))
                                .set(MODULE_XBT_MANIFEST.MODULE_ID, valueOf(pathToModuleId.apply(record.get(MODULE.PATH))))
                                .set(MODULE_XBT_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                                .set(MODULE_XBT_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_XBT_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                                .set(MODULE_XBT_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createXbtModuleManifest(XbtManifestId xbtManifestId,
                                        ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_XBT_MANIFEST)
                .set(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID, valueOf(moduleSetReleaseId))
                .set(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID, valueOf(xbtManifestId))
                .set(MODULE_XBT_MANIFEST.MODULE_ID, valueOf(moduleId))
                .set(MODULE_XBT_MANIFEST.CREATED_BY, valueOf(requester().userId()))
                .set(MODULE_XBT_MANIFEST.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_XBT_MANIFEST.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(MODULE_XBT_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    @Override
    public void deleteXbtModuleManifest(XbtManifestId xbtManifestId,
                                        ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId) {
        dslContext().deleteFrom(MODULE_XBT_MANIFEST)
                .where(and(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID.eq(valueOf(xbtManifestId)),
                        MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_XBT_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .execute();
    }

    @Override
    public void copyBlobContentModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                              ModuleSetReleaseId baseModuleSetReleaseId,
                                              ReleaseId releaseId,
                                              Function<String, ModuleId> pathToModuleId) {
        LocalDateTime timestamp = LocalDateTime.now();
        dslContext().insertInto(MODULE_BLOB_CONTENT_MANIFEST,
                        MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID,
                        MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID,
                        MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID,
                        MODULE_BLOB_CONTENT_MANIFEST.CREATED_BY,
                        MODULE_BLOB_CONTENT_MANIFEST.CREATION_TIMESTAMP,
                        MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATED_BY,
                        MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                inline(valueOf(moduleSetReleaseId)),
                                MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID,
                                MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID,
                                inline(valueOf(requester().userId())),
                                inline(timestamp),
                                inline(valueOf(requester().userId())),
                                inline(timestamp))
                        .from(MODULE_BLOB_CONTENT_MANIFEST)
                        .join(BLOB_CONTENT_MANIFEST).on(MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID.eq(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID))
                        .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(baseModuleSetReleaseId))))
                .execute();
    }
}
