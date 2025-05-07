package org.oagi.score.gateway.http.api.bie_management.repository;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.external.data.BieList;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.*;
import static org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.gateway.http.common.model.SortDirection.ASC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Routines.levenshtein;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Deprecated
@Repository
public class BusinessInformationEntityRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ApplicationConfigurationService configService;

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

        public SelectBieListArguments setLibraryId(LibraryId libraryId) {
            if (libraryId != null) {
                conditions.add(LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())));
            }
            return this;
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

        public SelectBieListArguments setAsccpManifestId(AsccpManifestId asccpManifestId) {
            if (asccpManifestId != null) {
                conditions.add(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId.value())));
            }
            return this;
        }

        public SelectBieListArguments setExcludePropertyTerms(List<String> excludePropertyTerms) {
            if (!excludePropertyTerms.isEmpty()) {
                conditions.add(ASCCP.PROPERTY_TERM.notIn(excludePropertyTerms));
            }
            return this;
        }

        public SelectBieListArguments setTopLevelAsbiepIds(List<TopLevelAsbiepId> topLevelAsbiepIds) {
            if (!topLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                        topLevelAsbiepIds.stream().map(e -> ULong.valueOf(e.value())).collect(Collectors.toList())
                ));
            }
            return this;
        }

        public SelectBieListArguments setBasedTopLevelAsbiepIds(List<TopLevelAsbiepId> basedTopLevelAsbiepIds) {
            if (!basedTopLevelAsbiepIds.isEmpty()) {
                List<ULong> result = basedTopLevelAsbiepIds.stream()
                        .map(e -> ULong.valueOf(e.value())).collect(Collectors.toList());
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

        public SelectBieListArguments setExcludeTopLevelAsbiepIds(List<TopLevelAsbiepId> excludeTopLevelAsbiepIds) {
            if (!excludeTopLevelAsbiepIds.isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.notIn(
                        excludeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e.value())).collect(Collectors.toList())
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

        public SelectBieListArguments setOwnerLoginIdList(List<String> ownerLoginIdList) {
            if (!ownerLoginIdList.isEmpty()) {
                conditions.add(APP_USER.as("owner").LOGIN_ID.in(ownerLoginIdList));
            }
            return this;
        }

        public SelectBieListArguments setUpdaterLoginIdList(List<String> updaterLoginIdList) {
            if (!updaterLoginIdList.isEmpty()) {
                conditions.add(APP_USER.as("updater").LOGIN_ID.in(updaterLoginIdList));
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

        public SelectBieListArguments setSort(List<Sort> sorts) {
            for (Sort sort : sorts) {
                String sortActive = sort.field();
                SortDirection sortDirection = sort.direction();

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

        public SelectBieListArguments setReleaseIds(List<ReleaseId> releaseIds) {
            if (releaseIds != null && !releaseIds.isEmpty()) {
                releaseIds = releaseIds.stream().filter(e -> e.value().longValue() > 0L).collect(Collectors.toList());
            }

            if (releaseIds != null && !releaseIds.isEmpty()) {
                if (releaseIds.size() == 1) {
                    conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(ULong.valueOf(releaseIds.get(0).value())));
                } else {
                    conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.in(releaseIds.stream().map(e -> ULong.valueOf(e.value())).collect(Collectors.toList())));
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

        public SelectBieListArguments setTenantBusinessCtx(ScoreUser requester, List<TenantId> userTenantIds) {
            if (configService.isTenantEnabled(requester) && !requester.isAdministrator()) {
                conditions.add(BIZ_CTX.BIZ_CTX_ID.in(
                        dslContext.select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                                .from(TENANT_BUSINESS_CTX)
                                .where(TENANT_BUSINESS_CTX.TENANT_ID.in(
                                        userTenantIds.stream().map(e -> ULong.valueOf(e.value()))
                                                .collect(Collectors.toList())
                                ))));
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

        public PageResponse<BieList> fetch() {
            return selectBieList(this);
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
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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

    private PageResponse<BieList> selectBieList(SelectBieListArguments arguments) {
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

        return new PageResponse<BieList>(
                (offsetStep != null) ? offsetStep.fetchInto(BieList.class) : conditionStep.fetchInto(BieList.class),
                arguments.getOffset(), arguments.getNumberOfRows(), pageCount);
    }

}
