package org.oagi.score.repo;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TopLevelAsbiepRecord;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.field;
import static org.oagi.score.gateway.http.helper.Utility.sha256;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.bie.model.BieState.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BusinessInformationEntityRepository {

    @Autowired
    private DSLContext dslContext;

    public class InsertTopLevelAsbiepArguments {
        private ULong releaseId;
        private ULong userId;
        private BieState bieState = WIP;
        private String version;
        private String status;

        private boolean inverseMode;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertTopLevelAsbiepArguments setReleaseId(BigInteger releaseId) {
            return setReleaseId(ULong.valueOf(releaseId));
        }

        public InsertTopLevelAsbiepArguments setReleaseId(ULong releaseId) {
            this.releaseId = releaseId;
            return this;
        }

        public InsertTopLevelAsbiepArguments setBieState(BieState bieState) {
            this.bieState = bieState;
            return this;
        }

        public InsertTopLevelAsbiepArguments setVersion(String version) {
            this.version = version;
            return this;
        }

        public InsertTopLevelAsbiepArguments setStatus(String status) {
            this.status = status;
            return this;
        }

        public InsertTopLevelAsbiepArguments setInverseMode(boolean inverseMode) {
            this.inverseMode = inverseMode;
            return this;
        }

        public InsertTopLevelAsbiepArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }

        public InsertTopLevelAsbiepArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public InsertTopLevelAsbiepArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }

        public InsertTopLevelAsbiepArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }

        public InsertTopLevelAsbiepArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ULong getReleaseId() {
            return releaseId;
        }

        public BieState getBieState() {
            return bieState;
        }

        public String getVersion() {
            return version;
        }

        public String getStatus() {
            return status;
        }

        public boolean isInverseMode() {
            return inverseMode;
        }

        public ULong getUserId() {
            return userId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public ULong execute() {
            return insertTopLevelAsbiep(this);
        }
    }

    public InsertTopLevelAsbiepArguments insertTopLevelAsbiep() {
        return new InsertTopLevelAsbiepArguments();
    }

    private ULong insertTopLevelAsbiep(InsertTopLevelAsbiepArguments arguments) {
        TopLevelAsbiepRecord record = new TopLevelAsbiepRecord();
        record.setOwnerUserId(arguments.getUserId());
        record.setReleaseId(arguments.getReleaseId());
        record.setState(arguments.getBieState().name());
        record.setVersion(arguments.getVersion());
        record.setStatus(arguments.getStatus());
        record.setInverseMode((byte) (arguments.isInverseMode() ? 1 : 0));
        record.setLastUpdatedBy(arguments.getUserId());
        record.setLastUpdateTimestamp(arguments.getTimestamp());

        return dslContext.insertInto(TOP_LEVEL_ASBIEP)
                .set(record)
                .returningResult(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .fetchOne().value1();
    }

    public class InsertAbieArguments {
        private ULong userId;
        private ULong accManifestId;
        private String path;
        private ULong topLevelAsbiepId;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertAbieArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }

        public InsertAbieArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public InsertAbieArguments setAccManifestId(BigInteger accManifestId) {
            return setAccManifestId(ULong.valueOf(accManifestId));
        }

        public InsertAbieArguments setAccManifestId(ULong accManifestId) {
            this.accManifestId = accManifestId;
            return this;
        }

        public String getPath() {
            return path;
        }

        public InsertAbieArguments setPath(String path) {
            this.path = path;
            return this;
        }

        public InsertAbieArguments setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
            return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
        }

        public InsertAbieArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
            return this;
        }

        public InsertAbieArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }

        public InsertAbieArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }

        public InsertAbieArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ULong getUserId() {
            return userId;
        }

        public ULong getAccManifestId() {
            return accManifestId;
        }

        public ULong getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public ULong execute() {
            return insertAbie(this);
        }
    }

    public InsertAbieArguments insertAbie() {
        return new InsertAbieArguments();
    }

    private ULong insertAbie(InsertAbieArguments arguments) {
        return dslContext.insertInto(ABIE)
                .set(ABIE.GUID, ScoreGuid.randomGuid())
                .set(ABIE.BASED_ACC_MANIFEST_ID, arguments.getAccManifestId())
                .set(ABIE.PATH, arguments.getPath())
                .set(ABIE.HASH_PATH, sha256(arguments.getPath()))
                .set(ABIE.CREATED_BY, arguments.getUserId())
                .set(ABIE.LAST_UPDATED_BY, arguments.getUserId())
                .set(ABIE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(ABIE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                .returningResult(ABIE.ABIE_ID)
                .fetchOne().value1();
    }

    public class InsertBizCtxAssignmentArguments {
        private ULong topLevelAsbiepId;
        private List<ULong> bizCtxIds = Collections.emptyList();

        public InsertBizCtxAssignmentArguments setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
            return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
        }

        public InsertBizCtxAssignmentArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
            return this;
        }

        public InsertBizCtxAssignmentArguments setBizCtxIds(List<BigInteger> bizCtxIds) {
            if (bizCtxIds != null && !bizCtxIds.isEmpty()) {
                this.bizCtxIds = bizCtxIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList());
            }
            return this;
        }

        public ULong getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }

        public List<ULong> getBizCtxIds() {
            return bizCtxIds;
        }

        public void execute() {
            insertBizCtxAssignments(this);
        }
    }

    public InsertBizCtxAssignmentArguments insertBizCtxAssignments() {
        return new InsertBizCtxAssignmentArguments();
    }

    private void insertBizCtxAssignments(InsertBizCtxAssignmentArguments arguments) {
        dslContext.batch(
                arguments.getBizCtxIds().stream().map(bizCtxId -> {
                    return dslContext.insertInto(BIZ_CTX_ASSIGNMENT)
                            .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, arguments.topLevelAsbiepId)
                            .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, bizCtxId);
                }).collect(Collectors.toList())
        ).execute();
    }

    public class InsertAsbiepArguments {
        private ULong asccpManifestId;
        private ULong roleOfAbieId;
        private ULong topLevelAsbiepId;
        private String path;
        private ULong userId;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        public InsertAsbiepArguments setAsccpManifestId(BigInteger asccpManifestId) {
            return setAsccpManifestId(ULong.valueOf(asccpManifestId));
        }

        public InsertAsbiepArguments setAsccpManifestId(ULong asccpManifestId) {
            this.asccpManifestId = asccpManifestId;
            return this;
        }

        public InsertAsbiepArguments setRoleOfAbieId(BigInteger roleOfAbieId) {
            return setRoleOfAbieId(ULong.valueOf(roleOfAbieId));
        }

        public InsertAsbiepArguments setRoleOfAbieId(ULong roleOfAbieId) {
            this.roleOfAbieId = roleOfAbieId;
            return this;
        }

        public InsertAsbiepArguments setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
            return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
        }

        public InsertAsbiepArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
            return this;
        }

        public String getPath() {
            return path;
        }

        public InsertAsbiepArguments setPath(String path) {
            this.path = path;
            return this;
        }

        public InsertAsbiepArguments setUserId(BigInteger userId) {
            return setUserId(ULong.valueOf(userId));
        }

        public InsertAsbiepArguments setUserId(ULong userId) {
            this.userId = userId;
            return this;
        }

        public InsertAsbiepArguments setTimestamp(long millis) {
            return setTimestamp(new Timestamp(millis).toLocalDateTime());
        }

        public InsertAsbiepArguments setTimestamp(Date date) {
            return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
        }

        public InsertAsbiepArguments setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ULong getAsccpManifestId() {
            return asccpManifestId;
        }

        public ULong getRoleOfAbieId() {
            return roleOfAbieId;
        }

        public ULong getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }

        public ULong getUserId() {
            return userId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public ULong execute() {
            return insertAsbiep(this);
        }
    }

    public InsertAsbiepArguments insertAsbiep() {
        return new InsertAsbiepArguments();
    }

    private ULong insertAsbiep(InsertAsbiepArguments arguments) {
        return dslContext.insertInto(ASBIEP)
                .set(ASBIEP.GUID, ScoreGuid.randomGuid())
                .set(ASBIEP.BASED_ASCCP_MANIFEST_ID, arguments.getAsccpManifestId())
                .set(ASBIEP.PATH, arguments.getPath())
                .set(ASBIEP.HASH_PATH, sha256(arguments.getPath()))
                .set(ASBIEP.ROLE_OF_ABIE_ID, arguments.getRoleOfAbieId())
                .set(ASBIEP.CREATED_BY, arguments.getUserId())
                .set(ASBIEP.LAST_UPDATED_BY, arguments.getUserId())
                .set(ASBIEP.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(ASBIEP.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                .returningResult(ASBIEP.ASBIEP_ID)
                .fetchOne().value1();
    }

    public class UpdateTopLevelAsbiepArguments {
        private ULong asbiepId;
        private ULong topLevelAsbiepId;

        public UpdateTopLevelAsbiepArguments setAsbiepId(ULong asbiepId) {
            this.asbiepId = asbiepId;
            return this;
        }

        public UpdateTopLevelAsbiepArguments setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
            return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId));
        }

        public UpdateTopLevelAsbiepArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
            this.topLevelAsbiepId = topLevelAsbiepId;
            return this;
        }

        public ULong getAsbiepId() {
            return asbiepId;
        }

        public ULong getTopLevelAsbiepId() {
            return topLevelAsbiepId;
        }

        public void execute() {
            updateTopLevelAsbiep(this);
        }
    }

    public UpdateTopLevelAsbiepArguments updateTopLevelAsbiep() {
        return new UpdateTopLevelAsbiepArguments();
    }

    private void updateTopLevelAsbiep(UpdateTopLevelAsbiepArguments arguments) {
        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.ASBIEP_ID, arguments.getAsbiepId())
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(arguments.getTopLevelAsbiepId()))
                .execute();
    }

    public class SelectBieListArguments {

        private final List<Condition> conditions = new ArrayList();
        private SortField sortField;
        private int offset = -1;
        private int numberOfRows = -1;

        private String den;
        private String type;

        public SelectBieListArguments setDen(String den) {
            if (StringUtils.hasLength(den)) {
                conditions.addAll(contains(den, ASCCP.DEN));
            }
            return this;
        }

        public SelectBieListArguments setPropertyTerm(String propertyTerm) {
            if (StringUtils.hasLength(propertyTerm)) {
                conditions.addAll(contains(propertyTerm, ASCCP.PROPERTY_TERM));
            }
            return this;
        }

        public SelectBieListArguments setBusinessContext(String businessContext) {
            if (StringUtils.hasLength(businessContext)) {
                conditions.addAll(contains(businessContext, BIZ_CTX.NAME));
            }
            return this;
        }

        public SelectBieListArguments setAsccpManifestId(BigInteger asccpManifestId) {
            if (asccpManifestId != null && asccpManifestId.longValue() > 0L) {
                conditions.add(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)));
            }
            return this;
        }

        public SelectBieListArguments setExcludePropertyTerms(List<String> excludePropertyTerms) {
            if (!excludePropertyTerms.isEmpty()) {
                conditions.add(ASCCP.PROPERTY_TERM.notIn(excludePropertyTerms));
            }
            return this;
        }

        public SelectBieListArguments setExcludeTopLevelAsbiepIds(List<BigInteger> excludeTopLevelAsbiepIds) {
            if (!excludeTopLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.notIn(
                        excludeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public SelectBieListArguments setIncludeTopLevelAsbiepIds(List<BigInteger> includeTopLevelAsbiepIds) {
            if (!includeTopLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                        includeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public SelectBieListArguments setStates(List<BieState> states) {
            if (!states.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.STATE.in(states.stream().map(e -> e.name()).collect(Collectors.toList())));
            }
            return this;
        }

        public SelectBieListArguments setType(String type) {
            this.type = type;
            return this;
        }

        public SelectBieListArguments setOwnerLoginIds(List<String> ownerLoginIds) {
            if (!ownerLoginIds.isEmpty()) {
                conditions.add(APP_USER.LOGIN_ID.in(ownerLoginIds));
            }
            return this;
        }

        public SelectBieListArguments setUpdaterLoginIds(List<String> updaterLoginIds) {
            if (!updaterLoginIds.isEmpty()) {
                conditions.add(APP_USER.as("updater").LOGIN_ID.in(updaterLoginIds));
            }
            return this;
        }

        public SelectBieListArguments setUpdateDate(Date from, Date to) {
            return setUpdateDate(
                    (from != null) ? new Timestamp(from.getTime()).toLocalDateTime() : null,
                    (to != null) ? new Timestamp(to.getTime()).toLocalDateTime() : null
            );
        }

        public SelectBieListArguments setUpdateDate(LocalDateTime from, LocalDateTime to) {
            if (from != null) {
                conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(from));
            }
            if (to != null) {
                conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.lessThan(to));
            }
            return this;
        }

        public SelectBieListArguments setAccess(ULong userId, AccessPrivilege access) {
            if (access != null) {
                switch (access) {
                    case CanEdit:
                        conditions.add(
                                and(
                                        TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                                )
                        );
                        break;

                    case CanView:
                        conditions.add(
                                or(
                                        TOP_LEVEL_ASBIEP.STATE.in(QA.name(), Production.name()),
                                        and(
                                                TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                                TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                                        )
                                )
                        );
                        break;
                }
            }
            return this;
        }

        public SelectBieListArguments setSort(String field, String direction) {
            if (StringUtils.hasLength(field)) {
                switch (field) {
                    case "state":
                        if ("asc".equals(direction)) {
                            this.sortField = TOP_LEVEL_ASBIEP.STATE.asc();
                        } else if ("desc".equals(direction)) {
                            this.sortField = TOP_LEVEL_ASBIEP.STATE.desc();
                        }

                        break;

                    case "topLevelAsccpPropertyTerm":
                        if ("asc".equals(direction)) {
                            this.sortField = ASCCP.PROPERTY_TERM.asc();
                        } else if ("desc".equals(direction)) {
                            this.sortField = ASCCP.PROPERTY_TERM.desc();
                        }

                        break;

                    case "den":
                        if ("asc".equals(direction)) {
                            this.sortField = ASCCP.DEN.asc();
                        } else if ("desc".equals(direction)) {
                            this.sortField = ASCCP.DEN.desc();
                        }
                        break;

                    case "releaseNum":
                        if ("asc".equals(direction)) {
                            this.sortField = RELEASE.RELEASE_NUM.asc();
                        } else if ("desc".equals(direction)) {
                            this.sortField = RELEASE.RELEASE_NUM.desc();
                        }

                        break;

                    case "lastUpdateTimestamp":
                        if ("asc".equals(direction)) {
                            this.sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.asc();
                        } else if ("desc".equals(direction)) {
                            this.sortField = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.desc();
                        }

                        break;
                }
            }

            return this;
        }

        public SelectBieListArguments setOffset(int offset, int numberOfRows) {
            this.offset = offset;
            this.numberOfRows = numberOfRows;
            return this;
        }

        public SelectBieListArguments setReleaseId(BigInteger releaseId) {
            if (releaseId != null && releaseId.longValue() > 0) {
                conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(ULong.valueOf(releaseId)));
            }
            return this;
        }

        public SelectBieListArguments setOwnedByDeveloper(Boolean ownedByDeveloper) {
            if (ownedByDeveloper != null) {
                conditions.add(APP_USER.IS_DEVELOPER.eq(ownedByDeveloper ? (byte) 1 : 0));
            }
            return this;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public SortField getSortField() {
            return sortField;
        }

        public int getOffset() {
            return offset;
        }

        public int getNumberOfRows() {
            return numberOfRows;
        }

        public <E> PaginationResponse<E> fetchInto(Class<? extends E> type) {
            return selectBieList(this, type);
        }
    }

    public SelectBieListArguments selectBieLists() {
        return new SelectBieListArguments();
    }

    private SelectOnConditionStep<Record14<
            ULong, String, String, String, String,
            String, String, ULong, String, String,
            String, LocalDateTime, String, String>> getSelectOnConditionStep() {
        return dslContext.selectDistinct(
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                TOP_LEVEL_ASBIEP.VERSION,
                TOP_LEVEL_ASBIEP.STATUS,
                ABIE.GUID,
                ASCCP.DEN,
                ASCCP.PROPERTY_TERM,
                RELEASE.RELEASE_NUM,
                TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                APP_USER.LOGIN_ID.as("owner"),
                ASBIEP.BIZ_TERM,
                ASBIEP.REMARK,
                TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                TOP_LEVEL_ASBIEP.STATE)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                )
                .join(ABIE).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(APP_USER.APP_USER_ID.eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                .join(APP_USER.as("updater")).on(APP_USER.as("updater").APP_USER_ID.eq(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID));
    }

    private <E> PaginationResponse<E> selectBieList(SelectBieListArguments arguments, Class<? extends E> type) {
        SelectOnConditionStep<Record14<
                ULong, String, String, String, String,
                String, String, ULong, String, String,
                String, LocalDateTime, String, String>> step = getSelectOnConditionStep();

        SelectConnectByStep<Record14<
                ULong, String, String, String, String,
                String, String, ULong, String, String,
                String, LocalDateTime, String, String>> conditionStep = step.where(arguments.getConditions());

        int pageCount = dslContext.fetchCount(conditionStep);

        SortField sortField = arguments.getSortField();
        SelectWithTiesAfterOffsetStep<Record14<
                ULong, String, String, String, String,
                String, String, ULong, String, String,
                String, LocalDateTime, String, String>> offsetStep = null;
        if (sortField != null) {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = conditionStep.orderBy(sortField)
                        .limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        } else {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = conditionStep
                        .limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        }

        return new PaginationResponse<>(pageCount,
                (offsetStep != null) ?
                        offsetStep.fetchInto(type) : conditionStep.fetchInto(type));
    }

    public BigInteger getAsccpManifestIdByTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        return dslContext.select(ASBIEP.BASED_ASCCP_MANIFEST_ID)
                .from(ASBIEP)
                .join(TOP_LEVEL_ASBIEP).on(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID)
                ))
                .where(and(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId))
                ))
                .fetchOptionalInto(BigInteger.class).orElse(null);
    }

    public AsccpManifestRecord getAsccpManifestIdByTopLevelAsbiepIdAndReleaseId(BigInteger topLevelAsbiepId, BigInteger releaseId) {
        BigInteger asccp_id = dslContext.select(ASCCP_MANIFEST.ASCCP_ID)
                .from(ASBIEP)
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(TOP_LEVEL_ASBIEP).on(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID)
                ))
                .where(and(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId))
                ))
                .fetchOptionalInto(BigInteger.class).orElse(null);

        return dslContext.selectFrom(ASCCP_MANIFEST)
                .where(and(ASCCP_MANIFEST.ASCCP_ID.eq(ULong.valueOf(asccp_id)),
                        ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .fetchOptionalInto(AsccpManifestRecord.class).orElse(null);
    }

    public List<BigInteger> getReusingTopLevelAsbiepIds(BigInteger reusedTopLevelAsbiepId) {
        return dslContext.select(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ASBIE)
                .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(reusedTopLevelAsbiepId))
                ))
                .fetchInto(BigInteger.class);
    }

}
