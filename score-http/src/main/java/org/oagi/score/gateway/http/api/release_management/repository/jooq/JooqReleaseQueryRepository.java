package org.oagi.score.gateway.http.api.release_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.*;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.release_management.repository.criteria.ReleaseListFilterCriteria;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/**
 * Repository implementation using JOOQ for querying release-related data.
 */
public class JooqReleaseQueryRepository extends JooqBaseRepository implements ReleaseQueryRepository {

    /**
     * Constructs a new {@code JooqReleaseQueryRepository} with the given {@code DSLContext}.
     *
     * @param dslContext The {@code DSLContext} used to interact with the database.
     */
    public JooqReleaseQueryRepository(DSLContext dslContext, ScoreUser requester,
                                      RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ReleaseSummaryRecord getReleaseSummary(ReleaseId releaseId) {
        if (releaseId == null) {
            return null;
        }

        var queryBuilder = new GetReleaseSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public ReleaseSummaryRecord getReleaseSummary(LibraryId libraryId, String releaseNum) {
        var queryBuilder = new GetReleaseSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        RELEASE.LIBRARY_ID.eq(valueOf(libraryId)),
                        RELEASE.RELEASE_NUM.eq(releaseNum)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<ReleaseSummaryRecord> getReleaseSummaryList(LibraryId libraryId) {
        return getReleaseSummaryList(libraryId, null);
    }

    @Override
    public List<ReleaseSummaryRecord> getReleaseSummaryList(LibraryId libraryId, Collection<ReleaseState> releaseStateSet) {
        List<Condition> conditions = new ArrayList();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        if (releaseStateSet != null && !releaseStateSet.isEmpty()) {
            conditions.add(RELEASE.STATE.in(releaseStateSet));
        }

        var queryBuilder = new GetReleaseSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .orderBy(RELEASE.RELEASE_ID.desc())
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<ReleaseSummaryRecord> getDependentReleaseSummaryList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetReleaseSummaryListQueryBuilder();
        return queryBuilder.select()
                .join(RELEASE_DEP).on(RELEASE_DEP.DEPEND_ON_RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .where(RELEASE_DEP.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public Set<ReleaseSummaryRecord> getIncludedReleaseSummaryList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptySet();
        }

        Set<ReleaseSummaryRecord> includedReleases = new HashSet<>();
        retrieveIncludedReleases(getReleaseSummary(releaseId), includedReleases);
        return includedReleases;
    }

    private void retrieveIncludedReleases(ReleaseSummaryRecord release, Set<ReleaseSummaryRecord> result) {
        if (release == null || !result.add(release)) {
            return;  // Prevent cycles
        }

        getDependentReleaseSummaryList(release.releaseId()).forEach(includedRelease -> {
            retrieveIncludedReleases(includedRelease, result);
        });
    }

    @Override
    public Map<AsccpManifestId, ReleaseId> getReleaseIdMapByAsccpManifestIdList(Collection<AsccpManifestId> asccpManifestIdList) {
        if (asccpManifestIdList == null || asccpManifestIdList.isEmpty()) {
            return Collections.emptyMap();
        }
        return dslContext().select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.RELEASE_ID)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(
                        asccpManifestIdList.stream().map(e -> valueOf(e)).collect(Collectors.toList())))
                .fetchStream().collect(Collectors.toMap(
                        e -> new AsccpManifestId(e.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger()),
                        e -> new ReleaseId(e.get(ASCCP_MANIFEST.RELEASE_ID).toBigInteger())));
    }

    private class GetReleaseSummaryListQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            RELEASE.RELEASE_ID,
                            RELEASE.LIBRARY_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE)
                    .from(RELEASE);
        }

        private RecordMapper<org.jooq.Record, ReleaseSummaryRecord> mapper() {
            return record -> new ReleaseSummaryRecord(
                    new ReleaseId(record.getValue(RELEASE.RELEASE_ID).toBigInteger()),
                    new LibraryId(record.getValue(RELEASE.LIBRARY_ID).toBigInteger()),
                    record.getValue(RELEASE.RELEASE_NUM),
                    ReleaseState.valueOf(record.getValue(RELEASE.STATE))
            );
        }
    }

    @Override
    public ResultAndCount<ReleaseListEntryRecord> getReleaseList(
            ReleaseListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetReleaseListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<ReleaseListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetReleaseListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            RELEASE.RELEASE_ID,
                            RELEASE.LIBRARY_ID,
                            RELEASE.GUID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE,
                            RELEASE.NAMESPACE_ID,
                            RELEASE.CREATION_TIMESTAMP,
                            RELEASE.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(RELEASE)
                    .join(LIBRARY).on(LIBRARY.LIBRARY_ID.eq(RELEASE.LIBRARY_ID))
                    .join(creatorTable()).on(RELEASE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(RELEASE.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions(ReleaseListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();
            conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));
            if (StringUtils.hasLength(filterCriteria.releaseNum())) {
                conditions.add(RELEASE.RELEASE_NUM.containsIgnoreCase(filterCriteria.releaseNum().trim()));
            }
            if (filterCriteria.excludeReleaseNumSet() != null && !filterCriteria.excludeReleaseNumSet().isEmpty()) {
                conditions.add(RELEASE.RELEASE_NUM.notIn(filterCriteria.excludeReleaseNumSet()));
            }
            if (filterCriteria.releaseStateSet() != null && !filterCriteria.releaseStateSet().isEmpty()) {
                conditions.add(RELEASE.STATE.in(filterCriteria.releaseStateSet()));
            }
            if (filterCriteria.namespaceIdSet() != null && !filterCriteria.namespaceIdSet().isEmpty()) {
                conditions.add(RELEASE.NAMESPACE_ID.in(valueOf(filterCriteria.namespaceIdSet())));
            }
            if (filterCriteria.creatorLoginIdSet() != null && !filterCriteria.creatorLoginIdSet().isEmpty()) {
                conditions.add(creatorTable().LOGIN_ID.in(filterCriteria.creatorLoginIdSet()));
            }
            if (filterCriteria.createdTimestampRange() != null) {
                if (filterCriteria.createdTimestampRange().after() != null) {
                    conditions.add(RELEASE.CREATION_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.createdTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.createdTimestampRange().before() != null) {
                    conditions.add(RELEASE.CREATION_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.createdTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(RELEASE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(RELEASE.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "releaseNum":
                        field = RELEASE.RELEASE_NUM;
                        break;

                    case "state":
                        field = RELEASE.STATE;
                        break;

                    case "creationTimestamp":
                        field = RELEASE.CREATION_TIMESTAMP;
                        break;

                    case "lastUpdateTimestamp":
                        field = RELEASE.LAST_UPDATE_TIMESTAMP;
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

        public List<ReleaseListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        private RecordMapper<org.jooq.Record, ReleaseListEntryRecord> mapper() {
            return record -> new ReleaseListEntryRecord(
                    new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                    new LibraryId(record.get(RELEASE.LIBRARY_ID).toBigInteger()),
                    new Guid(record.get(RELEASE.GUID)),
                    record.get(RELEASE.RELEASE_NUM),
                    ReleaseState.valueOf(record.get(RELEASE.STATE)),
                    (record.get(RELEASE.NAMESPACE_ID) != null) ?
                            new NamespaceId(record.get(RELEASE.NAMESPACE_ID).toBigInteger()) : null,
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(RELEASE.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(RELEASE.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }
    }

    @Override
    public ReleaseDetailsRecord getReleaseDetails(ReleaseId releaseId) {
        if (releaseId == null) {
            return null;
        }
        var queryBuilder = new GetReleaseDetailsQueryBuilder();
        return queryBuilder.select()
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public ReleaseDetailsRecord getReleaseDetails(LibraryId libraryId, String releaseNum) {
        if (libraryId == null || releaseNum == null) {
            return null;
        }
        var queryBuilder = new GetReleaseDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        RELEASE.LIBRARY_ID.eq(valueOf(libraryId)),
                        RELEASE.RELEASE_NUM.eq(releaseNum)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<ReleaseDetailsRecord> getReleaseDetailsList() {
        var queryBuilder = new GetReleaseDetailsQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    private class GetReleaseDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            RELEASE.RELEASE_ID,
                            RELEASE.LIBRARY_ID,
                            RELEASE.GUID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE,
                            RELEASE.NAMESPACE_ID,
                            RELEASE.RELEASE_NOTE,
                            RELEASE.RELEASE_LICENSE,
                            RELEASE.CREATION_TIMESTAMP,
                            RELEASE.LAST_UPDATE_TIMESTAMP,
                            RELEASE.PREV_RELEASE_ID,
                            RELEASE.as("prev").LIBRARY_ID.as("prev_library_id"),
                            RELEASE.as("prev").RELEASE_NUM.as("prev_release_num"),
                            RELEASE.as("prev").STATE.as("prev_release_state"),
                            RELEASE.NEXT_RELEASE_ID,
                            RELEASE.as("next").LIBRARY_ID.as("next_library_id"),
                            RELEASE.as("next").RELEASE_NUM.as("next_release_num"),
                            RELEASE.as("next").STATE.as("next_release_state")
                    ), creatorFields(), updaterFields()))
                    .from(RELEASE)
                    .join(LIBRARY).on(LIBRARY.LIBRARY_ID.eq(RELEASE.LIBRARY_ID))
                    .join(creatorTable()).on(RELEASE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(RELEASE.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(RELEASE.as("prev")).on(RELEASE.PREV_RELEASE_ID.eq(RELEASE.as("prev").RELEASE_ID))
                    .leftJoin(RELEASE.as("next")).on(RELEASE.NEXT_RELEASE_ID.eq(RELEASE.as("next").RELEASE_ID));
        }

        RecordMapper<Record, ReleaseDetailsRecord> mapper() {
            return record -> new ReleaseDetailsRecord(
                    new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                    new LibraryId(record.get(RELEASE.LIBRARY_ID).toBigInteger()),
                    new Guid(record.get(RELEASE.GUID)),
                    record.get(RELEASE.RELEASE_NUM),
                    ReleaseState.valueOf(record.get(RELEASE.STATE)),
                    record.get(RELEASE.RELEASE_NOTE),
                    record.get(RELEASE.RELEASE_LICENSE),
                    (record.get(RELEASE.NAMESPACE_ID) != null) ?
                            new NamespaceId(record.get(RELEASE.NAMESPACE_ID).toBigInteger()) : null,
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            Date.from(record.get(RELEASE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            Date.from(record.get(RELEASE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    ),
                    record.get(RELEASE.PREV_RELEASE_ID) != null ?
                            new ReleaseSummaryRecord(
                                    new ReleaseId(record.get(RELEASE.PREV_RELEASE_ID).toBigInteger()),
                                    new LibraryId(record.get(RELEASE.as("prev").LIBRARY_ID.as("prev_library_id")).toBigInteger()),
                                    record.get(RELEASE.as("prev").RELEASE_NUM.as("prev_release_num")),
                                    ReleaseState.valueOf(record.get(RELEASE.as("prev").STATE.as("prev_release_state")))
                            ) : null,
                    record.get(RELEASE.NEXT_RELEASE_ID) != null ?
                            new ReleaseSummaryRecord(
                                    new ReleaseId(record.get(RELEASE.NEXT_RELEASE_ID).toBigInteger()),
                                    new LibraryId(record.get(RELEASE.as("next").LIBRARY_ID.as("next_library_id")).toBigInteger()),
                                    record.get(RELEASE.as("next").RELEASE_NUM.as("next_release_num")),
                                    ReleaseState.valueOf(record.get(RELEASE.as("next").STATE.as("next_release_state")))
                            ) : null
            );
        }
    }

    @Override
    public boolean exists(ReleaseId releaseId) {
        if (releaseId == null) {
            throw new IllegalArgumentException();
        }
        return dslContext().selectCount()
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateReleaseNumber(LibraryId libraryId, String releaseNum) {
        return dslContext().selectCount()
                .from(RELEASE)
                .where(and(
                        RELEASE.LIBRARY_ID.eq(valueOf(libraryId)),
                        RELEASE.RELEASE_NUM.eq(releaseNum)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateReleaseNumberExcludingCurrent(ReleaseId releaseId, String releaseNum) {
        ULong libraryId = dslContext().select(RELEASE.LIBRARY_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchOneInto(ULong.class);

        return dslContext().selectCount()
                .from(RELEASE)
                .where(and(
                        RELEASE.LIBRARY_ID.eq(libraryId),
                        RELEASE.RELEASE_ID.ne(valueOf(releaseId)),
                        RELEASE.RELEASE_NUM.eq(releaseNum)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasRecordsByNamespaceId(NamespaceId namespaceId) {
        return dslContext().selectCount()
                .from(RELEASE)
                .where(RELEASE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public AssignComponents getAssignComponents(ReleaseId releaseId) {
        AssignComponents assignComponents = new AssignComponents();

        // ACCs
        Map<ULong, List<Record8<
                ULong, String, String, LocalDateTime, String,
                String, UInteger, UInteger>>> map =
                dslContext().select(
                                ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                                ACC.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ACC.STATE,
                                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                        .from(ACC_MANIFEST)
                        .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                        .where(and(
                                or(
                                        RELEASE.RELEASE_ID.eq(valueOf(releaseId)),
                                        RELEASE.RELEASE_NUM.eq("Working")
                                ),
                                ACC.STATE.notEqual(CcState.Published.name())
                        ))
                        .fetchStream()
                        .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            AccManifestId accManifestId = new AccManifestId(e.get(0).value1().toBigInteger());
            node.setManifestId(accManifestId);
            node.setDen(e.get(0).value2());
            node.setTimestamp(toDate(e.get(0).value4()));
            node.setOwnerUsername(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.ACC);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableAccManifest(
                        accManifestId, node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableAccManifest(
                        accManifestId, node);
            }
        });

        // ASCCPs
        map = dslContext().select(
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        ASCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ASCCP.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(valueOf(releaseId)),
                                RELEASE.RELEASE_NUM.eq("Working")
                        ),
                        ASCCP.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            AsccpManifestId asccpManifestId = new AsccpManifestId(e.get(0).value1().toBigInteger());
            node.setManifestId(asccpManifestId);
            node.setDen(e.get(0).value2());
            node.setTimestamp(toDate(e.get(0).value4()));
            node.setOwnerUsername(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.ASCCP);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableAsccpManifest(
                        asccpManifestId, node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableAsccpManifest(
                        asccpManifestId, node);
            }
        });

        // BCCPs
        map = dslContext().select(
                        BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        BCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, BCCP.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(valueOf(releaseId)),
                                RELEASE.RELEASE_NUM.eq("Working")
                        ),
                        BCCP.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            BccpManifestId bccpManifestId = new BccpManifestId(e.get(0).value1().toBigInteger());
            node.setManifestId(bccpManifestId);
            node.setDen(e.get(0).value2());
            node.setTimestamp(toDate(e.get(0).value4()));
            node.setOwnerUsername(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.BCCP);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableBccpManifest(
                        bccpManifestId, node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableBccpManifest(
                        bccpManifestId, node);
            }
        });

        // CODE_LISTs
        map = dslContext().select(
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME, RELEASE.RELEASE_NUM,
                        CODE_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, CODE_LIST.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(CODE_LIST_MANIFEST)
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(APP_USER).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(valueOf(releaseId)),
                                RELEASE.RELEASE_NUM.eq("Working")
                        ),
                        CODE_LIST.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            CodeListManifestId codeListManifestId = new CodeListManifestId(e.get(0).value1().toBigInteger());
            node.setManifestId(codeListManifestId);
            node.setDen(e.get(0).value2());
            node.setTimestamp(toDate(e.get(0).value4()));
            node.setOwnerUsername(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.CODE_LIST);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableCodeListManifest(
                        codeListManifestId, node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableCodeListManifest(
                        codeListManifestId, node);
            }
        });

        // AGENCY_ID_LISTs
        map = dslContext().select(
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME, RELEASE.RELEASE_NUM,
                        AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, AGENCY_ID_LIST.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(APP_USER).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(valueOf(releaseId)),
                                RELEASE.RELEASE_NUM.eq("Working")
                        ),
                        AGENCY_ID_LIST.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            AgencyIdListManifestId agencyIdListManifestId = new AgencyIdListManifestId(e.get(0).value1().toBigInteger());
            node.setManifestId(agencyIdListManifestId);
            node.setDen(e.get(0).value2());
            node.setTimestamp(toDate(e.get(0).value4()));
            node.setOwnerUsername(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.AGENCY_ID_LIST);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableAgencyIdListManifest(
                        agencyIdListManifestId, node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableAgencyIdListManifest(
                        agencyIdListManifestId, node);
            }
        });

        // DTs
        map = dslContext().select(
                        DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        DT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, DT.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(valueOf(releaseId)),
                                RELEASE.RELEASE_NUM.eq("Working")
                        ),
                        DT.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            DtManifestId dtManifestId = new DtManifestId(e.get(0).value1().toBigInteger());
            node.setManifestId(dtManifestId);
            node.setDen(e.get(0).value2());
            node.setTimestamp(toDate(e.get(0).value4()));
            node.setOwnerUsername(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.DT);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableDtManifest(
                        dtManifestId, node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableDtManifest(
                        dtManifestId, node);
            }
        });

        return assignComponents;
    }

}
