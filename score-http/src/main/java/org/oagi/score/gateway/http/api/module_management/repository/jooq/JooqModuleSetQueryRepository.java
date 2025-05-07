package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetListFilterCriteria;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.module_management.model.ModuleType.DIRECTORY;
import static org.oagi.score.gateway.http.api.module_management.model.ModuleType.FILE;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;

public class JooqModuleSetQueryRepository extends JooqBaseRepository implements ModuleSetQueryRepository {

    public JooqModuleSetQueryRepository(DSLContext dslContext, ScoreUser requester,
                                        RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<ModuleSetSummaryRecord> getModuleSetSummaryList(LibraryId libraryId) {
        var queryBuilder = new GetModuleSetSummaryQueryBuilder();
        return queryBuilder.select()
                .where(MODULE_SET.LIBRARY_ID.eq(valueOf(libraryId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetModuleSetSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            MODULE_SET.MODULE_SET_ID,
                            LIBRARY.LIBRARY_ID,
                            MODULE_SET.GUID,
                            MODULE_SET.NAME,
                            MODULE_SET.DESCRIPTION)
                    .from(MODULE_SET)
                    .join(LIBRARY).on(LIBRARY.LIBRARY_ID.eq(MODULE_SET.LIBRARY_ID));
        }

        RecordMapper<Record, ModuleSetSummaryRecord> mapper() {
            return record -> new ModuleSetSummaryRecord(
                    new ModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger()),
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    new Guid(record.get(MODULE_SET.GUID)),
                    record.get(MODULE_SET.NAME),
                    record.get(MODULE_SET.DESCRIPTION)
            );
        }
    }

    @Override
    public ModuleSetDetailsRecord getModuleSetDetails(ModuleSetId moduleSetId) {
        var queryBuilder = new GetModuleSetDetailsQueryBuilder();
        return queryBuilder.select()
                .where(MODULE_SET.MODULE_SET_ID.eq(valueOf(moduleSetId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetModuleSetDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            MODULE_SET.MODULE_SET_ID,
                            LIBRARY.LIBRARY_ID,
                            MODULE_SET.GUID,
                            MODULE_SET.NAME,
                            MODULE_SET.DESCRIPTION,
                            MODULE_SET.CREATION_TIMESTAMP,
                            MODULE_SET.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(MODULE_SET)
                    .join(LIBRARY).on(LIBRARY.LIBRARY_ID.eq(MODULE_SET.LIBRARY_ID))
                    .join(creatorTable()).on(MODULE_SET.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(MODULE_SET.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<Record, ModuleSetDetailsRecord> mapper() {
            return record -> new ModuleSetDetailsRecord(
                    new ModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger()),
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    new Guid(record.get(MODULE_SET.GUID)),
                    record.get(MODULE_SET.NAME),
                    record.get(MODULE_SET.DESCRIPTION),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            Date.from(record.get(MODULE_SET.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            Date.from(record.get(MODULE_SET.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    )
            );
        }
    }

    @Override
    public ResultAndCount<ModuleSetListEntryRecord> getModuleSetList(
            ModuleSetListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new ModuleSetListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<ModuleSetListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class ModuleSetListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            MODULE_SET.MODULE_SET_ID,
                            LIBRARY.LIBRARY_ID,
                            MODULE_SET.GUID,
                            MODULE_SET.NAME,
                            MODULE_SET.DESCRIPTION,
                            MODULE_SET.CREATION_TIMESTAMP,
                            MODULE_SET.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(MODULE_SET)
                    .join(LIBRARY).on(LIBRARY.LIBRARY_ID.eq(MODULE_SET.LIBRARY_ID))
                    .join(creatorTable()).on(MODULE_SET.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(MODULE_SET.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions(ModuleSetListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));
            if (StringUtils.hasLength(filterCriteria.name())) {
                conditions.addAll(contains(filterCriteria.name(), MODULE_SET.NAME));
            }
            if (StringUtils.hasLength(filterCriteria.description())) {
                conditions.addAll(contains(filterCriteria.description(), MODULE_SET.DESCRIPTION));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "name":
                        field = MODULE_SET.NAME;
                        break;

                    case "description":
                        field = MODULE_SET.DESCRIPTION;
                        break;

                    case "lastUpdateTimestamp":
                        field = MODULE_SET.LAST_UPDATE_TIMESTAMP;
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

        public List<ModuleSetListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        RecordMapper<Record, ModuleSetListEntryRecord> mapper() {
            return record -> new ModuleSetListEntryRecord(
                    new ModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger()),
                    new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                    new Guid(record.get(MODULE_SET.GUID)),
                    record.get(MODULE_SET.NAME),
                    record.get(MODULE_SET.DESCRIPTION),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            Date.from(record.get(MODULE_SET.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            Date.from(record.get(MODULE_SET.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant())
                    )
            );
        }
    }

    @Override
    public ModuleSetMetadataRecord getModuleSetMetadata(ModuleSetId moduleSetId) {

        int numberOfDirectories = dslContext().selectCount()
                .from(MODULE)
                .where(and(
                        MODULE.MODULE_SET_ID.eq(valueOf(moduleSetId)),
                        MODULE.TYPE.eq(DIRECTORY.name()),
                        MODULE.PARENT_MODULE_ID.isNotNull() // Do not count the root directory
                ))
                .fetchOptionalInto(Integer.class).orElse(0);

        int numberOfFiles = dslContext().selectCount()
                .from(MODULE)
                .where(and(
                        MODULE.MODULE_SET_ID.eq(valueOf(moduleSetId)),
                        MODULE.TYPE.eq(FILE.name())
                ))
                .fetchOptionalInto(Integer.class).orElse(0);

        return new ModuleSetMetadataRecord(moduleSetId, numberOfDirectories, numberOfFiles);
    }

}
