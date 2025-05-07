package org.oagi.score.gateway.http.api.context_management.context_category.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryListEntryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.criteria.ContextCategoryListFilterCriteria;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.CTX_CATEGORY;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.CTX_SCHEME;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;


public class JooqContextCategoryQueryRepository extends JooqBaseRepository implements ContextCategoryQueryRepository {

    public JooqContextCategoryQueryRepository(DSLContext dslContext, ScoreUser requester,
                                              RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<ContextCategorySummaryRecord> getContextCategorySummaryList() {
        var queryBuilder = new GetContextCategorySummaryListQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    private class GetContextCategorySummaryListQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            CTX_CATEGORY.CTX_CATEGORY_ID,
                            CTX_CATEGORY.GUID,
                            CTX_CATEGORY.NAME,
                            CTX_CATEGORY.DESCRIPTION)
                    .from(CTX_CATEGORY);
        }

        RecordMapper<org.jooq.Record, ContextCategorySummaryRecord> mapper() {
            return record -> new ContextCategorySummaryRecord(
                    new ContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger()),
                    new Guid(record.get(CTX_CATEGORY.GUID)),
                    record.get(CTX_CATEGORY.NAME),
                    record.get(CTX_CATEGORY.DESCRIPTION));
        }
    }

    @Override
    public ContextCategoryDetailsRecord getContextCategoryDetails(ContextCategoryId contextCategoryId) {
        var queryBuilder = new GetContextCategoryDetailsQueryBuilder();
        return queryBuilder.select()
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(valueOf(contextCategoryId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetContextCategoryDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CTX_CATEGORY.CTX_CATEGORY_ID,
                            CTX_CATEGORY.GUID,
                            CTX_CATEGORY.NAME,
                            CTX_CATEGORY.DESCRIPTION,
                            CTX_CATEGORY.CREATION_TIMESTAMP,
                            CTX_CATEGORY.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(CTX_CATEGORY)
                    .join(creatorTable()).on(CTX_CATEGORY.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CTX_CATEGORY.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, ContextCategoryDetailsRecord> mapper() {
            return record -> {
                ContextCategoryId contextCategoryId = new ContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger());
                return new ContextCategoryDetailsRecord(
                        contextCategoryId,
                        new Guid(record.get(CTX_CATEGORY.GUID)),
                        record.get(CTX_CATEGORY.NAME),
                        record.get(CTX_CATEGORY.DESCRIPTION),
                        isUsed(contextCategoryId),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CTX_CATEGORY.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP))
                        )
                );
            };
        }
    }

    private boolean isUsed(ContextCategoryId contextCategoryId) {
        return dslContext().selectCount().from(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_CATEGORY_ID.eq(valueOf(contextCategoryId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public ResultAndCount<ContextCategoryListEntryRecord> getContextCategoryList(
            ContextCategoryListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetContextCategoryListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<ContextCategoryListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetContextCategoryListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CTX_CATEGORY.CTX_CATEGORY_ID,
                            CTX_CATEGORY.GUID,
                            CTX_CATEGORY.NAME,
                            CTX_CATEGORY.DESCRIPTION,
                            CTX_CATEGORY.CREATION_TIMESTAMP,
                            CTX_CATEGORY.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(CTX_CATEGORY)
                    .join(creatorTable()).on(CTX_CATEGORY.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CTX_CATEGORY.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        List<Condition> conditions(ContextCategoryListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            if (StringUtils.hasLength(filterCriteria.name())) {
                conditions.addAll(contains(filterCriteria.name(), CTX_CATEGORY.NAME));
            }
            if (StringUtils.hasLength(filterCriteria.description())) {
                conditions.addAll(contains(filterCriteria.description(), CTX_CATEGORY.DESCRIPTION));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP.lessThan(
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
                        field = CTX_CATEGORY.NAME;
                        break;

                    case "description":
                        field = CTX_CATEGORY.DESCRIPTION;
                        break;

                    case "lastUpdateTimestamp":
                        field = CTX_CATEGORY.LAST_UPDATE_TIMESTAMP;
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

        List<ContextCategoryListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        RecordMapper<org.jooq.Record, ContextCategoryListEntryRecord> mapper() {
            return record -> {
                ContextCategoryId contextCategoryId = new ContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger());
                return new ContextCategoryListEntryRecord(
                        contextCategoryId,
                        new Guid(record.get(CTX_CATEGORY.GUID)),
                        record.get(CTX_CATEGORY.NAME),
                        record.get(CTX_CATEGORY.DESCRIPTION),
                        isUsed(contextCategoryId),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CTX_CATEGORY.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CTX_CATEGORY.LAST_UPDATE_TIMESTAMP))
                        )
                );
            };
        }
    }
}
