package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.SourceTopLevelAsbiepRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.*;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.gateway.http.common.model.AccessPrivilege.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Routines.levenshtein;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public class JooqBiePackageQueryRepository extends JooqBaseRepository implements BiePackageQueryRepository {

    public JooqBiePackageQueryRepository(DSLContext dslContext,
                                         ScoreUser requester,
                                         RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BiePackageSummaryRecord getBiePackageSummary(BiePackageId biePackageId) {
        if (biePackageId == null) {
            return null;
        }

        var queryBuilder = new GetBiePackageSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                .groupBy(BIE_PACKAGE.BIE_PACKAGE_ID)
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<BiePackageSummaryRecord> getBiePackageSummaryList(Collection<BiePackageId> biePackageIds) {

        var queryBuilder = new GetBiePackageSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.in(valueOf(biePackageIds)))
                .groupBy(BIE_PACKAGE.BIE_PACKAGE_ID)
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BiePackageSummaryRecord> getBiePackagesReferencingAsPrevious(Collection<BiePackageId> biePackageIds) {
        if (biePackageIds == null || biePackageIds.isEmpty()) {
            return Collections.emptyList();
        }

        var ids = valueOf(biePackageIds);
        var queryBuilder = new GetBiePackageSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        BIE_PACKAGE.PREV_BIE_PACKAGE_ID.in(ids),
                        BIE_PACKAGE.BIE_PACKAGE_ID.notIn(ids)
                ))
                .groupBy(BIE_PACKAGE.BIE_PACKAGE_ID)
                .fetch(queryBuilder.mapper());
    }

    private class GetBiePackageSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(concat(fields(
                            BIE_PACKAGE.BIE_PACKAGE_ID,
                            BIE_PACKAGE.GUID,
                            BIE_PACKAGE.LIBRARY_ID,
                            BIE_PACKAGE.NAME,
                            BIE_PACKAGE.VERSION_ID,
                            BIE_PACKAGE.VERSION_NAME,
                            BIE_PACKAGE.DESCRIPTION,
                            BIE_PACKAGE.REVISION_REASON,
                            groupConcatDistinct(RELEASE.RELEASE_ID).as("release_id_list"),
                            groupConcatDistinct(RELEASE.RELEASE_NUM).as("release_num_list"),
                            BIE_PACKAGE.STATE,
                            BIE_PACKAGE.PREV_BIE_PACKAGE_ID
                    ), ownerFields()))
                    .from(BIE_PACKAGE)
                    .join(libraryTable()).on(libraryTablePk().eq(BIE_PACKAGE.LIBRARY_ID))
                    .join(ownerTable()).on(BIE_PACKAGE.OWNER_USER_ID.eq(ownerTablePk()))
                    .leftJoin(BIE_PACKAGE_TOP_LEVEL_ASBIEP).on(BIE_PACKAGE.BIE_PACKAGE_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID))
                    .leftJoin(TOP_LEVEL_ASBIEP).on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .leftJoin(RELEASE).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID));
        }

        RecordMapper<Record, BiePackageSummaryRecord> mapper() {
            return record -> {

                String releaseIdListStr = record.get(field("release_id_list"), String.class);
                List<String> releaseIdList = hasLength(releaseIdListStr) ? Arrays.asList(releaseIdListStr.split(",")) : Collections.emptyList();

                String releaseNumListStr = record.get(field("release_num_list"), String.class);
                List<String> releaseNumList = hasLength(releaseNumListStr) ? Arrays.asList(releaseNumListStr.split(",")) : Collections.emptyList();

                List<ReleaseSummaryRecord> releases = new ArrayList<>();
                if (!releaseIdList.isEmpty() && releaseIdList.size() == releaseNumList.size()) {
                    releases = repositoryFactory().releaseQueryRepository(requester()).getReleaseSummaryList(
                            releaseIdList.stream().map(e -> ReleaseId.from(e)).collect(Collectors.toSet())
                    );
                }

                BieState state = BieState.valueOf(record.get(BIE_PACKAGE.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                return new BiePackageSummaryRecord(
                        new BiePackageId(record.get(BIE_PACKAGE.BIE_PACKAGE_ID).toBigInteger()),
                        new Guid(record.get(BIE_PACKAGE.GUID)),
                        new LibraryId(record.get(BIE_PACKAGE.LIBRARY_ID).toBigInteger()),
                        record.get(BIE_PACKAGE.NAME),
                        record.get(BIE_PACKAGE.VERSION_ID),
                        record.get(BIE_PACKAGE.VERSION_NAME),
                        record.get(BIE_PACKAGE.DESCRIPTION),
                        record.get(BIE_PACKAGE.REVISION_REASON),
                        releases,
                        state,
                        owner,
                        (record.get(BIE_PACKAGE.PREV_BIE_PACKAGE_ID) != null) ?
                                new BiePackageId(record.get(BIE_PACKAGE.PREV_BIE_PACKAGE_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public ResultAndCount<BiePackageListEntryRecord> getBiePackageList(
            BiePackageListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetBiePackageListQueryBuilder();
        var where = queryBuilder.select().where(
                        queryBuilder.conditions(filterCriteria))
                .groupBy(BIE_PACKAGE.BIE_PACKAGE_ID);
        int count = dslContext().fetchCount(where);
        List<BiePackageListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetBiePackageListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(concat(fields(
                            BIE_PACKAGE.BIE_PACKAGE_ID,
                            BIE_PACKAGE.GUID,
                            BIE_PACKAGE.NAME,
                            BIE_PACKAGE.LIBRARY_ID,
                            BIE_PACKAGE.VERSION_ID,
                            BIE_PACKAGE.VERSION_NAME,
                            BIE_PACKAGE.DESCRIPTION,
                            groupConcatDistinct(RELEASE.RELEASE_ID).as("release_id_list"),
                            groupConcatDistinct(RELEASE.RELEASE_NUM).as("release_num_list"),
                            BIE_PACKAGE.STATE,
                            BIE_PACKAGE.CREATION_TIMESTAMP,
                            BIE_PACKAGE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BIE_PACKAGE)
                    .join(libraryTable()).on(libraryTablePk().eq(BIE_PACKAGE.LIBRARY_ID))
                    .join(ownerTable()).on(BIE_PACKAGE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(BIE_PACKAGE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BIE_PACKAGE.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(BIE_PACKAGE_TOP_LEVEL_ASBIEP).on(BIE_PACKAGE.BIE_PACKAGE_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID))
                    // -- only join the BIE-package -> top-level rows that are NOT superseded
                    .andNotExists(selectOne()
                            .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP.as("bptla2"))
                            .where(and(
                                    BIE_PACKAGE_TOP_LEVEL_ASBIEP.as("bptla2").PREV_TOP_LEVEL_ASBIEP_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                                    BIE_PACKAGE_TOP_LEVEL_ASBIEP.as("bptla2").BIE_PACKAGE_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID)
                            )))
                    .leftJoin(TOP_LEVEL_ASBIEP).on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .leftJoin(RELEASE).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                    .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .leftJoin(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                    .leftJoin(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID));
        }

        public List<Condition> conditions(BiePackageListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            if (filterCriteria.libraryId() != null) {
                conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));
            }
            if (hasLength(filterCriteria.name())) {
                conditions.addAll(contains(filterCriteria.name(), BIE_PACKAGE.NAME));
            }
            if (hasLength(filterCriteria.versionId())) {
                conditions.addAll(contains(filterCriteria.versionId(), BIE_PACKAGE.VERSION_ID));
            }
            if (hasLength(filterCriteria.versionName())) {
                conditions.addAll(contains(filterCriteria.versionName(), BIE_PACKAGE.VERSION_NAME));
            }
            if (hasLength(filterCriteria.description())) {
                conditions.addAll(contains(filterCriteria.description(), BIE_PACKAGE.DESCRIPTION));
            }
            if (hasLength(filterCriteria.den())) {
                conditions.addAll(contains(filterCriteria.den(), ASCCP_MANIFEST.DEN));
            }
            if (hasLength(filterCriteria.businessTerm())) {
                conditions.addAll(contains(filterCriteria.businessTerm(), BIZ_CTX.NAME));
            }
            if (hasLength(filterCriteria.version())) {
                conditions.addAll(contains(filterCriteria.version(), TOP_LEVEL_ASBIEP.VERSION));
            }
            if (hasLength(filterCriteria.remark())) {
                conditions.addAll(contains(filterCriteria.remark(), ASBIEP.REMARK));
            }
            if (!filterCriteria.states().isEmpty()) {
                conditions.add(BIE_PACKAGE.STATE.in(filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())));
            }
            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.releaseIds() != null && !filterCriteria.releaseIds().isEmpty()) {
                conditions.add(RELEASE.RELEASE_ID.in(valueOf(filterCriteria.releaseIds())));
            }
            if (filterCriteria.biePackageIds() != null && !filterCriteria.biePackageIds().isEmpty()) {
                conditions.add(BIE_PACKAGE.BIE_PACKAGE_ID.in(valueOf(filterCriteria.biePackageIds())));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP.lessThan(
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
                        field = BIE_PACKAGE.STATE;
                        break;

                    case "branch":
                        field = field("release_num_list");
                        break;

                    case "name":
                        field = BIE_PACKAGE.NAME;
                        break;

                    case "versionId":
                        field = BIE_PACKAGE.VERSION_ID;
                        break;

                    case "versionName":
                        field = BIE_PACKAGE.VERSION_NAME;
                        break;

                    case "owner":
                        field = ownerTable().LOGIN_ID;
                        break;

                    case "description":
                        field = BIE_PACKAGE.DESCRIPTION;
                        break;

                    case "lastUpdateTimestamp":
                        field = BIE_PACKAGE.LAST_UPDATE_TIMESTAMP;
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

            return sortFields;
        }

        public List<BiePackageListEntryRecord> fetch(SelectHavingStep<?> conditionStep, PageRequest pageRequest) {
            var sortFields = sortFields(pageRequest);
            SelectFinalStep<? extends org.jooq.Record> finalStep;
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

        RecordMapper<Record, BiePackageListEntryRecord> mapper() {
            return record -> {

                String releaseIdListStr = record.get(field("release_id_list"), String.class);
                List<String> releaseIdList = hasLength(releaseIdListStr) ? Arrays.asList(releaseIdListStr.split(",")) : Collections.emptyList();

                String releaseNumListStr = record.get(field("release_num_list"), String.class);
                List<String> releaseNumList = hasLength(releaseNumListStr) ? Arrays.asList(releaseNumListStr.split(",")) : Collections.emptyList();

                List<ReleaseSummaryRecord> releases = new ArrayList<>();
                if (!releaseIdList.isEmpty() && releaseIdList.size() == releaseNumList.size()) {
                    releases = repositoryFactory().releaseQueryRepository(requester()).getReleaseSummaryList(
                            releaseIdList.stream().map(e -> ReleaseId.from(e)).collect(Collectors.toSet())
                    );
                }

                BieState state = BieState.valueOf(record.get(BIE_PACKAGE.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                return new BiePackageListEntryRecord(
                        new BiePackageId(record.get(BIE_PACKAGE.BIE_PACKAGE_ID).toBigInteger()),
                        new Guid(record.get(BIE_PACKAGE.GUID)),
                        new LibraryId(record.get(BIE_PACKAGE.LIBRARY_ID).toBigInteger()),
                        record.get(BIE_PACKAGE.NAME),
                        record.get(BIE_PACKAGE.VERSION_ID),
                        record.get(BIE_PACKAGE.VERSION_NAME),
                        record.get(BIE_PACKAGE.DESCRIPTION),
                        releases,
                        state,
                        toAccessPrivilege(owner.userId(), state),
                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                Date.from(record.get(BIE_PACKAGE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                Date.from(record.get(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                        )
                );
            };
        }
    }

    @Override
    public BiePackageDetailsRecord getBiePackageDetails(BiePackageId biePackageId) {

        var queryBuilder = new GetBiePackageDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                .groupBy(BIE_PACKAGE.BIE_PACKAGE_ID)
                .fetchOne(queryBuilder.mapper());
    }

    private class GetBiePackageDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(concat(fields(
                            BIE_PACKAGE.BIE_PACKAGE_ID,
                            BIE_PACKAGE.GUID,
                            BIE_PACKAGE.LIBRARY_ID,
                            BIE_PACKAGE.NAME,
                            BIE_PACKAGE.VERSION_ID,
                            BIE_PACKAGE.VERSION_NAME,
                            BIE_PACKAGE.DESCRIPTION,
                            BIE_PACKAGE.REVISION_REASON,
                            groupConcatDistinct(RELEASE.RELEASE_ID).as("release_id_list"),
                            groupConcatDistinct(RELEASE.RELEASE_NUM).as("release_num_list"),
                            BIE_PACKAGE.STATE,
                            BIE_PACKAGE.PREV_BIE_PACKAGE_ID,
                            BIE_PACKAGE.CREATION_TIMESTAMP,
                            BIE_PACKAGE.LAST_UPDATE_TIMESTAMP,
                            BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID
                    ), libraryFields(), ownerFields(), creatorFields(), updaterFields()))
                    .from(BIE_PACKAGE)
                    .join(libraryTable()).on(libraryTablePk().eq(BIE_PACKAGE.LIBRARY_ID))
                    .join(ownerTable()).on(BIE_PACKAGE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(BIE_PACKAGE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BIE_PACKAGE.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(BIE_PACKAGE_TOP_LEVEL_ASBIEP).on(BIE_PACKAGE.BIE_PACKAGE_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID))
                    // -- only join the BIE-package -> top-level rows that are NOT superseded
                    .andNotExists(selectOne()
                            .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP.as("bptla2"))
                            .where(and(
                                    BIE_PACKAGE_TOP_LEVEL_ASBIEP.as("bptla2").PREV_TOP_LEVEL_ASBIEP_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                                    BIE_PACKAGE_TOP_LEVEL_ASBIEP.as("bptla2").BIE_PACKAGE_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID)
                            )))
                    .leftJoin(TOP_LEVEL_ASBIEP).on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .leftJoin(RELEASE).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                    .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .leftJoin(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                    .leftJoin(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID));
        }

        RecordMapper<Record, BiePackageDetailsRecord> mapper() {
            return record -> {

                String releaseIdListStr = record.get(field("release_id_list"), String.class);
                List<String> releaseIdList = hasLength(releaseIdListStr) ? Arrays.asList(releaseIdListStr.split(",")) : Collections.emptyList();

                String releaseNumListStr = record.get(field("release_num_list"), String.class);
                List<String> releaseNumList = hasLength(releaseNumListStr) ? Arrays.asList(releaseNumListStr.split(",")) : Collections.emptyList();

                List<ReleaseSummaryRecord> releases = new ArrayList<>();
                if (!releaseIdList.isEmpty() && releaseIdList.size() == releaseNumList.size()) {
                    releases = repositoryFactory().releaseQueryRepository(requester()).getReleaseSummaryList(
                            releaseIdList.stream().map(e -> ReleaseId.from(e)).collect(Collectors.toSet())
                    );
                }

                BiePackageId prevBiePackageId = (record.get(BIE_PACKAGE.PREV_BIE_PACKAGE_ID) != null) ?
                        new BiePackageId(record.get(BIE_PACKAGE.PREV_BIE_PACKAGE_ID).toBigInteger()) : null;
                BiePackageId sourceBiePackageId = (record.get(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID) != null) ?
                        new BiePackageId(record.get(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID).toBigInteger()) : null;

                BieState state = BieState.valueOf(record.get(BIE_PACKAGE.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                return new BiePackageDetailsRecord(
                        new BiePackageId(record.get(BIE_PACKAGE.BIE_PACKAGE_ID).toBigInteger()),
                        new Guid(record.get(BIE_PACKAGE.GUID)),
                        new LibraryId(record.get(BIE_PACKAGE.LIBRARY_ID).toBigInteger()),
                        record.get(BIE_PACKAGE.NAME),
                        record.get(BIE_PACKAGE.VERSION_ID),
                        record.get(BIE_PACKAGE.VERSION_NAME),
                        record.get(BIE_PACKAGE.DESCRIPTION),
                        record.get(BIE_PACKAGE.REVISION_REASON),
                        releases,
                        state,
                        toAccessPrivilege(owner.userId(), state),

                        getBiePackageSummary(prevBiePackageId),
                        getBiePackageSummary(sourceBiePackageId),
                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                Date.from(record.get(BIE_PACKAGE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                Date.from(record.get(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                        )
                );
            };
        }
    }

    private AccessPrivilege toAccessPrivilege(UserId biePackageOwnerId, BieState state) {
        UserId requesterId = requester().userId();

        AccessPrivilege accessPrivilege = Prohibited;
        switch (state) {
            case WIP:
                if (requesterId.equals(biePackageOwnerId)) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;
            case QA:
                if (requesterId.equals(biePackageOwnerId)) {
                    accessPrivilege = CanMove;
                } else {
                    accessPrivilege = CanView;
                }
                break;
            case Production:
                accessPrivilege = CanView;
                break;
        }
        return accessPrivilege;
    }

    @Override
    public List<TopLevelAsbiepId> getTopLevelAsbiepIdListInBiePackage(BiePackageId biePackageId) {
        Collection<BiePackageTopLevelAsbiepId> biePackageTopLevelAsbiepIdList = getBiePackageTopLevelAsbiepIdListInBiePackage(biePackageId);
        if (biePackageTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return dslContext().selectDistinct(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_TOP_LEVEL_ASBIEP_ID.in(biePackageTopLevelAsbiepIdList))
                .fetchStream().map(record ->
                        new TopLevelAsbiepId(record.get(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger()))
                .collect(Collectors.toList());
    }

    private Collection<BiePackageTopLevelAsbiepId> getBiePackageTopLevelAsbiepIdListInBiePackage(BiePackageId biePackageId) {

        CommonTableExpression<?> cte = name("hierarchy").fields(
                "bie_package_top_level_asbiep_id",
                "bie_package_id",
                "top_level_asbiep_id",
                "prev_top_level_asbiep_id"
        ).as(
                dslContext()
                        .select(
                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_TOP_LEVEL_ASBIEP_ID,
                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID,
                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.PREV_TOP_LEVEL_ASBIEP_ID
                        )
                        .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                        .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.PREV_TOP_LEVEL_ASBIEP_ID.isNull())
                        .and(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                        .unionAll(
                                dslContext()
                                        .select(
                                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_TOP_LEVEL_ASBIEP_ID,
                                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID,
                                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                                                BIE_PACKAGE_TOP_LEVEL_ASBIEP.PREV_TOP_LEVEL_ASBIEP_ID
                                        )
                                        .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                                        .join(table(name("hierarchy")))
                                        .on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.PREV_TOP_LEVEL_ASBIEP_ID.eq(field(name("hierarchy", "top_level_asbiep_id"), ULong.class))
                                                .and(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(field(name("hierarchy", "bie_package_id"), ULong.class))))
                        )
        );

        return dslContext().withRecursive(cte)
                .selectDistinct(field(name("hierarchy", "bie_package_top_level_asbiep_id"), ULong.class))
                .from(table(name("hierarchy")))
                .leftJoin(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .on(field(name("hierarchy", "top_level_asbiep_id"), ULong.class)
                        .eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.PREV_TOP_LEVEL_ASBIEP_ID)
                        .and(field(name("hierarchy", "bie_package_id"), ULong.class)
                                .eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID)))
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.isNull())
                .fetchStream().map(e -> new BiePackageTopLevelAsbiepId(e.component1().toBigInteger()))
                .collect(Collectors.toList());
    }

    @Override
    public ResultAndCount<BieListEntryRecord> getBieListInBiePackage(
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
            if (StringUtils.hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(ASCCP.PROPERTY_TERM), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().selectDistinct(concat(fields.stream(), libraryFields(), releaseFields(), ownerFields(), creatorFields(), updaterFields()))
                    .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                    .join(TOP_LEVEL_ASBIEP).on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ASBIEP).on(and(
                            ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                            ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                    )
                    .join(ABIE).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.ABIE_ID))
                    .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(releaseTable()).on(releaseTablePk().eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                    .join(libraryTable()).on(libraryTablePk().eq(releaseTable().LIBRARY_ID))
                    .join(ownerTable()).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(ASBIEP.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY.eq(updaterTablePk()))
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
                conditions.add(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_TOP_LEVEL_ASBIEP_ID.in(
                        getBiePackageTopLevelAsbiepIdListInBiePackage(filterCriteria.biePackageId())
                ));
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

            if (StringUtils.hasLength(filterCriteria.den())) {
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
                        fetchLibrarySummary(record),
                        fetchReleaseSummary(record),

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

    public boolean exists(BiePackageId biePackageId, TopLevelAsbiepId topLevelAsbiepId) {

        return dslContext().selectCount()
                .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(and(
                        BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(valueOf(biePackageId)),
                        BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    public boolean hasDuplicateVersion(BiePackageId biePackageId, String versionId) {

        BiePackageSummaryRecord biePackage = getBiePackageSummary(biePackageId);
        return dslContext().selectCount()
                .from(BIE_PACKAGE)
                .where(and(
                        BIE_PACKAGE.NAME.eq(biePackage.name()),
                        BIE_PACKAGE.VERSION_ID.eq(versionId),
                        BIE_PACKAGE.VERSION_NAME.eq(biePackage.versionName())
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

}
