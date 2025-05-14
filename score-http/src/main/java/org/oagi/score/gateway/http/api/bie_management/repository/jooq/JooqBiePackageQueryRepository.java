package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.groupConcatDistinct;
import static org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.gateway.http.common.model.AccessPrivilege.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
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

    private class GetBiePackageSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(concat(fields(
                            BIE_PACKAGE.BIE_PACKAGE_ID,
                            BIE_PACKAGE.LIBRARY_ID,
                            BIE_PACKAGE.VERSION_ID,
                            BIE_PACKAGE.VERSION_NAME,
                            BIE_PACKAGE.DESCRIPTION,
                            groupConcatDistinct(RELEASE.RELEASE_ID).as("release_id_list"),
                            groupConcatDistinct(RELEASE.RELEASE_NUM).as("release_num_list"),
                            BIE_PACKAGE.STATE
                    ), ownerFields()))
                    .from(BIE_PACKAGE)
                    .join(ownerTable()).on(BIE_PACKAGE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(LIBRARY).on(BIE_PACKAGE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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
                if (!releaseIdList.isEmpty() && !releaseIdList.isEmpty() && releaseIdList.size() == releaseNumList.size()) {
                    for (int i = 0, len = releaseIdList.size(); i < len; ++i) {
                        ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                                new ReleaseId(new BigInteger(releaseIdList.get(i))),
                                null,
                                releaseNumList.get(i),
                                null
                        );
                        releases.add(release);
                    }
                }

                BieState state = BieState.valueOf(record.get(BIE_PACKAGE.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                return new BiePackageSummaryRecord(
                        new BiePackageId(record.get(BIE_PACKAGE.BIE_PACKAGE_ID).toBigInteger()),
                        new LibraryId(record.get(BIE_PACKAGE.LIBRARY_ID).toBigInteger()),
                        record.get(BIE_PACKAGE.VERSION_ID),
                        record.get(BIE_PACKAGE.VERSION_NAME),
                        record.get(BIE_PACKAGE.DESCRIPTION),
                        releases,
                        state,
                        owner
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
                    .join(ownerTable()).on(BIE_PACKAGE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(BIE_PACKAGE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BIE_PACKAGE.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .join(LIBRARY).on(BIE_PACKAGE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .leftJoin(BIE_PACKAGE_TOP_LEVEL_ASBIEP).on(BIE_PACKAGE.BIE_PACKAGE_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID))
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
                if (!releaseIdList.isEmpty() && !releaseIdList.isEmpty() && releaseIdList.size() == releaseNumList.size()) {
                    for (int i = 0, len = releaseIdList.size(); i < len; ++i) {
                        ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                                new ReleaseId(new BigInteger(releaseIdList.get(i))),
                                null,
                                releaseNumList.get(i),
                                null
                        );
                        releases.add(release);
                    }
                }

                BieState state = BieState.valueOf(record.get(BIE_PACKAGE.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                return new BiePackageListEntryRecord(
                        new BiePackageId(record.get(BIE_PACKAGE.BIE_PACKAGE_ID).toBigInteger()),
                        new LibraryId(record.get(BIE_PACKAGE.LIBRARY_ID).toBigInteger()),
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
                            BIE_PACKAGE.LIBRARY_ID,
                            BIE_PACKAGE.VERSION_ID,
                            BIE_PACKAGE.VERSION_NAME,
                            BIE_PACKAGE.DESCRIPTION,
                            groupConcatDistinct(RELEASE.RELEASE_ID).as("release_id_list"),
                            groupConcatDistinct(RELEASE.RELEASE_NUM).as("release_num_list"),
                            BIE_PACKAGE.STATE,
                            BIE_PACKAGE.CREATION_TIMESTAMP,
                            BIE_PACKAGE.LAST_UPDATE_TIMESTAMP,
                            BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BIE_PACKAGE)
                    .join(ownerTable()).on(BIE_PACKAGE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(BIE_PACKAGE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BIE_PACKAGE.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .join(LIBRARY).on(BIE_PACKAGE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .leftJoin(BIE_PACKAGE_TOP_LEVEL_ASBIEP).on(BIE_PACKAGE.BIE_PACKAGE_ID.eq(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID))
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
                if (!releaseIdList.isEmpty() && !releaseIdList.isEmpty() && releaseIdList.size() == releaseNumList.size()) {
                    for (int i = 0, len = releaseIdList.size(); i < len; ++i) {
                        ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                                new ReleaseId(new BigInteger(releaseIdList.get(i))),
                                null,
                                releaseNumList.get(i),
                                null
                        );
                        releases.add(release);
                    }
                }

                BiePackageId sourceBiePackageId = (record.get(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID) != null) ?
                        new BiePackageId(record.get(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID).toBigInteger()) : null;

                BieState state = BieState.valueOf(record.get(BIE_PACKAGE.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                return new BiePackageDetailsRecord(
                        new BiePackageId(record.get(BIE_PACKAGE.BIE_PACKAGE_ID).toBigInteger()),
                        new LibraryId(record.get(BIE_PACKAGE.LIBRARY_ID).toBigInteger()),
                        record.get(BIE_PACKAGE.VERSION_ID),
                        record.get(BIE_PACKAGE.VERSION_NAME),
                        record.get(BIE_PACKAGE.DESCRIPTION),
                        releases,
                        state,
                        toAccessPrivilege(owner.userId(), state),
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
        return dslContext().selectDistinct(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .from(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .join(TOP_LEVEL_ASBIEP).on(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                .fetchStream().map(record ->
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger()))
                .collect(Collectors.toList());
    }


}
