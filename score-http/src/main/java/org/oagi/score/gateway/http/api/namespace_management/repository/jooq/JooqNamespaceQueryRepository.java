package org.oagi.score.gateway.http.api.namespace_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceListEntryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.repository.criteria.NamespaceListFilterCriteria;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.LIBRARY;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.NAMESPACE;

public class JooqNamespaceQueryRepository extends JooqBaseRepository implements NamespaceQueryRepository {

    public JooqNamespaceQueryRepository(DSLContext dslContext, ScoreUser requester,
                                        RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public NamespaceSummaryRecord getNamespaceSummary(NamespaceId namespaceId) {
        var queryBuilder = new GetNamespaceSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<NamespaceSummaryRecord> getNamespaceSummaryList() {
        var queryBuilder = new GetNamespaceSummaryListQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<NamespaceSummaryRecord> getNamespaceSummaryList(LibraryId libraryId) {
        var queryBuilder = new GetNamespaceSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public NamespaceSummaryRecord getAnyStandardNamespaceSummary(LibraryId libraryId) {
        var queryBuilder = new GetNamespaceSummaryListQueryBuilder();
        return queryBuilder.select()
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .limit(1)
                .fetchOptional(queryBuilder.mapper()).orElse(null);
    }

    private class GetNamespaceSummaryListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(NAMESPACE.NAMESPACE_ID,
                            NAMESPACE.URI,
                            NAMESPACE.PREFIX,
                            NAMESPACE.IS_STD_NMSP)
                    .from(NAMESPACE)
                    .join(LIBRARY).on(NAMESPACE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID));
        }

        RecordMapper<Record, NamespaceSummaryRecord> mapper() {
            return record -> new NamespaceSummaryRecord(
                    new NamespaceId(record.getValue(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                    record.getValue(NAMESPACE.URI),
                    record.getValue(NAMESPACE.PREFIX),
                    record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
            );
        }
    }

    @Override
    public ResultAndCount<NamespaceListEntryRecord> getNamespaceList(NamespaceListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetNamespaceListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<NamespaceListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetNamespaceListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {

            return dslContext().select(concat(fields(
                            NAMESPACE.NAMESPACE_ID, LIBRARY.LIBRARY_ID, NAMESPACE.URI, NAMESPACE.PREFIX,
                            NAMESPACE.DESCRIPTION, NAMESPACE.IS_STD_NMSP,
                            NAMESPACE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), updaterFields()))
                    .from(NAMESPACE)
                    .join(LIBRARY).on(LIBRARY.LIBRARY_ID.eq(NAMESPACE.LIBRARY_ID))
                    .join(ownerTable()).on(NAMESPACE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(updaterTable()).on(NAMESPACE.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions(NamespaceListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();
            conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));
            if (StringUtils.hasLength(filterCriteria.uri())) {
                conditions.add(NAMESPACE.URI.containsIgnoreCase(filterCriteria.uri()));
            }
            if (StringUtils.hasLength(filterCriteria.prefix())) {
                conditions.add(NAMESPACE.PREFIX.containsIgnoreCase(filterCriteria.prefix()));
            }
            if (StringUtils.hasLength(filterCriteria.description())) {
                conditions.add(NAMESPACE.DESCRIPTION.containsIgnoreCase(filterCriteria.description()));
            }
            if (filterCriteria.standard() != null) {
                conditions.add(NAMESPACE.IS_STD_NMSP.eq((byte) (filterCriteria.standard() ? 1 : 0)));
            }
            if (filterCriteria.ownerLoginIdSet() != null && !filterCriteria.ownerLoginIdSet().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdSet()));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(NAMESPACE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(NAMESPACE.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "uri":
                        field = NAMESPACE.URI;
                        break;

                    case "prefix":
                        field = NAMESPACE.PREFIX;
                        break;

                    case "owner":
                        field = ownerTable().LOGIN_ID;
                        break;

                    case "std":
                    case "standard":
                        field = NAMESPACE.IS_STD_NMSP;
                        break;

                    case "description":
                        field = NAMESPACE.DESCRIPTION;
                        break;

                    case "lastUpdateTimestamp":
                        field = NAMESPACE.LAST_UPDATE_TIMESTAMP;
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

        public List<NamespaceListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        RecordMapper<Record, NamespaceListEntryRecord> mapper() {
            return record -> new NamespaceListEntryRecord(
                    new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    record.get(NAMESPACE.URI),
                    record.get(NAMESPACE.PREFIX),
                    record.get(NAMESPACE.DESCRIPTION),
                    record.get(NAMESPACE.IS_STD_NMSP) == (byte) 1,
                    fetchOwnerSummary(record),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(NAMESPACE.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }
    }

    @Override
    public NamespaceDetailsRecord getNamespaceDetails(NamespaceId namespaceId, UserId requesterId) {

        var queryBuilder = new GetNamespaceDetailsQueryBuilder(requesterId);
        return queryBuilder.select()
                .where(NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetNamespaceDetailsQueryBuilder {

        private final UserId requesterId;

        GetNamespaceDetailsQueryBuilder(UserId requesterId) {
            this.requesterId = requesterId;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {

            return dslContext().select(concat(fields(
                            NAMESPACE.NAMESPACE_ID, LIBRARY.LIBRARY_ID, NAMESPACE.URI, NAMESPACE.PREFIX,
                            NAMESPACE.DESCRIPTION, NAMESPACE.IS_STD_NMSP,
                            NAMESPACE.CREATION_TIMESTAMP, NAMESPACE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(NAMESPACE)
                    .join(LIBRARY).on(LIBRARY.LIBRARY_ID.eq(NAMESPACE.LIBRARY_ID))
                    .join(ownerTable()).on(NAMESPACE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(NAMESPACE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(NAMESPACE.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<Record, NamespaceDetailsRecord> mapper() {
            return record -> new NamespaceDetailsRecord(
                    new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    record.get(NAMESPACE.URI), record.get(NAMESPACE.PREFIX), record.get(NAMESPACE.DESCRIPTION),
                    record.get(NAMESPACE.IS_STD_NMSP) == (byte) 1,
                    fetchOwnerSummary(record),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(NAMESPACE.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(NAMESPACE.LAST_UPDATE_TIMESTAMP))
                    ),
                    Objects.equals(fetchOwnerUserId(record), requesterId) // canEdit
            );
        }
    }

    @Override
    public boolean exists(NamespaceId namespaceId) {
        if (namespaceId == null) {
            throw new IllegalArgumentException();
        }
        return dslContext().selectCount()
                .from(NAMESPACE)
                .where(NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateUri(LibraryId libraryId, String uri) {
        return dslContext().selectCount()
                .from(NAMESPACE)
                .where(and(
                        NAMESPACE.LIBRARY_ID.eq(valueOf(libraryId)),
                        NAMESPACE.URI.eq(uri)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateUriExcludingCurrent(NamespaceId namespaceId, String uri) {
        ULong libraryId = dslContext().select(NAMESPACE.LIBRARY_ID)
                .from(NAMESPACE)
                .where(NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOneInto(ULong.class);

        return dslContext().selectCount()
                .from(NAMESPACE)
                .where(and(
                        NAMESPACE.LIBRARY_ID.eq(libraryId),
                        NAMESPACE.NAMESPACE_ID.ne(valueOf(namespaceId)),
                        NAMESPACE.URI.eq(uri)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicatePrefix(LibraryId libraryId, String prefix) {
        return dslContext().selectCount()
                .from(NAMESPACE)
                .where(and(
                        NAMESPACE.LIBRARY_ID.eq(valueOf(libraryId)),
                        (StringUtils.hasLength(prefix)) ? NAMESPACE.PREFIX.eq(prefix) :
                                or(NAMESPACE.PREFIX.isNull(), length(NAMESPACE.PREFIX).eq(0))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicatePrefixExcludingCurrent(NamespaceId namespaceId, String prefix) {
        ULong libraryId = dslContext().select(NAMESPACE.LIBRARY_ID)
                .from(NAMESPACE)
                .where(NAMESPACE.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOneInto(ULong.class);

        return dslContext().selectCount()
                .from(NAMESPACE)
                .where(and(
                        NAMESPACE.LIBRARY_ID.eq(libraryId),
                        NAMESPACE.NAMESPACE_ID.ne(valueOf(namespaceId)),
                        (StringUtils.hasLength(prefix)) ? NAMESPACE.PREFIX.eq(prefix) :
                                or(NAMESPACE.PREFIX.isNull(), length(NAMESPACE.PREFIX).eq(0))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

}
