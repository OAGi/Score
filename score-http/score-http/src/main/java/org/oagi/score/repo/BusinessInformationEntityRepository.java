package org.oagi.score.repo;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TopLevelAsbiepRecord;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.helper.Utility.sha256;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.bie.model.BieState.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Routines.levenshtein;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BusinessInformationEntityRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ApplicationConfigurationService configService;
    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    public class InsertTopLevelAsbiepArguments {
        private ULong releaseId;
        private ULong userId;
        private BieState bieState = WIP;
        private String version;
        private String status;

        private boolean inverseMode;
        private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        private ULong basedTopLevelAsbiepId;

        private ULong sourceTopLevelAsbiepId;
        private String sourceAction;
        private LocalDateTime sourceTimestamp;

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

        public InsertTopLevelAsbiepArguments setBasedTopLevelAsbiepId(BigInteger basedTopLevelAsbiepId) {
            return setBasedTopLevelAsbiepId(ULong.valueOf(basedTopLevelAsbiepId));
        }

        public InsertTopLevelAsbiepArguments setBasedTopLevelAsbiepId(ULong basedTopLevelAsbiepId) {
            this.basedTopLevelAsbiepId = basedTopLevelAsbiepId;
            return this;
        }

        public InsertTopLevelAsbiepArguments setSource(BigInteger sourceTopLevelAsbiepId, String sourceAction) {
            this.sourceTopLevelAsbiepId = ULong.valueOf(sourceTopLevelAsbiepId);
            this.sourceAction = sourceAction;
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

        public ULong getBasedTopLevelAsbiepId() {
            return basedTopLevelAsbiepId;
        }

        public ULong getSourceTopLevelAsbiepId() {
            return sourceTopLevelAsbiepId;
        }

        public String getSourceAction() {
            return sourceAction;
        }

        public LocalDateTime getSourceTimestamp() {
            return (sourceTimestamp != null) ? sourceTimestamp : getTimestamp();
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

        if (arguments.getBasedTopLevelAsbiepId() != null) {
            record.setBasedTopLevelAsbiepId(arguments.getBasedTopLevelAsbiepId());
        }

        if (arguments.getSourceTopLevelAsbiepId() != null) {
            record.setSourceTopLevelAsbiepId(arguments.getSourceTopLevelAsbiepId());
            record.setSourceAction(arguments.getSourceAction());
            record.setSourceTimestamp(arguments.getSourceTimestamp());
        }

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

        private final List<Field> selectFields = new ArrayList<>();
        private final List<Condition> conditions = new ArrayList<>();
        private List<SortField<?>> sortFields = new ArrayList<>();
        private int offset = -1;
        private int numberOfRows = -1;

        private String den;
        private String type;

        SelectBieListArguments() {
            selectFields.addAll(Arrays.asList(
                    TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                    TOP_LEVEL_ASBIEP.VERSION,
                    TOP_LEVEL_ASBIEP.STATUS,
                    ASBIEP.GUID,
                    ASCCP_MANIFEST.DEN,
                    ASCCP.PROPERTY_TERM,
                    ASBIEP.DISPLAY_NAME,
                    RELEASE.RELEASE_NUM,
                    TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                    APP_USER.as("owner").LOGIN_ID.as("owner"),
                    APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                    APP_USER.as("owner").IS_ADMIN.as("owner_is_admin"),
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
                    ASBIEP.as("source_asbiep").DISPLAY_NAME.as("source_display_name"),
                    RELEASE.as("source_release").RELEASE_NUM.as("source_release_num"),

                    TOP_LEVEL_ASBIEP.as("based").TOP_LEVEL_ASBIEP_ID.as("based_top_level_asbiep_id"),
                    TOP_LEVEL_ASBIEP.as("based").RELEASE_ID.as("based_top_level_asbiep_release_id"),
                    RELEASE.as("based_release").RELEASE_NUM.as("based_top_level_asbiep_release_num"),
                    ASCCP_MANIFEST.as("based_asccp_manifest").DEN.as("based_top_level_asbiep_den"),
                    ASBIEP.as("based_asbiep").DISPLAY_NAME.as("based_top_level_asbiep_display_name")));
        }

        public List<Field> selectFields() {
            return this.selectFields;
        }

        public SelectBieListArguments setDen(String den) {
            if (StringUtils.hasLength(den)) {
                conditions.addAll(contains(den, ASCCP_MANIFEST.DEN, ASBIEP.DISPLAY_NAME));
                selectFields.add(
                        val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(den.toLowerCase()))
                                        .div(greatest(length(ASCCP.PROPERTY_TERM), length(den))))
                                .as("score")
                );
                sortFields.add(field("score").desc());
            }
            return this;
        }

        public SelectBieListArguments setPropertyTerm(String propertyTerm) {
            if (StringUtils.hasLength(propertyTerm)) {
                conditions.addAll(contains(propertyTerm, ASCCP.PROPERTY_TERM));
            }
            return this;
        }

        public SelectBieListArguments setVersion(String version) {
            if (StringUtils.hasLength(version)) {
                conditions.addAll(contains(version, TOP_LEVEL_ASBIEP.VERSION));
            }
            return this;
        }

        public SelectBieListArguments setRemark(String remark) {
            if (StringUtils.hasLength(remark)) {
                conditions.addAll(contains(remark, ASBIEP.REMARK));
            }
            return this;
        }

        public SelectBieListArguments setBusinessContext(String businessContext) {
            if (StringUtils.hasLength(businessContext)) {
                conditions.add(or(Arrays.asList(businessContext.split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e))
                        .map(e -> and(contains(e, BIZ_CTX.NAME)))
                        .collect(Collectors.toList())));
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

        public SelectBieListArguments setTopLevelAsbiepIds(List<BigInteger> topLevelAsbiepIds) {
            if (!topLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                        topLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public SelectBieListArguments setBasedTopLevelAsbiepIds(List<BigInteger> basedTopLevelAsbiepIds) {
            if (!basedTopLevelAsbiepIds.isEmpty()) {
                List<ULong> result = basedTopLevelAsbiepIds.stream()
                        .map(e -> ULong.valueOf(e)).collect(Collectors.toList());
                List<ULong> allInheritedTopLevelAsbiepIds = new ArrayList<>();
                while (!result.isEmpty()) {
                    allInheritedTopLevelAsbiepIds.addAll(result);
                    result = dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                            .from(TOP_LEVEL_ASBIEP)
                            .where(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.in(result))
                            .fetchInto(ULong.class);
                }

                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(allInheritedTopLevelAsbiepIds));
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

        public SelectBieListArguments setDeprecated(Boolean deprecated) {
            if (deprecated != null) {
                conditions.add(TOP_LEVEL_ASBIEP.IS_DEPRECATED.eq((byte) (deprecated ? 1 : 0)));
            }
            return this;
        }

        public SelectBieListArguments setBieIdAndType(BigInteger bieId, List<String> types) {
            if (bieId != null && types.size() == 1) {
                String type = types.get(0);
                if (type.equals("ASBIE")) {
                    conditions.add(ASBIE.ASBIE_ID.eq(ULong.valueOf(bieId)));
                } else if (type.equals("BBIE")) {
                    conditions.add(BBIE.BBIE_ID.eq(ULong.valueOf(bieId)));
                }
            }
            return this;
        }

        public SelectBieListArguments setAsccBccDen(String den) {
            this.den = den;
            return this;
        }

        public SelectBieListArguments setType(String type) {
            this.type = type;
            return this;
        }

        public SelectBieListArguments setOwnerLoginIds(List<String> ownerLoginIds) {
            if (!ownerLoginIds.isEmpty()) {
                conditions.add(APP_USER.as("owner").LOGIN_ID.in(ownerLoginIds));
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

        public SelectBieListArguments setSort(List<String> fields, List<SortDirection> directions) {
            for (int i = 0, len = directions.size(); i < len; ++i) {
                String sortActive = fields.get(i);
                SortDirection sortDirection = directions.get(i);

                Field field;
                switch (sortActive) {
                    case "state":
                        field = TOP_LEVEL_ASBIEP.STATE;
                        break;
                    case "branch":
                        field = RELEASE.RELEASE_NUM;
                        break;
                    case "topLevelAsccpPropertyTerm":
                        field = ASCCP.PROPERTY_TERM;
                        break;
                    case "den":
                        field = ASCCP_MANIFEST.DEN;
                        break;
                    case "releaseNum":
                        field = RELEASE.RELEASE_NUM;
                        break;
                    case "owner":
                        field = APP_USER.as("owner").LOGIN_ID;
                        break;
                    case "businessContexts":
                        field = BIZ_CTX.NAME;
                        break;
                    case "version":
                        field = TOP_LEVEL_ASBIEP.VERSION;
                        break;
                    case "status":
                        field = TOP_LEVEL_ASBIEP.STATUS;
                        break;
                    case "bizTerm":
                        field = ASBIEP.BIZ_TERM;
                        break;
                    case "remark":
                        field = ASBIEP.REMARK;
                        break;
                    case "lastUpdateTimestamp":
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

            return this;
        }

        public SelectBieListArguments setOffset(int offset, int numberOfRows) {
            this.offset = offset;
            this.numberOfRows = numberOfRows;
            return this;
        }

        public SelectBieListArguments setReleaseIds(List<BigInteger> releaseIds) {
            if (releaseIds != null && !releaseIds.isEmpty()) {
                releaseIds = releaseIds.stream().filter(e -> e.longValue() > 0L).collect(Collectors.toList());
            }

            if (releaseIds != null && !releaseIds.isEmpty()) {
                if (releaseIds.size() == 1) {
                    conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(ULong.valueOf(releaseIds.get(0))));
                } else {
                    conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.in(releaseIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())));
                }
            }
            return this;
        }

        public SelectBieListArguments setOwnedByDeveloper(Boolean ownedByDeveloper) {
            if (ownedByDeveloper != null) {
                conditions.add(APP_USER.as("owner").IS_DEVELOPER.eq(ownedByDeveloper ? (byte) 1 : 0));
            }
            return this;
        }

        public SelectBieListArguments setTenantBusinessCtx(boolean isAdmin, List<ULong> userTenantIds) {
            if (configService.isTenantEnabled() && !isAdmin) {
                conditions.add(BIZ_CTX.BIZ_CTX_ID.in(dslContext.select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                        .from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.in(userTenantIds))));
            }
            return this;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public List<SortField<?>> getSortFields() {
            return this.sortFields;
        }

        public int getOffset() {
            return offset;
        }

        public int getNumberOfRows() {
            return numberOfRows;
        }

        public String getDen() {
            return den;
        }

        public String getType() {
            return type;
        }

        public <E> PaginationResponse<E> fetchInto(Class<? extends E> type) {
            return selectBieList(this, type);
        }

        public <E> PaginationResponse<E> fetchAsbieBbieInto(List<String> types, Class<? extends E> type) {
            return selectAsbieBbieList(this, types, type);
        }
    }

    public SelectBieListArguments selectBieLists() {
        return new SelectBieListArguments();
    }

    private SelectOnConditionStep<Record> getSelectOnConditionStep(SelectBieListArguments arguments) {
        List<Field> selectFields = arguments.selectFields();
        return dslContext.selectDistinct(selectFields)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                )
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
                .leftJoin(ASCCP.as("source_asccp")).on(ASCCP_MANIFEST.as("source_asccp_manifest").ASCCP_ID.eq(ASCCP.as("source_asccp").ASCCP_ID))

                .leftJoin(TOP_LEVEL_ASBIEP.as("based")).on(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.as("based").TOP_LEVEL_ASBIEP_ID))
                .leftJoin(RELEASE.as("based_release")).on(TOP_LEVEL_ASBIEP.as("based").RELEASE_ID.eq(RELEASE.as("based_release").RELEASE_ID))
                .leftJoin(ASBIEP.as("based_asbiep")).on(TOP_LEVEL_ASBIEP.as("based").ASBIEP_ID.eq(ASBIEP.as("based_asbiep").ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST.as("based_asccp_manifest")).on(ASBIEP.as("based_asbiep").BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("based_asccp_manifest").ASCCP_MANIFEST_ID));
    }

    private <E> PaginationResponse<E> selectBieList(SelectBieListArguments arguments, Class<? extends E> type) {
        SelectOnConditionStep<Record> step = getSelectOnConditionStep(arguments);
        SelectConnectByStep<Record> conditionStep = step.where(arguments.getConditions());

        int pageCount = dslContext.fetchCount(conditionStep);

        List<SortField<?>> sortFields = arguments.getSortFields();
        SelectWithTiesAfterOffsetStep<Record> offsetStep = null;
        if (!sortFields.isEmpty()) {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = conditionStep.orderBy(sortFields)
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

    public SelectOrderByStep getAsbieList(SelectBieListArguments arguments) {
        List<Condition> conditions = arguments.getConditions().stream().collect(Collectors.toList());
        if (arguments.getDen() != null && StringUtils.hasLength(arguments.getDen())) {
            conditions.add(or(
                    ASCC_MANIFEST.DEN.contains(arguments.getDen()),
                    ASCCP_MANIFEST.DEN.contains(arguments.getDen())
            ));
        }
        return dslContext.select(
                        inline("ASBIE").as("type"),
                        ASBIE.ASBIE_ID.as("bieId"),
                        ASBIE.GUID,
                        ASCC_MANIFEST.DEN,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.STATUS,
                        BIZ_CTX.NAME.as("bizCtxName"),
                        RELEASE.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        ASBIE.REMARK,
                        APP_USER.as("updater").LOGIN_ID.as("lastUpdateUser"),
                        APP_USER.as("owner").LOGIN_ID.as("owner"),
                        APP_USER.as("owner").APP_USER_ID.as("ownerUserId"),
                        ASBIE.LAST_UPDATE_TIMESTAMP,
                        ASBIE.IS_USED.as("used"),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        ASCCP.PROPERTY_TERM.as("topLevelAsccpPropertyTerm"))
                .from(ASBIE)
//                next two joins to get DEN
                .join(ASCC_MANIFEST).on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
//                join with TOP_LEVEL_ASBIEP to get state, version, status
                .join(TOP_LEVEL_ASBIEP).on(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                ))
//                next three joins to get top level property term
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
//                join w RELEASE to get RELEASE_NUM
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
//                next two joins to get BIZ_CTX.NAME
                .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID))
//                join with APP_USER to get updater and owner
                .join(APP_USER.as("updater"))
                .on(ASBIE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(APP_USER.as("owner"))
                .on(ASBIE.CREATED_BY.eq(APP_USER.as("owner").APP_USER_ID))
                .where(conditions);
    }

    public SelectOrderByStep getBbieList(SelectBieListArguments arguments) {
        List<Condition> conditions = arguments.getConditions().stream().collect(Collectors.toList());
        if (arguments.getDen() != null && StringUtils.hasLength(arguments.getDen())) {
            conditions.add(or(
                    BCC_MANIFEST.DEN.contains(arguments.getDen()),
                    ASCCP_MANIFEST.DEN.contains(arguments.getDen())
            ));
        }
        return dslContext.select(
                        inline("BBIE").as("type"),
                        BBIE.BBIE_ID.as("bieId"),
                        BBIE.GUID,
                        BCC_MANIFEST.DEN,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.STATUS,
                        BIZ_CTX.NAME.as("bizCtxName"),
                        RELEASE.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        BBIE.REMARK,
                        APP_USER.as("updater").LOGIN_ID.as("lastUpdateUser"),
                        APP_USER.as("owner").LOGIN_ID.as("owner"),
                        APP_USER.as("owner").APP_USER_ID.as("ownerUserId"),
                        BBIE.LAST_UPDATE_TIMESTAMP,
                        BBIE.IS_USED.as("used"),
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        ASCCP.PROPERTY_TERM.as("topLevelAsccpPropertyTerm"))
                .from(BBIE)
                //                next two joins to get DEN
                .join(BCC_MANIFEST).on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                //                join with TOP_LEVEL_ASBIEP to get state, version, status
                .join(TOP_LEVEL_ASBIEP).on(and(
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                ))
                //                next three joins to get top level property term
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                //                join w RELEASE to get RELEASE_NUM
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                //                next two joins to get BIZ_CTX.NAME
                .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID))
                //                join with APP_USER to get updater
                .join(APP_USER.as("updater"))
                .on(BBIE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(APP_USER.as("owner"))
                .on(BBIE.CREATED_BY.eq(APP_USER.as("owner").APP_USER_ID))
                .where(conditions);
    }

    private <E> PaginationResponse<E> selectAsbieBbieList(SelectBieListArguments arguments, List<String> types, Class<? extends E> type) {
        SelectOrderByStep select = null;
        if (types.contains("ASBIE") || types.isEmpty()) {
            select = getAsbieList(arguments);
        }
        if (types.contains("BBIE") || types.isEmpty()) {
            select = (select != null) ? select.union(getBbieList(arguments)) :
                    getBbieList(arguments);
        }

        int pageCount = dslContext.fetchCount(select);

        List<SortField<?>> sortFields = arguments.getSortFields();
        SelectWithTiesAfterOffsetStep offsetStep = null;
        if (!sortFields.isEmpty()) {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = select.orderBy(sortFields).limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        } else {
            if (arguments.getOffset() >= 0 && arguments.getNumberOfRows() >= 0) {
                offsetStep = select.limit(arguments.getOffset(), arguments.getNumberOfRows());
            }
        }

        return new PaginationResponse<>(pageCount,
                (offsetStep != null) ?
                        offsetStep.fetchInto(type) : select.fetchInto(type));
    }

}
