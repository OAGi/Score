package org.oagi.score.gateway.http.api.context_management.business_context.repository.jooq;

import org.jooq.*;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.*;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextListFilterCriteria;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextSummaryListFilterCriteria;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;
import static org.springframework.util.StringUtils.hasLength;


public class JooqBusinessContextQueryRepository extends JooqBaseRepository implements BusinessContextQueryRepository {

    public JooqBusinessContextQueryRepository(DSLContext dslContext, ScoreUser requester,
                                              RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList() {

        var queryBuilder = new GetBusinessContextSummaryQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList(TopLevelAsbiepId topLevelAsbiepId) {

        if (topLevelAsbiepId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetBusinessContextSummaryQueryBuilder();
        return queryBuilder.select()
                .join(BIZ_CTX_ASSIGNMENT).on(BIZ_CTX.BIZ_CTX_ID.eq(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID))
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList(
            BusinessContextSummaryListFilterCriteria filterCriteria) {

        var queryBuilder = new GetBusinessContextSummaryQueryBuilder();
        var select = queryBuilder.select();
        SelectConditionStep<?> where;
        if (filterCriteria != null && filterCriteria.topLevelAsbiepId() != null) {
            where = select
                    .join(BIZ_CTX_ASSIGNMENT).on(BIZ_CTX.BIZ_CTX_ID.eq(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID))
                    .where(queryBuilder.conditions(filterCriteria));
        } else {
            where = select
                    .where(queryBuilder.conditions(filterCriteria));
        }
        return where.fetch(queryBuilder.mapper());
    }

    private class GetBusinessContextSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            BIZ_CTX.BIZ_CTX_ID,
                            BIZ_CTX.GUID,
                            BIZ_CTX.NAME)
                    .from(BIZ_CTX);
        }

        List<Condition> conditions(BusinessContextSummaryListFilterCriteria filterCriteria) {
            if (filterCriteria == null) {
                return Collections.emptyList();
            }

            List<Condition> conditions = new ArrayList<>();

            if (hasLength(filterCriteria.name())) {
                conditions.add(or(Arrays.asList(filterCriteria.name().split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e))
                        .map(e -> and(contains(e, BIZ_CTX.NAME)))
                        .collect(Collectors.toList())));
            }
            if (filterCriteria.businessContextIdList() != null && !filterCriteria.businessContextIdList().isEmpty()) {
                conditions.add(BIZ_CTX.BIZ_CTX_ID.in(valueOf(filterCriteria.businessContextIdList())));
            }
            if (filterCriteria.topLevelAsbiepId() != null) {
                conditions.add(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(valueOf(filterCriteria.topLevelAsbiepId())));
            }

            additionalConditionsForTenant(conditions,
                    filterCriteria.tenantEnabled(), null, false,
                    false, Collections.emptyList());

            return conditions;
        }

        RecordMapper<org.jooq.Record, BusinessContextSummaryRecord> mapper() {
            return record -> new BusinessContextSummaryRecord(
                    new BusinessContextId(record.get(BIZ_CTX.BIZ_CTX_ID).toBigInteger()),
                    new Guid(record.get(BIZ_CTX.GUID)),
                    record.get(BIZ_CTX.NAME)
            );
        }
    }

    @Override
    public BusinessContextDetailsRecord getBusinessContextDetails(
            BusinessContextId businessContextId) {
        var queryBuilder = new GetBusinessContextDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BIZ_CTX.BIZ_CTX_ID.eq(valueOf(businessContextId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetBusinessContextDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BIZ_CTX.BIZ_CTX_ID,
                            BIZ_CTX.GUID,
                            BIZ_CTX.NAME,
                            BIZ_CTX.CREATION_TIMESTAMP,
                            BIZ_CTX.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(BIZ_CTX)
                    .join(creatorTable()).on(BIZ_CTX.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BIZ_CTX.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, BusinessContextDetailsRecord> mapper() {
            return record -> {
                BusinessContextId businessContextId = new BusinessContextId(record.get(BIZ_CTX.BIZ_CTX_ID).toBigInteger());
                return new BusinessContextDetailsRecord(businessContextId,
                        new Guid(record.get(BIZ_CTX.GUID)), record.get(BIZ_CTX.NAME), isUsed(businessContextId),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BIZ_CTX.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BIZ_CTX.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    private boolean isUsed(BusinessContextId businessContextId) {
        return dslContext().selectCount().from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(valueOf(businessContextId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public List<BusinessContextValueRecord> getBusinessContextValueList(
            BusinessContextId businessContextId) {

        var queryBuilder = new GetBusinessContextValueQueryBuilder();
        return queryBuilder.select()
                .where(BIZ_CTX.BIZ_CTX_ID.eq(valueOf(businessContextId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetBusinessContextValueQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID,
                            BIZ_CTX.BIZ_CTX_ID,
                            CTX_CATEGORY.CTX_CATEGORY_ID,
                            CTX_CATEGORY.NAME.as("context_category_name"),
                            CTX_SCHEME.CTX_SCHEME_ID,
                            CTX_SCHEME.SCHEME_NAME.as("context_scheme_name"),
                            CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                            CTX_SCHEME_VALUE.VALUE.as("context_scheme_value"),
                            CTX_SCHEME_VALUE.MEANING.as("context_scheme_value_meaning"))
                    .from(BIZ_CTX_VALUE)
                    .join(BIZ_CTX).on(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID))
                    .join(CTX_SCHEME_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.eq(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID))
                    .join(CTX_SCHEME).on(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(CTX_SCHEME.CTX_SCHEME_ID))
                    .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.eq(CTX_CATEGORY.CTX_CATEGORY_ID));
        }

        RecordMapper<org.jooq.Record, BusinessContextValueRecord> mapper() {
            return record -> new BusinessContextValueRecord(
                    new BusinessContextValueId(record.get(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID).toBigInteger()),
                    new BusinessContextId(record.get(BIZ_CTX.BIZ_CTX_ID).toBigInteger()),
                    new ContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger()),
                    record.get(CTX_CATEGORY.NAME.as("context_category_name")),
                    new ContextSchemeId(record.get(CTX_SCHEME.CTX_SCHEME_ID).toBigInteger()),
                    record.get(CTX_SCHEME.SCHEME_NAME.as("context_scheme_name")),
                    new ContextSchemeValueId(record.get(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID).toBigInteger()),
                    record.get(CTX_SCHEME_VALUE.VALUE.as("context_scheme_value")),
                    record.get(CTX_SCHEME_VALUE.MEANING.as("context_scheme_value_meaning"))
            );
        }
    }

    @Override
    public ResultAndCount<BusinessContextListEntryRecord> getBusinessContextList(
            BusinessContextListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetBusinessContextListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<BusinessContextListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetBusinessContextListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BIZ_CTX.BIZ_CTX_ID,
                            BIZ_CTX.GUID,
                            BIZ_CTX.NAME,
                            BIZ_CTX.CREATION_TIMESTAMP,
                            BIZ_CTX.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(BIZ_CTX)
                    .join(creatorTable()).on(BIZ_CTX.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BIZ_CTX.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(TENANT_BUSINESS_CTX).on(BIZ_CTX.BIZ_CTX_ID.eq(TENANT_BUSINESS_CTX.BIZ_CTX_ID));
        }

        List<Condition> conditions(BusinessContextListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            if (StringUtils.hasLength(filterCriteria.name())) {
                conditions.add(or(Arrays.asList(filterCriteria.name().split(",")).stream().map(e -> e.trim())
                        .filter(e -> StringUtils.hasLength(e))
                        .map(e -> and(contains(e, BIZ_CTX.NAME)))
                        .collect(Collectors.toList())));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(BIZ_CTX.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(BIZ_CTX.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }

            additionalConditionsForTenant(conditions,
                    filterCriteria.tenantEnabled(), filterCriteria.tenantId(), filterCriteria.notConnectedToTenant(),
                    filterCriteria.bieEditing(), filterCriteria.userTenantIdList());

            return conditions;
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
                    case "name":
                        field = BIZ_CTX.NAME;
                        break;

                    case "lastUpdateTimestamp":
                        field = BIZ_CTX.LAST_UPDATE_TIMESTAMP;
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

        List<BusinessContextListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        RecordMapper<org.jooq.Record, BusinessContextListEntryRecord> mapper() {
            return record -> {
                BusinessContextId businessContextId = new BusinessContextId(record.get(BIZ_CTX.BIZ_CTX_ID).toBigInteger());
                return new BusinessContextListEntryRecord(businessContextId,
                        new Guid(record.get(BIZ_CTX.GUID)), record.get(BIZ_CTX.NAME), null,
                        isUsed(businessContextId),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BIZ_CTX.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BIZ_CTX.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    private List<Condition> additionalConditionsForTenant(
            List<Condition> conditions,
            boolean tenantEnabled, TenantId tenantId, boolean notConnectedToTenant,
            boolean bieEditing, Collection<TenantId> userTenantIdList) {

        // for tenant management
        if (tenantEnabled) {
            if (tenantId != null && !notConnectedToTenant) {
                conditions.add(TENANT_BUSINESS_CTX.TENANT_ID.eq(valueOf(tenantId)));
            }

            if (tenantId != null && notConnectedToTenant) {
                conditions.add(BIZ_CTX.BIZ_CTX_ID.notIn(dslContext().select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                        .from(TENANT_BUSINESS_CTX).where(TENANT_BUSINESS_CTX.TENANT_ID.eq(valueOf(tenantId)))));
            }
        }

        // for editing bie
        if (tenantEnabled && bieEditing) {
            conditions.add(BIZ_CTX.BIZ_CTX_ID.in(
                    dslContext().select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
                            .from(TENANT_BUSINESS_CTX)
                            .where(TENANT_BUSINESS_CTX.TENANT_ID.in(valueOf(userTenantIdList)))));
        }

        return conditions;
    }
}
