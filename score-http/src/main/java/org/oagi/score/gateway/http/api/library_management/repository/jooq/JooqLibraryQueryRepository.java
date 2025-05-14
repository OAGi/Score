package org.oagi.score.gateway.http.api.library_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryListEntry;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.library_management.repository.criteria.LibraryListFilterCriteria;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.LIBRARY;
import static org.springframework.util.StringUtils.hasLength;

public class JooqLibraryQueryRepository extends JooqBaseRepository implements LibraryQueryRepository {

    public JooqLibraryQueryRepository(DSLContext dslContext, ScoreUser requester,
                                      RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<LibrarySummaryRecord> getLibrarySummaryList() {
        var queryBuilder = new GetLibrarySummaryListQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public LibrarySummaryRecord getLibrarySummaryByName(String name) {
        if (!hasLength(name)) {
            return null;
        }

        var queryBuilder = new GetLibrarySummaryListQueryBuilder();
        return queryBuilder.select()
                .where(LIBRARY.NAME.eq(name))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetLibrarySummaryListQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME,
                            LIBRARY.STATE,
                            LIBRARY.IS_READ_ONLY)
                    .from(LIBRARY);
        }

        RecordMapper<Record, LibrarySummaryRecord> mapper() {
            return record -> new LibrarySummaryRecord(
                    new LibraryId(record.getValue(LIBRARY.LIBRARY_ID).toBigInteger()),
                    record.getValue(LIBRARY.NAME),
                    record.getValue(LIBRARY.STATE),
                    record.getValue(LIBRARY.IS_READ_ONLY) == (byte) 1
            );
        }
    }

    @Override
    public LibraryDetailsRecord getLibraryDetails(LibraryId libraryId) {
        var queryBuilder = new GetLibraryDetailsQueryBuilder();
        return queryBuilder.select()
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetLibraryDetailsQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            LIBRARY.LIBRARY_ID, LIBRARY.TYPE, LIBRARY.NAME, LIBRARY.ORGANIZATION, LIBRARY.DESCRIPTION,
                            LIBRARY.LINK, LIBRARY.DOMAIN, LIBRARY.STATE, LIBRARY.IS_READ_ONLY,
                            LIBRARY.CREATION_TIMESTAMP, LIBRARY.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(LIBRARY)
                    .join(creatorTable()).on(LIBRARY.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(LIBRARY.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<Record, LibraryDetailsRecord> mapper() {
            return record -> new LibraryDetailsRecord(
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    record.get(LIBRARY.TYPE), record.get(LIBRARY.NAME), record.get(LIBRARY.ORGANIZATION), record.get(LIBRARY.DESCRIPTION),
                    record.get(LIBRARY.LINK), record.get(LIBRARY.DOMAIN), record.get(LIBRARY.STATE),
                    record.get(LIBRARY.IS_READ_ONLY) == (byte) 1,
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(LIBRARY.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(LIBRARY.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }
    }

    @Override
    public ResultAndCount<LibraryListEntry> getLibraryList(LibraryListFilterCriteria filterCriteria, PageRequest pageRequest) {
        var queryBuilder = new GetLibraryListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<LibraryListEntry> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetLibraryListQueryBuilder {

        SelectOnConditionStep<Record> select() {
            return dslContext().select(concat(fields(
                            LIBRARY.LIBRARY_ID, LIBRARY.TYPE, LIBRARY.NAME, LIBRARY.ORGANIZATION, LIBRARY.DESCRIPTION,
                            LIBRARY.LINK, LIBRARY.DOMAIN, LIBRARY.STATE, LIBRARY.IS_READ_ONLY,
                            LIBRARY.CREATION_TIMESTAMP, LIBRARY.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(LIBRARY)
                    .join(creatorTable()).on(LIBRARY.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(LIBRARY.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions(LibraryListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();
            if (hasLength(filterCriteria.type())) {
                conditions.add(LIBRARY.TYPE.containsIgnoreCase(filterCriteria.type()));
            }
            if (hasLength(filterCriteria.name())) {
                conditions.add(LIBRARY.NAME.containsIgnoreCase(filterCriteria.name()));
            }
            if (hasLength(filterCriteria.organization())) {
                conditions.add(LIBRARY.ORGANIZATION.containsIgnoreCase(filterCriteria.organization()));
            }
            if (hasLength(filterCriteria.description())) {
                conditions.add(LIBRARY.DESCRIPTION.containsIgnoreCase(filterCriteria.description()));
            }
            if (hasLength(filterCriteria.domain())) {
                conditions.add(LIBRARY.DOMAIN.containsIgnoreCase(filterCriteria.domain()));
            }
            if (hasLength(filterCriteria.state())) {
                conditions.add(LIBRARY.STATE.containsIgnoreCase(filterCriteria.state()));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(LIBRARY.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(LIBRARY.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "type":
                        field = LIBRARY.TYPE;
                        break;

                    case "name":
                        field = LIBRARY.NAME;
                        break;

                    case "organization":
                        field = LIBRARY.ORGANIZATION;
                        break;

                    case "description":
                        field = LIBRARY.DESCRIPTION;
                        break;

                    case "domain":
                        field = LIBRARY.DOMAIN;
                        break;

                    case "state":
                        field = LIBRARY.STATE;
                        break;

                    case "lastUpdateTimestamp":
                        field = LIBRARY.LAST_UPDATE_TIMESTAMP;
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

        public List<LibraryListEntry> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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
            return finalStep.fetch(record -> mapper(record));
        }

        LibraryListEntry mapper(Record record) {
            return new LibraryListEntry(
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    record.get(LIBRARY.TYPE), record.get(LIBRARY.NAME), record.get(LIBRARY.ORGANIZATION), record.get(LIBRARY.DESCRIPTION),
                    record.get(LIBRARY.LINK), record.get(LIBRARY.DOMAIN), record.get(LIBRARY.STATE),
                    record.get(LIBRARY.IS_READ_ONLY) == (byte) 1,
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(LIBRARY.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(LIBRARY.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }
    }

    @Override
    public boolean isReadOnly(LibraryId libraryId) {
        return dslContext().select(LIBRARY.IS_READ_ONLY)
                .from(LIBRARY)
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .fetchOneInto(Boolean.class);
    }

    @Override
    public boolean exists(LibraryId libraryId) {
        if (libraryId == null) {
            throw new IllegalArgumentException();
        }
        return dslContext().selectCount()
                .from(LIBRARY)
                .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateName(String name) {
        return dslContext().selectCount()
                .from(LIBRARY)
                .where(LIBRARY.NAME.eq(name))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateNameExcludingCurrent(LibraryId libraryId, String name) {
        return dslContext().selectCount()
                .from(LIBRARY)
                .where(and(
                        LIBRARY.LIBRARY_ID.ne(valueOf(libraryId)),
                        LIBRARY.NAME.eq(name)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

}
