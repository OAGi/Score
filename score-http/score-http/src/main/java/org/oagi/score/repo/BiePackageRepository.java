package org.oagi.score.repo;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.data.*;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.bie.model.BiePackageState;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BiePackageRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BiePackageTopLevelAsbiepRecord;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

@Repository
public class BiePackageRepository {

    @Autowired
    private DSLContext dslContext;

    public PaginationResponse<BiePackage> getBiePackageList(BiePackageListRequest request) {
        SelectConditionStep<Record> conditionStep = getSelectOnConditionStep(request).where(makeConditions(request));

        int pageCount = dslContext.fetchCount(conditionStep);

        List<SortField<?>> sortFields = getSortFields(request);
        SelectFinalStep<Record> finalStep;
        if (sortFields == null || sortFields.isEmpty()) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortFields).limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortFields);
            }
        }

        return new PaginationResponse<>(pageCount, finalStep.fetch(record -> mapperForBiePackage(record)));
    }

    private SelectOnConditionStep<Record> getSelectOnConditionStep(BiePackageListRequest request) {
        return dslContext.select(BIE_PACKAGE.BIE_PACKAGE_ID, BIE_PACKAGE.VERSION_ID, BIE_PACKAGE.VERSION_NAME, BIE_PACKAGE.DESCRIPTION, BIE_PACKAGE.RELEASE_ID, RELEASE.RELEASE_NUM, BIE_PACKAGE.STATE, APP_USER.as("creator").APP_USER_ID.as("creator_user_id"), APP_USER.as("creator").LOGIN_ID.as("creator_login_id"), APP_USER.as("creator").NAME.as("creator_name"), APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"), APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"), APP_USER.as("owner").APP_USER_ID.as("owner_user_id"), APP_USER.as("owner").LOGIN_ID.as("owner_login_id"), APP_USER.as("owner").NAME.as("owner_name"), APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"), APP_USER.as("owner").IS_ADMIN.as("owner_is_admin"), APP_USER.as("updater").APP_USER_ID.as("updater_user_id"), APP_USER.as("updater").LOGIN_ID.as("updater_login_id"), APP_USER.as("updater").NAME.as("updater_name"), APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"), APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"), BIE_PACKAGE.CREATION_TIMESTAMP, BIE_PACKAGE.LAST_UPDATE_TIMESTAMP, BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID, BIE_PACKAGE.SOURCE_ACTION, BIE_PACKAGE.SOURCE_TIMESTAMP, BIE_PACKAGE.as("source").VERSION_ID, BIE_PACKAGE.as("source").VERSION_NAME).from(BIE_PACKAGE).join(APP_USER.as("owner")).on(BIE_PACKAGE.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID)).join(APP_USER.as("creator")).on(BIE_PACKAGE.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID)).join(APP_USER.as("updater")).on(BIE_PACKAGE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID)).leftJoin(RELEASE).on(BIE_PACKAGE.RELEASE_ID.eq(RELEASE.RELEASE_ID)).leftJoin(BIE_PACKAGE.as("source")).on(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID.eq(BIE_PACKAGE.as("source").BIE_PACKAGE_ID));
    }

    private List<Condition> makeConditions(BiePackageListRequest request) {
        List<Condition> conditions = new ArrayList<>();

        if (hasLength(request.getVersionId())) {
            conditions.addAll(contains(request.getVersionId(), BIE_PACKAGE.VERSION_ID));
        }
        if (hasLength(request.getVersionName())) {
            conditions.addAll(contains(request.getVersionName(), BIE_PACKAGE.VERSION_NAME));
        }
        if (hasLength(request.getDescription())) {
            conditions.addAll(contains(request.getDescription(), BIE_PACKAGE.DESCRIPTION));
        }
        if (!request.getStates().isEmpty()) {
            conditions.add(BIE_PACKAGE.STATE.in(request.getStates().stream().map(e -> e.name()).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("owner").LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (!request.getReleaseIds().isEmpty()) {
            conditions.add(RELEASE.RELEASE_ID.in(request.getReleaseIds().stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())));
        }
        if (!request.getBiePackageIds().isEmpty()) {
            conditions.add(BIE_PACKAGE.BIE_PACKAGE_ID.in(request.getBiePackageIds().stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        return conditions;
    }

    private List<SortField<?>> getSortFields(BiePackageListRequest request) {
        List<SortField<?>> sortFields = new ArrayList<>();

        for (int i = 0, len = request.getSortActives().size(); i < len; ++i) {
            String sortActive = request.getSortActives().get(i);
            SortDirection sortDirection = request.getSortDirections().get(i);

            Field field;
            switch (sortActive.toLowerCase()) {
                case "state":
                    field = BIE_PACKAGE.STATE;
                    break;
                case "branch":
                    field = RELEASE.RELEASE_NUM;
                    break;
                case "versionid":
                    field = BIE_PACKAGE.VERSION_ID;
                    break;
                case "versionname":
                    field = BIE_PACKAGE.VERSION_NAME;
                    break;
                case "owner":
                    field = APP_USER.as("owner").LOGIN_ID;
                    break;
                case "description":
                    field = BIE_PACKAGE.DESCRIPTION;
                    break;
                case "lastupdatetimestamp":
                    field = BIE_PACKAGE.LAST_UPDATE_TIMESTAMP;
                    break;
                default:
                    continue;
            }

            if (sortDirection == ASC) {
                sortFields.add(field.asc());
            } else {
                sortFields.add(field.desc());
            }
        }
        return sortFields;
    }

    private BiePackage mapperForBiePackage(Record record) {
        BiePackage biePackage = new BiePackage();

        biePackage.setBiePackageId(record.get(BIE_PACKAGE.BIE_PACKAGE_ID).toBigInteger());
        biePackage.setVersionId(record.get(BIE_PACKAGE.VERSION_ID));
        biePackage.setVersionName(record.get(BIE_PACKAGE.VERSION_NAME));
        biePackage.setDescription(record.get(BIE_PACKAGE.DESCRIPTION));
        ULong releaseId = record.get(BIE_PACKAGE.RELEASE_ID);
        if (releaseId != null) {
            biePackage.setReleaseId(releaseId.toBigInteger());
        }
        biePackage.setReleaseNum(record.get(RELEASE.RELEASE_NUM));
        biePackage.setState(BiePackageState.valueOf(record.get(BIE_PACKAGE.STATE)));

        ScoreRole ownerRole = (byte) 1 == record.get(APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer")) ? DEVELOPER : END_USER;
        boolean isOwnerAdmin = (byte) 1 == record.get(APP_USER.as("owner").IS_ADMIN.as("owner_is_admin"));
        biePackage.setOwner((isOwnerAdmin) ? new ScoreUser(record.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(), record.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")), record.get(APP_USER.as("owner").NAME.as("owner_name")), Arrays.asList(ownerRole, ADMINISTRATOR)) : new ScoreUser(record.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(), record.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")), record.get(APP_USER.as("owner").NAME.as("owner_name")), ownerRole));

        ScoreRole creatorRole = (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER;
        boolean isCreatorAdmin = (byte) 1 == record.get(APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"));
        biePackage.setCreatedBy((isCreatorAdmin) ? new ScoreUser(record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(), record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")), record.get(APP_USER.as("creator").NAME.as("creator_name")), Arrays.asList(creatorRole, ADMINISTRATOR)) : new ScoreUser(record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(), record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")), record.get(APP_USER.as("creator").NAME.as("creator_name")), creatorRole));

        ScoreRole updaterRole = (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER;
        boolean isUpdaterAdmin = (byte) 1 == record.get(APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"));
        biePackage.setLastUpdatedBy((isUpdaterAdmin) ? new ScoreUser(record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(), record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")), record.get(APP_USER.as("updater").NAME.as("updater_name")), Arrays.asList(updaterRole, ADMINISTRATOR)) : new ScoreUser(record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(), record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")), record.get(APP_USER.as("updater").NAME.as("updater_name")), updaterRole));

        biePackage.setCreationTimestamp(Date.from(record.get(BIE_PACKAGE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
        biePackage.setLastUpdateTimestamp(Date.from(record.get(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));

        ULong sourceBiePackageId = record.get(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID);
        if (sourceBiePackageId != null) {
            biePackage.setSourceBiePackageId(sourceBiePackageId.toBigInteger());
        }
        biePackage.setSourceAction(record.get(BIE_PACKAGE.SOURCE_ACTION));
        LocalDateTime sourceTimestamp = record.get(BIE_PACKAGE.SOURCE_TIMESTAMP);
        if (sourceTimestamp != null) {
            biePackage.setSourceTimestamp(Date.from(sourceTimestamp.atZone(ZoneId.systemDefault()).toInstant()));
        }
        biePackage.setSourceBiePackageVersionId(record.get(BIE_PACKAGE.as("source").VERSION_ID));
        biePackage.setSourceBiePackageVersionName(record.get(BIE_PACKAGE.as("source").VERSION_NAME));

        return biePackage;
    }

    public BigInteger createBiePackage(CreateBiePackageRequest request) {
        BiePackageRecord biePackageRecord = new BiePackageRecord();

        biePackageRecord.setVersionId(request.getVersionId());
        biePackageRecord.setVersionName(request.getVersionName());
        biePackageRecord.setDescription(request.getDescription());
        biePackageRecord.setState(BiePackageState.WIP.name());
        ULong requesterUserId = ULong.valueOf(request.getRequester().getUserId());
        biePackageRecord.setOwnerUserId(requesterUserId);
        biePackageRecord.setCreatedBy(requesterUserId);
        biePackageRecord.setLastUpdatedBy(requesterUserId);
        LocalDateTime timestamp = LocalDateTime.now();
        biePackageRecord.setCreationTimestamp(timestamp);
        biePackageRecord.setLastUpdateTimestamp(timestamp);

        return dslContext.insertInto(BIE_PACKAGE).set(biePackageRecord).returning(BIE_PACKAGE.BIE_PACKAGE_ID).fetchOne().getBiePackageId().toBigInteger();
    }

    public void updateBiePackage(UpdateBiePackageRequest request) {
        UpdateSetFirstStep<BiePackageRecord> firstStep = dslContext.update(BIE_PACKAGE);
        UpdateSetMoreStep<BiePackageRecord> step;
        if (hasLength(request.getVersionId())) {
            step = firstStep.set(BIE_PACKAGE.VERSION_ID, request.getVersionId());
        } else {
            step = firstStep.setNull(BIE_PACKAGE.VERSION_ID);
        }
        if (hasLength(request.getVersionName())) {
            step = step.set(BIE_PACKAGE.VERSION_NAME, request.getVersionName());
        } else {
            step = step.setNull(BIE_PACKAGE.VERSION_NAME);
        }
        if (hasLength(request.getDescription())) {
            step = step.set(BIE_PACKAGE.DESCRIPTION, request.getDescription());
        } else {
            step = step.setNull(BIE_PACKAGE.DESCRIPTION);
        }

        step.set(BIE_PACKAGE.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                .set(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(ULong.valueOf(request.getBiePackageId())))
                .execute();
    }

    public void updateBiePackageState(ScoreUser requester, BiePackage biePackage, BiePackageState state) {
        dslContext.update(BIE_PACKAGE)
                .set(BIE_PACKAGE.STATE, state.name())
                .set(BIE_PACKAGE.LAST_UPDATED_BY, ULong.valueOf(requester.getUserId()))
                .set(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(ULong.valueOf(biePackage.getBiePackageId())))
                .execute();
    }

    public void deleteBiePackageList(List<BigInteger> biePackageIdList) {
        if (biePackageIdList == null || biePackageIdList.isEmpty()) {
            return;
        }

        dslContext.deleteFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP).where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.in(biePackageIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))).execute();

        dslContext.deleteFrom(BIE_PACKAGE).where(BIE_PACKAGE.BIE_PACKAGE_ID.in(biePackageIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))).execute();
    }

    public PaginationResponse<BieList> getBieListInBiePackage(BieListInBiePackageRequest request) {
        SelectConditionStep<Record> conditionStep = getSelectOnConditionStep(request)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(ULong.valueOf(request.getBiePackageId())));

        int pageCount = dslContext.fetchCount(conditionStep);

        List<SortField<?>> sortFields = getSortFields(request);
        SelectFinalStep<Record> finalStep;
        if (sortFields == null || sortFields.isEmpty()) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortFields).limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortFields);
            }
        }

        return new PaginationResponse<>(pageCount, finalStep.fetch(record -> mapperForBieList(record)));
    }

    private SelectOnConditionStep<Record> getSelectOnConditionStep(BieListInBiePackageRequest request) {
        return dslContext.selectDistinct(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.STATUS,
                        ASBIEP.GUID,
                        ASCCP_MANIFEST.DEN,
                        ASCCP.PROPERTY_TERM,
                        RELEASE.RELEASE_NUM,
                        TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                        APP_USER.as("owner").LOGIN_ID.as("owner"),
                        ASBIEP.BIZ_TERM,
                        ASBIEP.REMARK,
                        TOP_LEVEL_ASBIEP.IS_DEPRECATED.as("deprecated"),
                        TOP_LEVEL_ASBIEP.DEPRECATED_REASON,
                        TOP_LEVEL_ASBIEP.DEPRECATED_REMARK,
                        TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,
                        APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.SOURCE_ACTION,
                        TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP,
                        TOP_LEVEL_ASBIEP.as("source").RELEASE_ID.as("source_release_id"),
                        ASCCP_MANIFEST.as("source_asccp_manifest").DEN.as("source_den"),
                        RELEASE.as("source_release").RELEASE_NUM.as("source_release_num"))
                .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .join(TOP_LEVEL_ASBIEP).on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .join(ASBIEP).on(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID)))
                .join(ABIE).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER.as("owner")).on(APP_USER.as("owner").APP_USER_ID.eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                .join(APP_USER.as("updater")).on(APP_USER.as("updater").APP_USER_ID.eq(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID))
                .leftJoin(TENANT_BUSINESS_CTX).on(BIZ_CTX.BIZ_CTX_ID.eq(TENANT_BUSINESS_CTX.BIZ_CTX_ID))
                .leftJoin(TOP_LEVEL_ASBIEP.as("source")).on(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.as("source").TOP_LEVEL_ASBIEP_ID))
                .leftJoin(RELEASE.as("source_release")).on(TOP_LEVEL_ASBIEP.as("source").RELEASE_ID.eq(RELEASE.as("source_release").RELEASE_ID))
                .leftJoin(ASBIEP.as("source_asbiep")).on(TOP_LEVEL_ASBIEP.as("source").ASBIEP_ID.eq(ASBIEP.as("source_asbiep").ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST.as("source_asccp_manifest")).on(ASBIEP.as("source_asbiep").BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("source_asccp_manifest").ASCCP_MANIFEST_ID))
                .leftJoin(ASCCP.as("source_asccp")).on(ASCCP_MANIFEST.as("source_asccp_manifest").ASCCP_ID.eq(ASCCP.as("source_asccp").ASCCP_ID));
    }

    private List<SortField<?>> getSortFields(BieListInBiePackageRequest request) {
        List<SortField<?>> sortFields = new ArrayList<>();

        for (int i = 0, len = request.getSortActives().size(); i < len; ++i) {
            String sortActive = request.getSortActives().get(i);
            SortDirection sortDirection = request.getSortDirections().get(i);

            Field field;
            switch (sortActive.toLowerCase()) {
                case "state":
                    field = TOP_LEVEL_ASBIEP.STATE;
                    break;
                case "branch":
                    field = RELEASE.RELEASE_NUM;
                    break;
                case "den":
                    field = ASCCP_MANIFEST.DEN;
                    break;
                case "owner":
                    field = APP_USER.as("owner").LOGIN_ID;
                    break;
                case "version":
                    field = TOP_LEVEL_ASBIEP.VERSION;
                    break;
                case "status":
                    field = TOP_LEVEL_ASBIEP.STATUS;
                    break;
                case "bizterm":
                    field = ASBIEP.BIZ_TERM;
                    break;
                case "remark":
                    field = ASBIEP.REMARK;
                    break;
                case "lastupdatetimestamp":
                    field = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP;
                    break;
                default:
                    continue;
            }

            if (sortDirection == ASC) {
                sortFields.add(field.asc());
            } else {
                sortFields.add(field.desc());
            }
        }
        return sortFields;
    }

    private BieList mapperForBieList(Record record) {
        BieList bieList = new BieList();

        bieList.setTopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
        bieList.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
        bieList.setStatus(record.get(TOP_LEVEL_ASBIEP.STATUS));
        bieList.setGuid(record.get(ASBIEP.GUID));
        bieList.setDen(record.get(ASCCP_MANIFEST.DEN));
        bieList.setPropertyTerm(record.get(ASCCP.PROPERTY_TERM));
        bieList.setReleaseNum(record.get(RELEASE.RELEASE_NUM));
        bieList.setOwnerUserId(record.get(TOP_LEVEL_ASBIEP.OWNER_USER_ID).toBigInteger());
        bieList.setOwner(record.get(APP_USER.as("owner").LOGIN_ID.as("owner")));
        bieList.setBizTerm(record.get(ASBIEP.BIZ_TERM));
        bieList.setRemark(record.get(ASBIEP.REMARK));
        bieList.setDeprecated((byte) 1 == record.get(TOP_LEVEL_ASBIEP.IS_DEPRECATED.as("deprecated")));
        bieList.setDeprecatedReason(record.get(TOP_LEVEL_ASBIEP.DEPRECATED_REASON));
        bieList.setDeprecatedRemark(record.get(TOP_LEVEL_ASBIEP.DEPRECATED_REMARK));
        bieList.setLastUpdateTimestamp(Date.from(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
        bieList.setLastUpdateUser(record.get(APP_USER.as("updater").LOGIN_ID.as("last_update_user")));
        bieList.setState(BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)));
        ULong sourceTopLevelAsbiepId = record.get(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID);
        if (sourceTopLevelAsbiepId != null) {
            bieList.setSourceTopLevelAsbiepId(sourceTopLevelAsbiepId.toBigInteger());
            bieList.setSourceAction(record.get(TOP_LEVEL_ASBIEP.SOURCE_ACTION));
            bieList.setSourceTimestamp(Date.from(record.get(TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            bieList.setSourceReleaseId(record.get(TOP_LEVEL_ASBIEP.as("source").RELEASE_ID.as("source_release_id")).toBigInteger());
            bieList.setSourceDen(record.get(ASCCP_MANIFEST.as("source_asccp_manifest").DEN.as("source_den")));
            bieList.setSourceReleaseNum(record.get(RELEASE.as("source_release").RELEASE_NUM.as("source_release_num")));
        }

        return bieList;
    }

    public void addBieToBiePackage(ScoreUser requester,
                                   BiePackage biePackage, List<BigInteger> topLevelAsbiepIdList) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        BigInteger releaseId = biePackage.getReleaseId();

        for (BigInteger topLevelAsbiepId : topLevelAsbiepIdList) {
            BigInteger topLevelAsbiepReleaseId = dslContext.select(TOP_LEVEL_ASBIEP.RELEASE_ID)
                    .from(TOP_LEVEL_ASBIEP)
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                    .fetchOneInto(BigInteger.class);
            if (releaseId != null) {
                if (!releaseId.equals(topLevelAsbiepReleaseId)) {
                    String topLevelAsbiepReleaseNum = dslContext.select(RELEASE.RELEASE_NUM)
                            .from(RELEASE)
                            .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(topLevelAsbiepReleaseId)))
                            .fetchOneInto(String.class);
                    throw new IllegalArgumentException("This BIE package does not allow the " + topLevelAsbiepReleaseNum + " release.");
                }
            } else {
                dslContext.update(BIE_PACKAGE)
                        .set(BIE_PACKAGE.RELEASE_ID, ULong.valueOf(topLevelAsbiepReleaseId))
                        .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(ULong.valueOf(biePackage.getBiePackageId())))
                        .execute();

                releaseId = topLevelAsbiepReleaseId;
            }

            BiePackageTopLevelAsbiepRecord record = new BiePackageTopLevelAsbiepRecord();
            record.setBiePackageId(ULong.valueOf(biePackage.getBiePackageId()));
            record.setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
            record.setCreatedBy(ULong.valueOf(requester.getUserId()));
            record.setCreationTimestamp(LocalDateTime.now());
            dslContext.insertInto(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                    .set(record)
                    .onDuplicateKeyUpdate()
                    .set(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID, ULong.valueOf(biePackage.getBiePackageId()))
                    .set(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId))
                    .execute();
        }
    }

    public void deleteBieInBiePackage(ScoreUser requester,
                                      BiePackage biePackage, List<BigInteger> topLevelAsbiepIdList) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        dslContext.deleteFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(and(
                        BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(ULong.valueOf(biePackage.getBiePackageId())),
                        BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                                topLevelAsbiepIdList.stream().map(e -> ULong.valueOf(e))
                                        .collect(Collectors.toSet())
                        )
                ))
                .execute();

        if (dslContext.selectCount()
                .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(ULong.valueOf(biePackage.getBiePackageId())))
                .fetchOneInto(Integer.class) == 0) {

            dslContext.update(BIE_PACKAGE)
                    .setNull(BIE_PACKAGE.RELEASE_ID)
                    .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(ULong.valueOf(biePackage.getBiePackageId())))
                    .execute();
        }
    }

    public void transferOwnership(BieOwnershipTransferRequest request) {
        ScoreUser requester = request.getRequester();
        BigInteger ownerAppUserId;
        // Issue #1576
        // Even if the administrator does not own BIE, they can transfer ownership.
        if (requester.hasRole(ADMINISTRATOR)) {
            ownerAppUserId = dslContext.select(BIE_PACKAGE.OWNER_USER_ID)
                    .from(BIE_PACKAGE)
                    .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(ULong.valueOf(request.getBiePackageId())))
                    .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
        } else {
            ownerAppUserId = requester.getUserId();
        }
        if (ownerAppUserId == null || ownerAppUserId.longValue() == 0L) {
            throw new IllegalArgumentException("Not found an owner user.");
        }

        ScoreUser targetUser = request.getTargetUser();
        if (targetUser == null) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        if (dslContext.selectCount()
                .from(BIE_PACKAGE)
                .where(and(
                        BIE_PACKAGE.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        BIE_PACKAGE.BIE_PACKAGE_ID.eq(ULong.valueOf(request.getBiePackageId()))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) == 0) {
            throw new IllegalArgumentException("This BIE package is not owned by the current user.");
        }

        dslContext.update(BIE_PACKAGE)
                .set(BIE_PACKAGE.OWNER_USER_ID, ULong.valueOf(targetUser.getUserId()))
                .where(and(
                        BIE_PACKAGE.OWNER_USER_ID.eq(ULong.valueOf(ownerAppUserId)),
                        BIE_PACKAGE.BIE_PACKAGE_ID.eq(ULong.valueOf(request.getBiePackageId()))
                ))
                .execute();
    }

}
