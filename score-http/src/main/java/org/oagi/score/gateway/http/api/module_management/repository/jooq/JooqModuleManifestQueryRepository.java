package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.module_management.model.AssignNodeRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleManifestQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqModuleManifestQueryRepository extends JooqBaseRepository implements ModuleManifestQueryRepository {

    public JooqModuleManifestQueryRepository(DSLContext dslContext, ScoreUser requester,
                                             RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<AssignNodeRecord> getAssignableACCByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId) {
        return dslContext().select(
                        ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        ACC.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ACC.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ACC_MANIFEST)
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_ACC_MANIFEST).on(
                        and(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID),
                                MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.OBJECT_CLASS_TERM.notEqual("Any Structured Content"),
                        MODULE_ACC_MANIFEST.MODULE_ACC_MANIFEST_ID.isNull()))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new AccManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()),
                                CcType.ACC,
                                CcState.valueOf(e.get(ACC.STATE)),
                                e.get(ACC_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(ACC.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignedACCByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {
        return dslContext().select(
                        ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        ACC.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ACC.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ACC_MANIFEST)
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(MODULE_ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_ACC_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new AccManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()),
                                CcType.ACC,
                                CcState.valueOf(e.get(ACC.STATE)),
                                e.get(ACC_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(ACC.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignableASCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId) {
        return dslContext().select(
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        ASCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ASCCP.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_ASCCP_MANIFEST).on(
                        and(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID),
                                MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCCP.PROPERTY_TERM.notEqual("Any Property"),
                        MODULE_ASCCP_MANIFEST.MODULE_ASCCP_MANIFEST_ID.isNull()))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new AsccpManifestId(e.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger()),
                                CcType.ASCCP,
                                CcState.valueOf(e.get(ASCCP.STATE)),
                                e.get(ASCCP_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(ASCCP.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignedASCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {
        return dslContext().select(
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        ASCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ASCCP.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(MODULE_ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_ASCCP_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new AsccpManifestId(e.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger()),
                                CcType.ASCCP,
                                CcState.valueOf(e.get(ASCCP.STATE)),
                                e.get(ASCCP_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(ASCCP.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignableBCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId) {
        List<ULong> elementBccpManifestList = dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .join(BCC_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCC_MANIFEST.TO_BCCP_MANIFEST_ID))
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(and(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCC.ENTITY_TYPE.eq(1)))
                .fetchInto(ULong.class);
        return dslContext().select(
                        BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        BCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, BCCP.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_BCCP_MANIFEST).on(
                        and(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID),
                                MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCCP_MANIFEST.BCCP_MANIFEST_ID.in(elementBccpManifestList),
                        MODULE_BCCP_MANIFEST.MODULE_BCCP_MANIFEST_ID.isNull()))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new BccpManifestId(e.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger()),
                                CcType.BCCP,
                                CcState.valueOf(e.get(BCCP.STATE)),
                                e.get(BCCP_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(BCCP.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignedBCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {
        return dslContext().select(
                        BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        BCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, BCCP.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(MODULE_BCCP_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_BCCP_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new BccpManifestId(e.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger()),
                                CcType.BCCP,
                                CcState.valueOf(e.get(BCCP.STATE)),
                                e.get(BCCP_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(BCCP.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignableDTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId) {
        return dslContext().select(
                        DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        DT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, DT.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_DT_MANIFEST).on(
                        and(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID),
                                MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT.BASED_DT_ID.isNotNull(),
                        MODULE_DT_MANIFEST.MODULE_DT_MANIFEST_ID.isNull()))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new DtManifestId(e.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger()),
                                CcType.DT,
                                CcState.valueOf(e.get(DT.STATE)),
                                e.get(DT_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(DT.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignedDTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {
        return dslContext().select(
                        DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        DT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, DT.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(MODULE_DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(MODULE_DT_MANIFEST.DT_MANIFEST_ID))
                .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_DT_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new DtManifestId(e.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger()),
                                CcType.DT,
                                CcState.valueOf(e.get(DT.STATE)),
                                e.get(DT_MANIFEST.DEN),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(DT.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignableCodeListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId) {
        return dslContext().select(
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME, RELEASE.RELEASE_NUM,
                        CODE_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, CODE_LIST.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(CODE_LIST_MANIFEST)
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(APP_USER).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_CODE_LIST_MANIFEST).on(
                        and(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID),
                                MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .where(and(
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        MODULE_CODE_LIST_MANIFEST.MODULE_CODE_LIST_MANIFEST_ID.isNull()))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new CodeListManifestId(e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()),
                                CcType.CODE_LIST,
                                CcState.valueOf(e.get(CODE_LIST.STATE)),
                                e.get(CODE_LIST.NAME),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(CODE_LIST.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignedCodeListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {
        return dslContext().select(
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME, RELEASE.RELEASE_NUM,
                        CODE_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, CODE_LIST.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(CODE_LIST_MANIFEST)
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(MODULE_CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .join(APP_USER).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new CodeListManifestId(e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()),
                                CcType.CODE_LIST,
                                CcState.valueOf(e.get(CODE_LIST.STATE)),
                                e.get(CODE_LIST.NAME),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(CODE_LIST.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignableAgencyIdListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId) {
        return dslContext().select(
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME, RELEASE.RELEASE_NUM,
                        AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, AGENCY_ID_LIST.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(APP_USER).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_AGENCY_ID_LIST_MANIFEST).on(
                        and(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID),
                                MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_AGENCY_ID_LIST_MANIFEST_ID.isNull()))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new AgencyIdListManifestId(e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                                CcType.AGENCY_ID_LIST,
                                CcState.valueOf(e.get(AGENCY_ID_LIST.STATE)),
                                e.get(AGENCY_ID_LIST.NAME),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignedAgencyIdListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {
        return dslContext().select(
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME, RELEASE.RELEASE_NUM,
                        AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, AGENCY_ID_LIST.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(MODULE_AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .join(APP_USER).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new AgencyIdListManifestId(e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                                CcType.AGENCY_ID_LIST,
                                CcState.valueOf(e.get(AGENCY_ID_LIST.STATE)),
                                e.get(AGENCY_ID_LIST.NAME),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignableXBTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId) {
        return dslContext().select(
                        XBT_MANIFEST.XBT_MANIFEST_ID, XBT.NAME, RELEASE.RELEASE_NUM,
                        XBT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, XBT.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(XBT_MANIFEST)
                .join(RELEASE).on(XBT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                .join(APP_USER).on(XBT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(XBT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_XBT_MANIFEST).on(
                        and(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID),
                                MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId))))
                .where(and(XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        MODULE_XBT_MANIFEST.MODULE_XBT_MANIFEST_ID.isNull(),
                        XBT.BUILTIN_TYPE.notLike("xsd:%")))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new XbtManifestId(e.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger()),
                                CcType.XBT,
                                CcState.Published,
                                e.get(XBT.NAME),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(XBT.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

    @Override
    public List<AssignNodeRecord> getAssignedXBTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {
        return dslContext().select(
                        XBT_MANIFEST.XBT_MANIFEST_ID, XBT.NAME, RELEASE.RELEASE_NUM,
                        XBT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, XBT.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(XBT_MANIFEST)
                .join(RELEASE).on(XBT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                .join(MODULE_XBT_MANIFEST).on(XBT_MANIFEST.XBT_MANIFEST_ID.eq(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID))
                .join(APP_USER).on(XBT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(XBT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)),
                        MODULE_XBT_MANIFEST.MODULE_ID.eq(valueOf(moduleId))))
                .fetchStream().map(e ->
                        new AssignNodeRecord(
                                new XbtManifestId(e.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger()),
                                CcType.XBT,
                                CcState.Published,
                                e.get(XBT.NAME),
                                e.get(APP_USER.LOGIN_ID),
                                toDate(e.get(XBT.LAST_UPDATE_TIMESTAMP)),
                                e.get(LOG.REVISION_NUM).toBigInteger())
                ).collect(Collectors.toList());
    }

}
