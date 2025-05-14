package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.BieQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.info_management.model.SummaryBie;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.*;
import static org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.gateway.http.common.model.ScoreRole.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Routines.levenshtein;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

public class JooqBieQueryRepository extends JooqBaseRepository implements BieQueryRepository {

    public JooqBieQueryRepository(DSLContext dslContext,
                                  ScoreUser requester,
                                  RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ResultAndCount<BieListEntryRecord> getBieList(
            BieListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new BieListQueryBuilder(filterCriteria);
        var where = queryBuilder.select().where(
                queryBuilder.conditions());
        int count = dslContext().fetchCount(where);
        List<BieListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class BieListQueryBuilder {

        private final BieListFilterCriteria filterCriteria;

        public BieListQueryBuilder(BieListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                    ASBIEP.ASBIEP_ID,
                    ASBIEP.GUID,

                    ASCCP_MANIFEST.DEN,
                    ASCCP.PROPERTY_TERM,
                    ASBIEP.DISPLAY_NAME,
                    TOP_LEVEL_ASBIEP.VERSION,
                    TOP_LEVEL_ASBIEP.STATUS,
                    ASBIEP.BIZ_TERM,
                    ASBIEP.REMARK,
                    TOP_LEVEL_ASBIEP.STATE,
                    TOP_LEVEL_ASBIEP.IS_DEPRECATED,
                    TOP_LEVEL_ASBIEP.DEPRECATED_REASON,
                    TOP_LEVEL_ASBIEP.DEPRECATED_REMARK,

                    ASBIEP.CREATION_TIMESTAMP,
                    TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,

                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID,
                    ASCCP_MANIFEST.as("source_asccp_manifest").DEN.as("source_den"),
                    ASBIEP.as("source_asbiep").DISPLAY_NAME.as("source_display_name"),
                    TOP_LEVEL_ASBIEP.as("source").RELEASE_ID.as("source_release_id"),
                    RELEASE.as("source_release").LIBRARY_ID.as("source_library_id"),
                    RELEASE.as("source_release").RELEASE_NUM.as("source_release_num"),
                    RELEASE.as("source_release").STATE.as("source_release_state"),
                    TOP_LEVEL_ASBIEP.SOURCE_ACTION,
                    TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP,

                    TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID,
                    ASCCP_MANIFEST.as("based_asccp_manifest").DEN.as("based_top_level_asbiep_den"),
                    ASBIEP.as("based_asbiep").DISPLAY_NAME.as("based_top_level_asbiep_display_name"),
                    TOP_LEVEL_ASBIEP.as("based").RELEASE_ID.as("based_top_level_asbiep_release_id"),
                    RELEASE.as("based_release").LIBRARY_ID.as("based_library_id"),
                    RELEASE.as("based_release").RELEASE_NUM.as("based_top_level_asbiep_release_num"),
                    RELEASE.as("based_release").STATE.as("based_top_level_asbiep_release_state"),
                    TOP_LEVEL_ASBIEP.as("based").LAST_UPDATE_TIMESTAMP));
            if (hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(ASCCP.PROPERTY_TERM), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().selectDistinct(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(TOP_LEVEL_ASBIEP)
                    .join(ASBIEP).on(and(
                            ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                            ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                    )
                    .join(ABIE).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                    .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(ownerTable()).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(ASBIEP.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY.eq(updaterTablePk()))
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

        List<Condition> conditions() {
            List<Condition> conditions = new ArrayList();

            if (filterCriteria.libraryId() != null) {
                conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));
            }

            if (filterCriteria.releaseIdList() != null && !filterCriteria.releaseIdList().isEmpty()) {
                if (filterCriteria.releaseIdList().size() == 1) {
                    conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(valueOf(filterCriteria.releaseIdList().iterator().next())));
                } else {
                    conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.in(valueOf(filterCriteria.releaseIdList())));
                }
            }

            String den = filterCriteria.den();
            if (StringUtils.hasLength(den)) {
                conditions.addAll(contains(den, ASCCP_MANIFEST.DEN, ASBIEP.DISPLAY_NAME));
            }

            if (StringUtils.hasLength(filterCriteria.propertyTerm())) {
                conditions.addAll(contains(filterCriteria.propertyTerm(), ASCCP.PROPERTY_TERM));
            }

            if (StringUtils.hasLength(filterCriteria.version())) {
                conditions.addAll(contains(filterCriteria.version(), TOP_LEVEL_ASBIEP.VERSION));
            }

            if (StringUtils.hasLength(filterCriteria.remark())) {
                conditions.addAll(contains(filterCriteria.remark(), ASBIEP.REMARK));
            }

            if (filterCriteria.businessContextNameList() != null && !filterCriteria.businessContextNameList().isEmpty()) {
                conditions.add(or(filterCriteria.businessContextNameList().stream()
                        .map(e -> and(contains(e, BIZ_CTX.NAME)))
                        .collect(Collectors.toList())));
            }

            if (filterCriteria.asccpManifestId() != null) {
                conditions.add(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(valueOf(filterCriteria.asccpManifestId())));
            }

            if (filterCriteria.excludePropertyTermList() != null && !filterCriteria.excludePropertyTermList().isEmpty()) {
                conditions.add(ASCCP.PROPERTY_TERM.notIn(filterCriteria.excludePropertyTermList()));
            }

            if (filterCriteria.topLevelAsbiepIdList() != null && !filterCriteria.topLevelAsbiepIdList().isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(filterCriteria.topLevelAsbiepIdList())));
            }

            if (filterCriteria.basedTopLevelAsbiepIdList() != null && !filterCriteria.basedTopLevelAsbiepIdList().isEmpty()) {
                List<ULong> result = filterCriteria.basedTopLevelAsbiepIdList().stream()
                        .map(e -> ULong.valueOf(e.value())).collect(Collectors.toList());
                List<ULong> allInheritedTopLevelAsbiepIds = new ArrayList<>();
                while (!result.isEmpty()) {
                    allInheritedTopLevelAsbiepIds.addAll(result);
                    result = dslContext().select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                            .from(TOP_LEVEL_ASBIEP)
                            .where(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.in(result))
                            .fetchInto(ULong.class);
                }

                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(allInheritedTopLevelAsbiepIds));
            }

            if (filterCriteria.excludeTopLevelAsbiepIdList() != null && !filterCriteria.excludeTopLevelAsbiepIdList().isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.notIn(valueOf(filterCriteria.excludeTopLevelAsbiepIdList())));
            }

            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(TOP_LEVEL_ASBIEP.STATE.in(
                        filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toList())));
            }

            if (filterCriteria.deprecated() != null) {
                conditions.add(TOP_LEVEL_ASBIEP.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }

            if (filterCriteria.access() != null) {
                switch (filterCriteria.access()) {
                    case CanEdit:
                        conditions.add(
                                and(
                                        TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                        TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(valueOf(requester().userId()))
                                )
                        );
                        break;

                    case CanView:
                        conditions.add(
                                or(
                                        TOP_LEVEL_ASBIEP.STATE.in(QA.name(), Production.name()),
                                        and(
                                                TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                                TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(valueOf(requester().userId()))
                                        )
                                )
                        );
                        break;
                }
            }

            if (filterCriteria.tenantEnabled() != null && filterCriteria.tenantEnabled()) {
                Collection<TenantId> userTenantIdList = filterCriteria.userTenantIdList();
                if (userTenantIdList == null) {
                    userTenantIdList = Collections.emptyList();
                }
                conditions.add(BIZ_CTX.BIZ_CTX_ID.in(dslContext().select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                        .from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.in(valueOf(userTenantIdList)))));
            }

            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }

            return conditions;
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
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
                        field = ownerTable().LOGIN_ID;
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

                if (sort.direction() == DESC) {
                    sortFields.add(field.desc());
                } else {
                    sortFields.add(field.asc());
                }
            }

            if (hasLength(filterCriteria.den())) {
                sortFields.add(field("score").desc());
            }

            return sortFields;
        }

        public List<BieListEntryRecord> fetch(
                SelectOrderByStep<?> conditionStep, PageRequest pageRequest) {
            var sortFields = sortFields(pageRequest);
            SelectFinalStep<? extends Record> finalStep;
            if (sortFields == null || sortFields.isEmpty()) {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep;
                }
            } else {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.orderBy(sortFields)
                            .limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep.orderBy(sortFields);
                }
            }
            return finalStep.fetch(mapper());
        }

        private RecordMapper<org.jooq.Record, BieListEntryRecord> mapper() {
            return record -> {
                TopLevelAsbiepId topLevelAsbiepId = new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                AsbiepId asbiepId = new AsbiepId(record.get(ASBIEP.ASBIEP_ID).toBigInteger());

                SourceTopLevelAsbiepRecord source = null;
                if (record.get(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID) != null) {
                    source = new SourceTopLevelAsbiepRecord(
                            new ReleaseSummaryRecord(
                                    new ReleaseId(record.get(TOP_LEVEL_ASBIEP.as("source").RELEASE_ID.as("source_release_id")).toBigInteger()),
                                    new LibraryId(record.get(RELEASE.as("source_release").LIBRARY_ID.as("source_library_id")).toBigInteger()),
                                    record.get(RELEASE.as("source_release").RELEASE_NUM.as("source_release_num")),
                                    ReleaseState.valueOf(record.get(RELEASE.as("source_release").STATE.as("source_release_state")))
                            ),
                            new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID).toBigInteger()),
                            record.get(ASCCP_MANIFEST.as("source_asccp_manifest").DEN.as("source_den")),
                            record.get(ASBIEP.as("source_asbiep").DISPLAY_NAME.as("source_display_name")),
                            record.get(TOP_LEVEL_ASBIEP.SOURCE_ACTION),
                            toDate(record.get(TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP))
                    );
                }

                SourceTopLevelAsbiepRecord based = null;
                if (record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID) != null) {
                    based = new SourceTopLevelAsbiepRecord(
                            new ReleaseSummaryRecord(
                                    new ReleaseId(record.get(TOP_LEVEL_ASBIEP.as("based").RELEASE_ID.as("based_top_level_asbiep_release_id")).toBigInteger()),
                                    new LibraryId(record.get(RELEASE.as("based_release").LIBRARY_ID.as("based_library_id")).toBigInteger()),
                                    record.get(RELEASE.as("based_release").RELEASE_NUM.as("based_top_level_asbiep_release_num")),
                                    ReleaseState.valueOf(record.get(RELEASE.as("based_release").STATE.as("based_top_level_asbiep_release_state")))
                            ),
                            new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID).toBigInteger()),
                            record.get(ASCCP_MANIFEST.as("based_asccp_manifest").DEN.as("based_top_level_asbiep_den")),
                            record.get(ASBIEP.as("based_asbiep").DISPLAY_NAME.as("based_top_level_asbiep_display_name")),
                            "Inherit",
                            toDate(record.get(TOP_LEVEL_ASBIEP.as("based").LAST_UPDATE_TIMESTAMP))
                    );
                }

                BieState state = BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                var bizCtxQuery = repositoryFactory().businessContextQueryRepository(requester());

                return new BieListEntryRecord(
                        new LibrarySummaryRecord(
                                new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                                record.get(LIBRARY.NAME.as("library_name")),
                                record.get(LIBRARY.STATE.as("library_state")),
                                (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                        ),
                        new ReleaseSummaryRecord(
                                new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                                new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                                record.get(RELEASE.RELEASE_NUM),
                                ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                        ),

                        topLevelAsbiepId,
                        asbiepId,
                        new Guid(record.get(ASBIEP.GUID)),

                        record.get(ASCCP_MANIFEST.DEN),
                        record.get(ASCCP.PROPERTY_TERM),
                        record.get(ASBIEP.DISPLAY_NAME),
                        record.get(TOP_LEVEL_ASBIEP.VERSION),
                        record.get(TOP_LEVEL_ASBIEP.STATUS),
                        record.get(ASBIEP.BIZ_TERM),
                        record.get(ASBIEP.REMARK),
                        bizCtxQuery.getBusinessContextSummaryList(topLevelAsbiepId),
                        state,
                        AccessPrivilege.toAccessPrivilege(requester(), owner.userId(), state),

                        (byte) 1 == record.get(TOP_LEVEL_ASBIEP.IS_DEPRECATED),
                        record.get(TOP_LEVEL_ASBIEP.DEPRECATED_REASON),
                        record.get(TOP_LEVEL_ASBIEP.DEPRECATED_REMARK),

                        source,
                        based,

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASBIEP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP))
                        )
                );
            };
        }

    }

    @Override
    public ResultAndCount<BieListEntryRecord> getBieList(
            BieListInBiePackageFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new BieListInBiePackageQueryBuilder(filterCriteria);
        var where = queryBuilder.select().where(
                queryBuilder.conditions());
        int count = dslContext().fetchCount(where);
        List<BieListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class BieListInBiePackageQueryBuilder {

        private final BieListInBiePackageFilterCriteria filterCriteria;

        public BieListInBiePackageQueryBuilder(BieListInBiePackageFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                    ASBIEP.ASBIEP_ID,
                    ASBIEP.GUID,

                    ASCCP_MANIFEST.DEN,
                    ASCCP.PROPERTY_TERM,
                    ASBIEP.DISPLAY_NAME,
                    TOP_LEVEL_ASBIEP.VERSION,
                    TOP_LEVEL_ASBIEP.STATUS,
                    ASBIEP.BIZ_TERM,
                    ASBIEP.REMARK,
                    TOP_LEVEL_ASBIEP.STATE,
                    TOP_LEVEL_ASBIEP.IS_DEPRECATED,
                    TOP_LEVEL_ASBIEP.DEPRECATED_REASON,
                    TOP_LEVEL_ASBIEP.DEPRECATED_REMARK,

                    ASBIEP.CREATION_TIMESTAMP,
                    TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,

                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID,
                    ASCCP_MANIFEST.as("source_asccp_manifest").DEN.as("source_den"),
                    ASBIEP.as("source_asbiep").DISPLAY_NAME.as("source_display_name"),
                    TOP_LEVEL_ASBIEP.as("source").RELEASE_ID.as("source_release_id"),
                    RELEASE.as("source_release").LIBRARY_ID.as("source_library_id"),
                    RELEASE.as("source_release").RELEASE_NUM.as("source_release_num"),
                    RELEASE.as("source_release").STATE.as("source_release_state"),
                    TOP_LEVEL_ASBIEP.SOURCE_ACTION,
                    TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP,

                    TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID,
                    ASCCP_MANIFEST.as("based_asccp_manifest").DEN.as("based_top_level_asbiep_den"),
                    ASBIEP.as("based_asbiep").DISPLAY_NAME.as("based_top_level_asbiep_display_name"),
                    TOP_LEVEL_ASBIEP.as("based").RELEASE_ID.as("based_top_level_asbiep_release_id"),
                    RELEASE.as("based_release").LIBRARY_ID.as("based_library_id"),
                    RELEASE.as("based_release").RELEASE_NUM.as("based_top_level_asbiep_release_num"),
                    RELEASE.as("based_release").STATE.as("based_top_level_asbiep_release_state"),
                    TOP_LEVEL_ASBIEP.as("based").LAST_UPDATE_TIMESTAMP));
            if (hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(ASCCP.PROPERTY_TERM), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().selectDistinct(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                    .join(TOP_LEVEL_ASBIEP).on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ASBIEP).on(and(
                            ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                            ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                    )
                    .join(ABIE).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                    .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(ownerTable()).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(ASBIEP.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY.eq(updaterTablePk()))
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

        List<Condition> conditions() {
            List<Condition> conditions = new ArrayList();

            if (filterCriteria.biePackageId() != null) {
                conditions.add(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(valueOf(filterCriteria.biePackageId())));
            }

            String den = filterCriteria.den();
            if (StringUtils.hasLength(den)) {
                conditions.addAll(contains(den, ASCCP_MANIFEST.DEN, ASBIEP.DISPLAY_NAME));
            }

            if (StringUtils.hasLength(filterCriteria.version())) {
                conditions.addAll(contains(filterCriteria.version(), TOP_LEVEL_ASBIEP.VERSION));
            }

            if (StringUtils.hasLength(filterCriteria.remark())) {
                conditions.addAll(contains(filterCriteria.remark(), ASBIEP.REMARK));
            }

            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }

            return conditions;
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
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
                        field = ownerTable().LOGIN_ID;
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

                if (sort.direction() == DESC) {
                    sortFields.add(field.desc());
                } else {
                    sortFields.add(field.asc());
                }
            }

            if (hasLength(filterCriteria.den())) {
                sortFields.add(field("score").desc());
            }

            return sortFields;
        }

        public List<BieListEntryRecord> fetch(
                SelectOrderByStep<?> conditionStep, PageRequest pageRequest) {
            var sortFields = sortFields(pageRequest);
            SelectFinalStep<? extends Record> finalStep;
            if (sortFields == null || sortFields.isEmpty()) {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep;
                }
            } else {
                if (pageRequest.isPagination()) {
                    finalStep = conditionStep.orderBy(sortFields)
                            .limit(pageRequest.pageOffset(), pageRequest.pageSize());
                } else {
                    finalStep = conditionStep.orderBy(sortFields);
                }
            }
            return finalStep.fetch(mapper());
        }

        private RecordMapper<org.jooq.Record, BieListEntryRecord> mapper() {
            return record -> {
                TopLevelAsbiepId topLevelAsbiepId = new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                AsbiepId asbiepId = new AsbiepId(record.get(ASBIEP.ASBIEP_ID).toBigInteger());

                SourceTopLevelAsbiepRecord source = null;
                if (record.get(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID) != null) {
                    source = new SourceTopLevelAsbiepRecord(
                            new ReleaseSummaryRecord(
                                    new ReleaseId(record.get(TOP_LEVEL_ASBIEP.as("source").RELEASE_ID.as("source_release_id")).toBigInteger()),
                                    new LibraryId(record.get(RELEASE.as("source_release").LIBRARY_ID.as("source_library_id")).toBigInteger()),
                                    record.get(RELEASE.as("source_release").RELEASE_NUM.as("source_release_num")),
                                    ReleaseState.valueOf(record.get(RELEASE.as("source_release").STATE.as("source_release_state")))
                            ),
                            new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.SOURCE_TOP_LEVEL_ASBIEP_ID).toBigInteger()),
                            record.get(ASCCP_MANIFEST.as("source_asccp_manifest").DEN.as("source_den")),
                            record.get(ASBIEP.as("source_asbiep").DISPLAY_NAME.as("source_display_name")),
                            record.get(TOP_LEVEL_ASBIEP.SOURCE_ACTION),
                            toDate(record.get(TOP_LEVEL_ASBIEP.SOURCE_TIMESTAMP))
                    );
                }

                SourceTopLevelAsbiepRecord based = null;
                if (record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID) != null) {
                    based = new SourceTopLevelAsbiepRecord(
                            new ReleaseSummaryRecord(
                                    new ReleaseId(record.get(TOP_LEVEL_ASBIEP.as("based").RELEASE_ID.as("based_top_level_asbiep_release_id")).toBigInteger()),
                                    new LibraryId(record.get(RELEASE.as("based_release").LIBRARY_ID.as("based_library_id")).toBigInteger()),
                                    record.get(RELEASE.as("based_release").RELEASE_NUM.as("based_top_level_asbiep_release_num")),
                                    ReleaseState.valueOf(record.get(RELEASE.as("based_release").STATE.as("based_top_level_asbiep_release_state")))
                            ),
                            new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID).toBigInteger()),
                            record.get(ASCCP_MANIFEST.as("based_asccp_manifest").DEN.as("based_top_level_asbiep_den")),
                            record.get(ASBIEP.as("based_asbiep").DISPLAY_NAME.as("based_top_level_asbiep_display_name")),
                            "Inherit",
                            toDate(record.get(TOP_LEVEL_ASBIEP.as("based").LAST_UPDATE_TIMESTAMP))
                    );
                }

                BieState state = BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                var bizCtxQuery = repositoryFactory().businessContextQueryRepository(requester());

                return new BieListEntryRecord(
                        new LibrarySummaryRecord(
                                new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                                record.get(LIBRARY.NAME.as("library_name")),
                                record.get(LIBRARY.STATE.as("library_state")),
                                (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                        ),
                        new ReleaseSummaryRecord(
                                new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                                new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                                record.get(RELEASE.RELEASE_NUM),
                                ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                        ),

                        topLevelAsbiepId,
                        asbiepId,
                        new Guid(record.get(ASBIEP.GUID)),

                        record.get(ASCCP_MANIFEST.DEN),
                        record.get(ASCCP.PROPERTY_TERM),
                        record.get(ASBIEP.DISPLAY_NAME),
                        record.get(TOP_LEVEL_ASBIEP.VERSION),
                        record.get(TOP_LEVEL_ASBIEP.STATUS),
                        record.get(ASBIEP.BIZ_TERM),
                        record.get(ASBIEP.REMARK),
                        bizCtxQuery.getBusinessContextSummaryList(topLevelAsbiepId),
                        state,
                        AccessPrivilege.toAccessPrivilege(requester(), owner.userId(), state),

                        (byte) 1 == record.get(TOP_LEVEL_ASBIEP.IS_DEPRECATED),
                        record.get(TOP_LEVEL_ASBIEP.DEPRECATED_REASON),
                        record.get(TOP_LEVEL_ASBIEP.DEPRECATED_REMARK),

                        source,
                        based,

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASBIEP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP))
                        )
                );
            };
        }

    }

    private SelectJoinStep selectAbie() {
        return dslContext().select(ABIE.ABIE_ID,
                        ABIE.GUID,
                        ABIE.BASED_ACC_MANIFEST_ID,
                        ABIE.PATH,
                        ABIE.HASH_PATH,
                        ABIE.DEFINITION,
                        ABIE.REMARK,
                        ABIE.BIZ_TERM,
                        ABIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ABIE);
    }

    private RecordMapper<org.jooq.Record, Abie> mapperAbie() {
        return record -> {
            Abie abie = new Abie();
            abie.setAbieId(new AbieId(record.get(ABIE.ABIE_ID).toBigInteger()));
            abie.setGuid(record.get(ABIE.GUID));
            abie.setBasedAccManifestId(new AccManifestId(record.get(ABIE.BASED_ACC_MANIFEST_ID).toBigInteger()));
            abie.setPath(record.get(ABIE.PATH));
            abie.setHashPath(record.get(ABIE.HASH_PATH));
            abie.setDefinition(record.get(ABIE.DEFINITION));
            abie.setRemark(record.get(ABIE.REMARK));
            abie.setBizTerm(record.get(ABIE.BIZ_TERM));
            abie.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
            return abie;
        };
    }

    private SelectJoinStep selectAsbie() {
        return dslContext().select(ASBIE.ASBIE_ID,
                        ASBIE.GUID,
                        ASBIE.BASED_ASCC_MANIFEST_ID,
                        ASBIE.PATH,
                        ASBIE.HASH_PATH,
                        ASBIE.FROM_ABIE_ID,
                        ASBIE.TO_ASBIEP_ID,
                        ASBIE.CARDINALITY_MIN,
                        ASBIE.CARDINALITY_MAX,
                        ASBIE.IS_NILLABLE,
                        ASBIE.IS_DEPRECATED,
                        ASBIE.IS_USED,
                        ASBIE.DEFINITION,
                        ASBIE.REMARK,
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ASBIE);
    }

    private RecordMapper<org.jooq.Record, Asbie> mapperAsbie() {
        return record -> {
            Asbie asbie = new Asbie();
            asbie.setAsbieId(new AsbieId(record.get(ASBIE.ASBIE_ID).toBigInteger()));
            asbie.setGuid(record.get(ASBIE.GUID));
            asbie.setBasedAsccManifestId(new AsccManifestId(record.get(ASBIE.BASED_ASCC_MANIFEST_ID).toBigInteger()));
            asbie.setPath(record.get(ASBIE.PATH));
            asbie.setHashPath(record.get(ASBIE.HASH_PATH));
            asbie.setFromAbieId(new AbieId(record.get(ASBIE.FROM_ABIE_ID).toBigInteger()));
            asbie.setToAsbiepId(new AsbiepId(record.get(ASBIE.TO_ASBIEP_ID).toBigInteger()));
            asbie.setCardinalityMin(record.get(ASBIE.CARDINALITY_MIN));
            asbie.setCardinalityMax(record.get(ASBIE.CARDINALITY_MAX));
            asbie.setNillable((byte) 1 == record.get(ASBIE.IS_NILLABLE));
            asbie.setDeprecated((byte) 1 == record.get(ASBIE.IS_DEPRECATED));
            asbie.setUsed((byte) 1 == record.get(ASBIE.IS_USED));
            asbie.setDefinition(record.get(ASBIE.DEFINITION));
            asbie.setRemark(record.get(ASBIE.REMARK));
            asbie.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
            return asbie;
        };
    }

    private SelectJoinStep selectBbie() {
        return dslContext().select(BBIE.BBIE_ID,
                        BBIE.GUID,
                        BBIE.BASED_BCC_MANIFEST_ID,
                        BBIE.PATH,
                        BBIE.HASH_PATH,
                        BBIE.FROM_ABIE_ID,
                        BBIE.TO_BBIEP_ID,
                        BBIE.XBT_MANIFEST_ID,
                        BBIE.CODE_LIST_MANIFEST_ID,
                        BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE.CARDINALITY_MIN,
                        BBIE.CARDINALITY_MAX,
                        BBIE.DEFAULT_VALUE,
                        BBIE.FIXED_VALUE,
                        BBIE.IS_NILLABLE,
                        BBIE.IS_NULL,
                        BBIE.IS_DEPRECATED,
                        BBIE.IS_USED,
                        BBIE.DEFINITION,
                        BBIE.REMARK,
                        BBIE.EXAMPLE,
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(BBIE);
    }

    private RecordMapper<org.jooq.Record, Bbie> mapperBbie() {
        return record -> {
            Bbie bbie = new Bbie();
            bbie.setBbieId(new BbieId(record.get(BBIE.BBIE_ID).toBigInteger()));
            bbie.setGuid(record.get(BBIE.GUID));
            bbie.setBasedBccManifestId(new BccManifestId(record.get(BBIE.BASED_BCC_MANIFEST_ID).toBigInteger()));
            bbie.setPath(record.get(BBIE.PATH));
            bbie.setHashPath(record.get(BBIE.HASH_PATH));
            bbie.setFromAbieId(new AbieId(record.get(BBIE.FROM_ABIE_ID).toBigInteger()));
            bbie.setToBbiepId(new BbiepId(record.get(BBIE.TO_BBIEP_ID).toBigInteger()));
            bbie.setXbtManifestId((record.get(BBIE.XBT_MANIFEST_ID) != null) ?
                    new XbtManifestId(record.get(BBIE.XBT_MANIFEST_ID).toBigInteger()) : null);
            bbie.setCodeListManifestId((record.get(BBIE.CODE_LIST_MANIFEST_ID) != null) ?
                    new CodeListManifestId(record.get(BBIE.CODE_LIST_MANIFEST_ID).toBigInteger()) : null);
            bbie.setAgencyIdListManifestId((record.get(BBIE.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                    new AgencyIdListManifestId(record.get(BBIE.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null);
            bbie.setCardinalityMin(record.get(BBIE.CARDINALITY_MIN));
            bbie.setCardinalityMax(record.get(BBIE.CARDINALITY_MAX));
            bbie.setDefaultValue(record.get(BBIE.DEFAULT_VALUE));
            bbie.setFixedValue(record.get(BBIE.FIXED_VALUE));
            bbie.setNillable((byte) 1 == record.get(BBIE.IS_NILLABLE));
            bbie.setDeprecated((byte) 1 == record.get(BBIE.IS_DEPRECATED));
            bbie.setUsed((byte) 1 == record.get(BBIE.IS_USED));
            bbie.setDefinition(record.get(BBIE.DEFINITION));
            bbie.setRemark(record.get(BBIE.REMARK));
            bbie.setExample(record.get(BBIE.EXAMPLE));
            bbie.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
            return bbie;
        };
    }

    private SelectJoinStep selectAsbiep() {
        return dslContext().select(ASBIEP.ASBIEP_ID,
                        ASBIEP.GUID,
                        ASBIEP.BASED_ASCCP_MANIFEST_ID,
                        ASBIEP.PATH,
                        ASBIEP.HASH_PATH,
                        ASBIEP.ROLE_OF_ABIE_ID,
                        ASBIEP.DEFINITION,
                        ASBIEP.REMARK,
                        ASBIEP.BIZ_TERM,
                        ASBIEP.DISPLAY_NAME,
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ASBIEP);
    }

    private RecordMapper<org.jooq.Record, Asbiep> mapperAsbiep() {
        return record -> {
            Asbiep asbiep = new Asbiep();
            asbiep.setAsbiepId(new AsbiepId(record.get(ASBIEP.ASBIEP_ID).toBigInteger()));
            asbiep.setGuid(record.get(ASBIEP.GUID));
            asbiep.setBasedAsccpManifestId(new AsccpManifestId(record.get(ASBIEP.BASED_ASCCP_MANIFEST_ID).toBigInteger()));
            asbiep.setPath(record.get(ASBIEP.PATH));
            asbiep.setHashPath(record.get(ASBIEP.HASH_PATH));
            asbiep.setRoleOfAbieId(new AbieId(record.get(ASBIEP.ROLE_OF_ABIE_ID).toBigInteger()));
            asbiep.setDefinition(record.get(ASBIEP.DEFINITION));
            asbiep.setRemark(record.get(ASBIEP.REMARK));
            asbiep.setBizTerm(record.get(ASBIEP.BIZ_TERM));
            asbiep.setDisplayName(record.get(ASBIEP.DISPLAY_NAME));
            asbiep.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
            return asbiep;
        };
    }

    private SelectJoinStep selectBbiep() {
        return dslContext().select(BBIEP.BBIEP_ID,
                        BBIEP.GUID,
                        BBIEP.BASED_BCCP_MANIFEST_ID,
                        BBIEP.PATH,
                        BBIEP.HASH_PATH,
                        BBIEP.DEFINITION,
                        BBIEP.REMARK,
                        BBIEP.BIZ_TERM,
                        BBIEP.DISPLAY_NAME,
                        BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(BBIEP);
    }

    private RecordMapper<org.jooq.Record, Bbiep> mapperBbiep() {
        return record -> {
            Bbiep bbiep = new Bbiep();
            bbiep.setBbiepId(new BbiepId(record.get(BBIEP.BBIEP_ID).toBigInteger()));
            bbiep.setGuid(record.get(BBIEP.GUID));
            bbiep.setBasedBccpManifestId(new BccpManifestId(record.get(BBIEP.BASED_BCCP_MANIFEST_ID).toBigInteger()));
            bbiep.setPath(record.get(BBIEP.PATH));
            bbiep.setHashPath(record.get(BBIEP.HASH_PATH));
            bbiep.setDefinition(record.get(BBIEP.DEFINITION));
            bbiep.setRemark(record.get(BBIEP.REMARK));
            bbiep.setBizTerm(record.get(BBIEP.BIZ_TERM));
            bbiep.setDisplayName(record.get(BBIEP.DISPLAY_NAME));
            bbiep.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
            return bbiep;
        };
    }

    private SelectJoinStep selectBbieSc() {
        return dslContext().select(BBIE_SC.BBIE_SC_ID,
                        BBIE_SC.GUID,
                        BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                        BBIE_SC.PATH,
                        BBIE_SC.HASH_PATH,
                        BBIE_SC.BBIE_ID,
                        BBIE_SC.XBT_MANIFEST_ID,
                        BBIE_SC.CODE_LIST_MANIFEST_ID,
                        BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.DEFAULT_VALUE,
                        BBIE_SC.FIXED_VALUE,
                        BBIE_SC.IS_DEPRECATED,
                        BBIE_SC.IS_USED,
                        BBIE_SC.DEFINITION,
                        BBIE_SC.BIZ_TERM,
                        BBIE_SC.REMARK,
                        BBIE_SC.DISPLAY_NAME,
                        BBIE_SC.EXAMPLE,
                        BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(BBIE_SC);
    }

    private RecordMapper<Record, BbieSc> mapperBbieSc() {
        return record -> {
            BbieSc bbieSc = new BbieSc();
            bbieSc.setBbieScId(new BbieScId(record.get(BBIE_SC.BBIE_SC_ID).toBigInteger()));
            bbieSc.setGuid(record.get(BBIE_SC.GUID));
            bbieSc.setBasedDtScManifestId(new DtScManifestId(record.get(BBIE_SC.BASED_DT_SC_MANIFEST_ID).toBigInteger()));
            bbieSc.setBbieId(new BbieId(record.get(BBIE_SC.BBIE_ID).toBigInteger()));
            bbieSc.setPath(record.get(BBIE_SC.PATH));
            bbieSc.setHashPath(record.get(BBIE_SC.HASH_PATH));
            bbieSc.setXbtManifestId((record.get(BBIE_SC.XBT_MANIFEST_ID) != null) ?
                    new XbtManifestId(record.get(BBIE_SC.XBT_MANIFEST_ID).toBigInteger()) : null);
            bbieSc.setCodeListManifestId((record.get(BBIE_SC.CODE_LIST_MANIFEST_ID) != null) ?
                    new CodeListManifestId(record.get(BBIE_SC.CODE_LIST_MANIFEST_ID).toBigInteger()) : null);
            bbieSc.setAgencyIdListManifestId((record.get(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                    new AgencyIdListManifestId(record.get(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null);
            bbieSc.setCardinalityMin(record.get(BBIE_SC.CARDINALITY_MIN));
            bbieSc.setCardinalityMax(record.get(BBIE_SC.CARDINALITY_MAX));
            bbieSc.setDefaultValue(record.get(BBIE_SC.DEFAULT_VALUE));
            bbieSc.setFixedValue(record.get(BBIE_SC.FIXED_VALUE));
            bbieSc.setDeprecated((byte) 1 == record.get(BBIE_SC.IS_DEPRECATED));
            bbieSc.setUsed((byte) 1 == record.get(BBIE_SC.IS_USED));
            bbieSc.setDefinition(record.get(BBIE_SC.DEFINITION));
            bbieSc.setBizTerm(record.get(BBIE_SC.BIZ_TERM));
            bbieSc.setRemark(record.get(BBIE_SC.REMARK));
            bbieSc.setDisplayName(record.get(BBIE_SC.DISPLAY_NAME));
            bbieSc.setExample(record.get(BBIE_SC.EXAMPLE));
            bbieSc.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
            return bbieSc;
        };
    }

    @Override
    public List<SummaryBie> getSummaryBieList(
            LibraryId libraryId, ReleaseId releaseId, boolean tenantEnabled, List<TenantId> userTenantIds) {

        SelectOnConditionStep step = dslContext().selectDistinct(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                        APP_USER.LOGIN_ID.as("ownerUsername"),
                        ASCCP.PROPERTY_TERM)
                .from(TOP_LEVEL_ASBIEP)
                .join(APP_USER).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(ASBIEP).on(
                        TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(RELEASE)
                .on(and(
                        TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID),
                        ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID)
                ))
                .join(LIBRARY)
                .on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID));

        if (tenantEnabled) {
            step.join(BIZ_CTX_ASSIGNMENT)
                    .on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID)).join(BIZ_CTX)
                    .on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID)).leftJoin(TENANT_BUSINESS_CTX)
                    .on(BIZ_CTX.BIZ_CTX_ID.eq(TENANT_BUSINESS_CTX.BIZ_CTX_ID));
        }

        SelectConditionStep cond;
        List<Condition> conditions = new ArrayList();
        conditions.add(LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())));
        if (releaseId != null && releaseId.value().longValue() > 0) {
            conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(ULong.valueOf(releaseId.value())));
        } else {
            conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.isNotNull());
        }

        if (tenantEnabled) {
            conditions.add(BIZ_CTX.BIZ_CTX_ID.in(dslContext().select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                    .from(TENANT_BUSINESS_CTX)
                    .where(TENANT_BUSINESS_CTX.TENANT_ID.in(valueOf(userTenantIds)))));
        }
        cond = step.where(conditions);

        return cond.fetchInto(SummaryBie.class);
    }
    
    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public BieSet getBieSet(TopLevelAsbiepId topLevelAsbiepId, boolean used) {
        var topLevelAsbiepQuery = repositoryFactory().topLevelAsbiepQueryRepository(requester());
        TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

        if (topLevelAsbiep.state() == BieState.WIP) {
            if (!topLevelAsbiep.owner().userId().equals(requester().userId())) {
                throw new ScoreDataAccessException();
            }
        }

        BieSet bieSet = new BieSet();
        bieSet.setTopLevelAsbiep(topLevelAsbiep);

        if (topLevelAsbiep.state() != BieState.Initiating) {
            List<Condition> conditions;

            bieSet.setAbieList(selectAbie()
                    .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                    .fetch(mapperAbie())
            );

            conditions = new ArrayList();
            conditions.add(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)));
            if (used) {
                conditions.add(ASBIE.IS_USED.eq((byte) 1));
            }
            bieSet.setAsbieList(selectAsbie()
                    .where(conditions)
                    .fetch(mapperAsbie())
            );

            conditions = new ArrayList();
            conditions.add(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)));
            if (used) {
                conditions.add(BBIE.IS_USED.eq((byte) 1));
            }
            bieSet.setBbieList(selectBbie()
                    .where(conditions)
                    .fetch(mapperBbie())
            );

            bieSet.setAsbiepList(selectAsbiep()
                    .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                    .fetch(mapperAsbiep())
            );

            bieSet.setBbiepList(selectBbiep()
                    .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                    .fetch(mapperBbiep())
            );

            conditions = new ArrayList();
            conditions.add(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)));
            if (used) {
                conditions.add(BBIE_SC.IS_USED.eq((byte) 1));
            }
            bieSet.setBbieScList(selectBbieSc()
                    .where(conditions)
                    .fetch(mapperBbieSc())
            );
        }

        return bieSet;
    }

}
