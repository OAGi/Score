package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleSetReleaseReadRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

public class JooqModuleSetReleaseReadRepository
        extends JooqScoreRepository
        implements ModuleSetReleaseReadRepository {

    public JooqModuleSetReleaseReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                MODULE_SET_RELEASE.MODULE_SET_ID,
                MODULE_SET_RELEASE.NAME,
                MODULE_SET_RELEASE.DESCRIPTION,
                MODULE_SET_RELEASE.RELEASE_ID,
                MODULE_SET.NAME,
                RELEASE.RELEASE_NUM,
                MODULE_SET_RELEASE.IS_DEFAULT,
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"),
                MODULE_SET_RELEASE.CREATION_TIMESTAMP,
                MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP)
                .from(MODULE_SET_RELEASE)
                .join(APP_USER.as("creator")).on(MODULE_SET_RELEASE.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(MODULE_SET_RELEASE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(MODULE_SET_RELEASE.RELEASE_ID))
                .join(MODULE_SET).on(MODULE_SET.MODULE_SET_ID.eq(MODULE_SET_RELEASE.MODULE_SET_ID));
    }

    private RecordMapper<Record, ModuleSetRelease> mapper() {
        return record -> {
            ModuleSetRelease moduleSetRelease = new ModuleSetRelease();
            moduleSetRelease.setModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID).toBigInteger());
            moduleSetRelease.setModuleSetId(record.get(MODULE_SET_RELEASE.MODULE_SET_ID).toBigInteger());
            moduleSetRelease.setModuleSetReleaseName(record.get(MODULE_SET_RELEASE.NAME));
            moduleSetRelease.setModuleSetReleaseDescription(record.get(MODULE_SET_RELEASE.DESCRIPTION));
            moduleSetRelease.setModuleSetName(record.get(MODULE_SET.NAME));
            moduleSetRelease.setReleaseId(record.get(MODULE_SET_RELEASE.RELEASE_ID).toBigInteger());
            moduleSetRelease.setReleaseNum(record.get(RELEASE.RELEASE_NUM));
            moduleSetRelease.setDefault(record.get(MODULE_SET_RELEASE.IS_DEFAULT) == 1);

            ScoreRole creatorRole = (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER;
            boolean isCreatorAdmin = (byte) 1 == record.get(APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"));
            moduleSetRelease.setCreatedBy(
                    (isCreatorAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    Arrays.asList(creatorRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    creatorRole));

            ScoreRole updaterRole = (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER;
            boolean isUpdaterAdmin = (byte) 1 == record.get(APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"));
            moduleSetRelease.setLastUpdatedBy(
                    (isUpdaterAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    Arrays.asList(updaterRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    updaterRole));

            moduleSetRelease.setCreationTimestamp(
                    Date.from(record.get(MODULE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            moduleSetRelease.setLastUpdateTimestamp(
                    Date.from(record.get(MODULE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return moduleSetRelease;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleSetReleaseResponse getModuleSetRelease(GetModuleSetReleaseRequest request) throws ScoreDataAccessException {
        ModuleSetRelease moduleSetRelease = null;

        BigInteger moduleSetReleaseId = request.getModuleSetReleaseId();
        if (!isNull(moduleSetReleaseId)) {
            moduleSetRelease = (ModuleSetRelease) select()
                    .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(moduleSetReleaseId)))
                    .fetchOne(mapper());
        }

        return new GetModuleSetReleaseResponse(moduleSetRelease);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetModuleSetReleaseListResponse getModuleSetReleaseList(GetModuleSetReleaseListRequest request) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request);

        SelectConditionStep conditionStep;

        conditionStep = select().where(conditions);

        SortField sortField = getSortField(request);
        int length = dslContext().fetchCount(conditionStep);
        SelectFinalStep finalStep;
        if (sortField == null) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortField)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortField);
            }
        }

        return new GetModuleSetReleaseListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }


    private Collection<Condition> getConditions(GetModuleSetReleaseListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (StringUtils.hasLength(request.getName())) {
            conditions.addAll(contains(request.getName(), MODULE_SET_RELEASE.NAME));
        }

        if (request.getReleaseId() != null) {
            conditions.add(MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        }

        if (request.getDefault() != null) {
            conditions.add(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) (request.getDefault() == true ? 1 : 0)));
        }

        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetModuleSetReleaseListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "name":
                field = MODULE_SET.NAME;
                break;

            case "releaseNum":
                field = RELEASE.RELEASE_NUM;
                break;

            case "lastupdatetimestamp":
                field = MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    public List<AssignableNode> getAssignableACCByModuleSetReleaseId(GetAssignableCCListRequest request) throws ScoreDataAccessException {
        return dslContext().select(
                ACC_MANIFEST.ACC_MANIFEST_ID, ACC.DEN, RELEASE.RELEASE_NUM,
                ACC.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ACC.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ACC_MANIFEST)
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_ACC_MANIFEST).on(
                        and(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID),
                            MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId()))))
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        ACC.OBJECT_CLASS_TERM.notEqual("Any Structured Content"),
                        MODULE_ACC_MANIFEST.MODULE_ACC_MANIFEST_ID.isNull()))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(ACC.DEN));
                    node.setType("ACC");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(ACC.STATE)));
                    node.setTimestamp(e.get(ACC.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignedACCByModuleSetReleaseId(GetAssignedCCListRequest request) throws ScoreDataAccessException {
        return dslContext().select(
                ACC_MANIFEST.ACC_MANIFEST_ID, ACC.DEN, RELEASE.RELEASE_NUM,
                ACC.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ACC.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ACC_MANIFEST)
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(MODULE_ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                        MODULE_ACC_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(ACC.DEN));
                    node.setType("ACC");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(ACC.STATE)));
                    node.setTimestamp(e.get(ACC.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignableASCCPByModuleSetReleaseId(GetAssignableCCListRequest request) throws ScoreDataAccessException {
        return dslContext().select(
                ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP.DEN, RELEASE.RELEASE_NUM,
                ASCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ASCCP.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_ASCCP_MANIFEST).on(
                        and(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID),
                                MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId()))))
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        ASCCP.PROPERTY_TERM.notEqual("Any Property"),
                        MODULE_ASCCP_MANIFEST.MODULE_ASCCP_MANIFEST_ID.isNull()))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(ASCCP.DEN));
                    node.setType("ASCCP");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(ASCCP.STATE)));
                    node.setTimestamp(e.get(ASCCP.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignedASCCPByModuleSetReleaseId(GetAssignedCCListRequest request) throws ScoreDataAccessException {
        return dslContext().select(
                ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP.DEN, RELEASE.RELEASE_NUM,
                ASCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ASCCP.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(MODULE_ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                        MODULE_ASCCP_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(ASCCP.DEN));
                    node.setType("ASCCP");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(ASCCP.STATE)));
                    node.setTimestamp(e.get(ASCCP.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignableBCCPByModuleSetReleaseId(GetAssignableCCListRequest request) throws ScoreDataAccessException {
        List<ULong> elementBccpManifestList = dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .join(BCC_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCC_MANIFEST.TO_BCCP_MANIFEST_ID))
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(and(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        BCC.ENTITY_TYPE.eq(1)))
                .fetchInto(ULong.class);
        return dslContext().select(
                BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP.DEN, RELEASE.RELEASE_NUM,
                BCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, BCCP.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_BCCP_MANIFEST).on(
                        and(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID),
                                MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId()))))
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        BCCP_MANIFEST.BCCP_MANIFEST_ID.in(elementBccpManifestList),
                        MODULE_BCCP_MANIFEST.MODULE_BCCP_MANIFEST_ID.isNull()))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(BCCP.DEN));
                    node.setType("BCCP");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(BCCP.STATE)));
                    node.setTimestamp(e.get(BCCP.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignedBCCPByModuleSetReleaseId(GetAssignedCCListRequest request) throws ScoreDataAccessException {
        return dslContext().select(
                BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP.DEN, RELEASE.RELEASE_NUM,
                BCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, BCCP.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(MODULE_BCCP_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                        MODULE_BCCP_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(BCCP.DEN));
                    node.setType("BCCP");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(BCCP.STATE)));
                    node.setTimestamp(e.get(BCCP.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignableDTByModuleSetReleaseId(GetAssignableCCListRequest request) throws ScoreDataAccessException {
        return dslContext().select(
                DT_MANIFEST.DT_MANIFEST_ID, DT.DEN, RELEASE.RELEASE_NUM,
                DT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, DT.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(MODULE_DT_MANIFEST).on(
                        and(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID),
                                MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId()))))
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        DT.BASED_DT_ID.isNotNull(),
                        MODULE_DT_MANIFEST.MODULE_DT_MANIFEST_ID.isNull()))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(DT.DEN));
                    node.setType("DT");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(DT.STATE)));
                    node.setTimestamp(e.get(DT.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignedDTByModuleSetReleaseId(GetAssignedCCListRequest request) throws ScoreDataAccessException {
        return dslContext().select(
                DT_MANIFEST.DT_MANIFEST_ID, DT.DEN, RELEASE.RELEASE_NUM,
                DT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, DT.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(MODULE_DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(MODULE_DT_MANIFEST.DT_MANIFEST_ID))
                .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                        MODULE_DT_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(DT.DEN));
                    node.setType("DT");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(DT.STATE)));
                    node.setTimestamp(e.get(DT.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignableCodeListByModuleSetReleaseId(GetAssignableCCListRequest request) throws ScoreDataAccessException {
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
                                MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId()))))
                .where(and(
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        MODULE_CODE_LIST_MANIFEST.MODULE_CODE_LIST_MANIFEST_ID.isNull()))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(CODE_LIST.NAME));
                    node.setType("CODE_LIST");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(CODE_LIST.STATE)));
                    node.setTimestamp(e.get(CODE_LIST.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignedCodeListByModuleSetReleaseId(GetAssignedCCListRequest request) throws ScoreDataAccessException {
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
                .where(and(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                        MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(CODE_LIST.NAME));
                    node.setType("CODE_LIST");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(CODE_LIST.STATE)));
                    node.setTimestamp(e.get(CODE_LIST.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignableAgencyIdListByModuleSetReleaseId(GetAssignableCCListRequest request) throws ScoreDataAccessException {
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
                                MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId()))))
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_AGENCY_ID_LIST_MANIFEST_ID.isNull()))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(AGENCY_ID_LIST.NAME));
                    node.setType("AGENCY_ID_LIST");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(AGENCY_ID_LIST.STATE)));
                    node.setTimestamp(e.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignedAgencyIdListByModuleSetReleaseId(GetAssignedCCListRequest request) throws ScoreDataAccessException {
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
                .where(and(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                        MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(AGENCY_ID_LIST.NAME));
                    node.setType("AGENCY_ID_LIST");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.valueOf(e.get(AGENCY_ID_LIST.STATE)));
                    node.setTimestamp(e.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignableXBTByModuleSetReleaseId(GetAssignableCCListRequest request) throws ScoreDataAccessException {
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
                                MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId()))))
                .where(and(XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())),
                        MODULE_XBT_MANIFEST.MODULE_XBT_MANIFEST_ID.isNull(),
                        XBT.BUILTIN_TYPE.notLike("xsd:%")))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(XBT.NAME));
                    node.setType("XBT");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.Published);
                    node.setTimestamp(e.get(XBT.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AssignableNode> getAssignedXBTByModuleSetReleaseId(GetAssignedCCListRequest request) throws ScoreDataAccessException {
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
                .where(and(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                        MODULE_XBT_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                .fetchStream().map(e -> {
                    AssignableNode node = new AssignableNode();
                    node.setManifestId(e.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger());
                    node.setDen(e.get(XBT.NAME));
                    node.setType("XBT");
                    node.setOwnerUserId(e.get(APP_USER.LOGIN_ID));
                    node.setRevision(e.get(LOG.REVISION_NUM).toBigInteger());
                    node.setState(CcState.Published);
                    node.setTimestamp(e.get(XBT.LAST_UPDATE_TIMESTAMP));
                    return node;
                }).collect(Collectors.toList());
    }
}
