package org.oagi.score.gateway.http.api.context_management.context_scheme.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.*;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.criteria.ContextSchemeListFilterCriteria;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;

public class JooqContextSchemeQueryRepository extends JooqBaseRepository implements ContextSchemeQueryRepository {

    public JooqContextSchemeQueryRepository(DSLContext dslContext, ScoreUser requester,
                                            RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<ContextSchemeSummaryRecord> getContextSchemeSummaryList() {

        var queryBuilder = new GetContextSchemeSummaryQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<ContextSchemeSummaryRecord> getContextSchemeSummaryList(ContextCategoryId contextCategoryId) {

        var queryBuilder = new GetContextSchemeSummaryQueryBuilder();
        return queryBuilder.select()
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(valueOf(contextCategoryId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetContextSchemeSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            CTX_SCHEME.CTX_SCHEME_ID,
                            CTX_SCHEME.GUID,
                            CTX_SCHEME.SCHEME_ID,
                            CTX_SCHEME.SCHEME_NAME,
                            CTX_SCHEME.SCHEME_AGENCY_ID,
                            CTX_SCHEME.SCHEME_VERSION_ID,
                            CTX_SCHEME.DESCRIPTION,

                            CTX_CATEGORY.CTX_CATEGORY_ID,
                            CTX_CATEGORY.GUID.as("ctx_category_guid"),
                            CTX_CATEGORY.NAME.as("ctx_category_name"),
                            CTX_CATEGORY.DESCRIPTION.as("ctx_category_description"))
                    .from(CTX_SCHEME)
                    .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.eq(CTX_CATEGORY.CTX_CATEGORY_ID));
        }

        RecordMapper<org.jooq.Record, ContextSchemeSummaryRecord> mapper() {
            return record -> new ContextSchemeSummaryRecord(
                    new ContextSchemeId(record.get(CTX_SCHEME.CTX_SCHEME_ID).toBigInteger()),
                    new Guid(record.get(CTX_SCHEME.GUID)),
                    new ContextCategorySummaryRecord(
                            new ContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger()),
                            new Guid(record.get(CTX_CATEGORY.GUID.as("ctx_category_guid"))),
                            record.get(CTX_CATEGORY.NAME.as("ctx_category_name")),
                            record.get(CTX_CATEGORY.DESCRIPTION.as("ctx_category_description"))
                    ),
                    record.get(CTX_SCHEME.SCHEME_ID),
                    record.get(CTX_SCHEME.SCHEME_NAME),
                    record.get(CTX_SCHEME.SCHEME_AGENCY_ID),
                    record.get(CTX_SCHEME.SCHEME_VERSION_ID),
                    record.get(CTX_SCHEME.DESCRIPTION)
            );
        }
    }

    @Override
    public ContextSchemeDetailsRecord getContextSchemeDetails(ContextSchemeId contextSchemeId) {
        var queryBuilder = new GetContextSchemeDetailsQueryBuilder();
        return queryBuilder.select()
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(valueOf(contextSchemeId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetContextSchemeDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CTX_SCHEME.CTX_SCHEME_ID,
                            CTX_SCHEME.GUID,
                            CTX_SCHEME.SCHEME_ID,
                            CTX_SCHEME.SCHEME_NAME,
                            CTX_SCHEME.SCHEME_AGENCY_ID,
                            CTX_SCHEME.SCHEME_VERSION_ID,
                            CTX_SCHEME.DESCRIPTION,
                            CTX_SCHEME.CREATION_TIMESTAMP,
                            CTX_SCHEME.LAST_UPDATE_TIMESTAMP,

                            CODE_LIST.CODE_LIST_ID,
                            CODE_LIST.GUID.as("code_list_guid"),
                            CODE_LIST.NAME.as("code_list_name"),
                            CODE_LIST.LIST_ID.as("code_list_list_id"),
                            CODE_LIST.VERSION_ID.as("code_list_version_id"),
                            CODE_LIST.IS_DEPRECATED.as("code_list_deprecated"),
                            CODE_LIST.STATE.as("code_list_state"),

                            CTX_CATEGORY.CTX_CATEGORY_ID,
                            CTX_CATEGORY.GUID.as("ctx_category_guid"),
                            CTX_CATEGORY.NAME.as("ctx_category_name"),
                            CTX_CATEGORY.DESCRIPTION.as("ctx_category_description")
                    ), creatorFields(), updaterFields()))
                    .from(CTX_SCHEME)
                    .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.eq(CTX_CATEGORY.CTX_CATEGORY_ID))
                    .join(creatorTable()).on(CTX_SCHEME.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CTX_SCHEME.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(CODE_LIST).on(CTX_SCHEME.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID));
        }

        RecordMapper<org.jooq.Record, ContextSchemeDetailsRecord> mapper() {
            return record -> {
                ContextSchemeId contextSchemeId = new ContextSchemeId(record.get(CTX_SCHEME.CTX_SCHEME_ID).toBigInteger());
                return new ContextSchemeDetailsRecord(
                        contextSchemeId,
                        new Guid(record.get(CTX_SCHEME.GUID)),
                        new ContextCategorySummaryRecord(
                                new ContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger()),
                                new Guid(record.get(CTX_CATEGORY.GUID.as("ctx_category_guid"))),
                                record.get(CTX_CATEGORY.NAME.as("ctx_category_name")),
                                record.get(CTX_CATEGORY.DESCRIPTION.as("ctx_category_description"))
                        ),
                        record.get(CTX_SCHEME.SCHEME_ID),
                        record.get(CTX_SCHEME.SCHEME_NAME),
                        record.get(CTX_SCHEME.SCHEME_AGENCY_ID),
                        record.get(CTX_SCHEME.SCHEME_VERSION_ID),
                        record.get(CTX_SCHEME.DESCRIPTION),
                        isUsed(contextSchemeId),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CTX_SCHEME.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CTX_SCHEME.LAST_UPDATE_TIMESTAMP))
                        )
                );
            };
        }
    }

    private boolean isUsed(ContextSchemeId contextSchemeId) {
        return dslContext().selectCount()
                .from(BIZ_CTX_VALUE)
                .join(CTX_SCHEME_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.eq(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID))
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(valueOf(contextSchemeId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public ResultAndCount<ContextSchemeListEntryRecord> getContextSchemeList(
            ContextSchemeListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetContextSchemeListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<ContextSchemeListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetContextSchemeListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CTX_SCHEME.CTX_SCHEME_ID,
                            CTX_SCHEME.GUID,
                            CTX_SCHEME.SCHEME_ID,
                            CTX_SCHEME.SCHEME_NAME,
                            CTX_SCHEME.SCHEME_AGENCY_ID,
                            CTX_SCHEME.SCHEME_VERSION_ID,
                            CTX_SCHEME.DESCRIPTION,
                            CTX_SCHEME.CREATION_TIMESTAMP,
                            CTX_SCHEME.LAST_UPDATE_TIMESTAMP,

                            CODE_LIST.CODE_LIST_ID,
                            CODE_LIST.GUID.as("code_list_guid"),
                            CODE_LIST.NAME.as("code_list_name"),
                            CODE_LIST.LIST_ID.as("code_list_list_id"),
                            CODE_LIST.VERSION_ID.as("code_list_version_id"),
                            CODE_LIST.IS_DEPRECATED.as("code_list_deprecated"),
                            CODE_LIST.STATE.as("code_list_state"),

                            CTX_CATEGORY.CTX_CATEGORY_ID,
                            CTX_CATEGORY.GUID.as("ctx_category_guid"),
                            CTX_CATEGORY.NAME.as("ctx_category_name"),
                            CTX_CATEGORY.DESCRIPTION.as("ctx_category_description")
                    ), creatorFields(), updaterFields()))
                    .from(CTX_SCHEME)
                    .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.eq(CTX_CATEGORY.CTX_CATEGORY_ID))
                    .join(creatorTable()).on(CTX_SCHEME.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CTX_SCHEME.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(CODE_LIST).on(CTX_SCHEME.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID));
        }

        List<Condition> conditions(ContextSchemeListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            if (StringUtils.hasLength(filterCriteria.name())) {
                conditions.addAll(contains(filterCriteria.name(), CTX_SCHEME.SCHEME_NAME));
            }
            if (StringUtils.hasLength(filterCriteria.description())) {
                conditions.addAll(contains(filterCriteria.description(), CTX_SCHEME.DESCRIPTION));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(CTX_SCHEME.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(CTX_SCHEME.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "schemeName":
                        field = CTX_SCHEME.SCHEME_NAME;
                        break;

                    case "contextCategory":
                    case "contextCategoryName":
                        field = CTX_CATEGORY.NAME.as("ctx_category_name");
                        break;

                    case "schemeId":
                        field = CTX_SCHEME.SCHEME_ID;
                        break;

                    case "agencyId":
                    case "schemeAgencyId":
                        field = CTX_SCHEME.SCHEME_AGENCY_ID;
                        break;

                    case "version":
                    case "versionId":
                    case "schemeVersionId":
                        field = CTX_SCHEME.SCHEME_VERSION_ID;
                        break;

                    case "description":
                        field = CTX_SCHEME.DESCRIPTION;
                        break;

                    case "lastUpdateTimestamp":
                        field = CTX_SCHEME.LAST_UPDATE_TIMESTAMP;
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

        List<ContextSchemeListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        RecordMapper<org.jooq.Record, ContextSchemeListEntryRecord> mapper() {
            return record -> {
                ContextSchemeId contextSchemeId = new ContextSchemeId(record.get(CTX_SCHEME.CTX_SCHEME_ID).toBigInteger());
                return new ContextSchemeListEntryRecord(
                        contextSchemeId,
                        new Guid(record.get(CTX_SCHEME.GUID)),
                        new ContextCategorySummaryRecord(
                                new ContextCategoryId(record.get(CTX_CATEGORY.CTX_CATEGORY_ID).toBigInteger()),
                                new Guid(record.get(CTX_CATEGORY.GUID.as("ctx_category_guid"))),
                                record.get(CTX_CATEGORY.NAME.as("ctx_category_name")),
                                record.get(CTX_CATEGORY.DESCRIPTION.as("ctx_category_description"))
                        ),
                        record.get(CTX_SCHEME.SCHEME_ID),
                        record.get(CTX_SCHEME.SCHEME_NAME),
                        record.get(CTX_SCHEME.SCHEME_AGENCY_ID),
                        record.get(CTX_SCHEME.SCHEME_VERSION_ID),
                        record.get(CTX_SCHEME.DESCRIPTION),
                        isUsed(contextSchemeId),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CTX_SCHEME.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CTX_SCHEME.LAST_UPDATE_TIMESTAMP))
                        )
                );
            };
        }
    }

    @Override
    public List<ContextSchemeValueDetailsRecord> getContextSchemeValueList(ContextSchemeId contextSchemeId) {

        var queryBuilder = new GetContextSchemeValueDetailsQueryBuilder();
        return queryBuilder.select()
                .where(CTX_SCHEME.CTX_SCHEME_ID.eq(valueOf(contextSchemeId)))
                .groupBy(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                .fetch(queryBuilder.mapper());
    }

    private class GetContextSchemeValueDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(
                            CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                            CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID,
                            CTX_SCHEME_VALUE.GUID,
                            CTX_SCHEME_VALUE.VALUE,
                            CTX_SCHEME_VALUE.MEANING,
                            coalesce(count(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID), 0).as("used")
                    )
                    .from(CTX_SCHEME_VALUE)
                    .join(CTX_SCHEME).on(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(CTX_SCHEME.CTX_SCHEME_ID))
                    .leftJoin(BIZ_CTX_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.eq(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID));
        }

        RecordMapper<org.jooq.Record, ContextSchemeValueDetailsRecord> mapper() {
            return record -> new ContextSchemeValueDetailsRecord(
                    new ContextSchemeValueId(record.get(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID).toBigInteger()),
                    new ContextSchemeId(record.get(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID).toBigInteger()),
                    new Guid(record.get(CTX_SCHEME_VALUE.GUID)),
                    record.get(CTX_SCHEME_VALUE.VALUE),
                    record.get(CTX_SCHEME_VALUE.MEANING),
                    record.get(coalesce(count(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID), 0).as("used")) > 0
            );
        }
    }

    @Override
    public List<ContextSchemeValueSummaryRecord> getContextSchemeValueSummaryList() {

        var queryBuilder = new GetContextSchemeValueSummaryQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public ContextSchemeValueSummaryRecord getContextSchemeValueSummary(ContextSchemeValueId contextSchemeValueId) {

        if (contextSchemeValueId == null) {
            return null;
        }

        var queryBuilder = new GetContextSchemeValueSummaryQueryBuilder();
        return queryBuilder.select()
                .where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.eq(valueOf(contextSchemeValueId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetContextSchemeValueSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(
                            CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID,
                            CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID,
                            CTX_SCHEME_VALUE.GUID,
                            CTX_SCHEME_VALUE.VALUE,
                            CTX_SCHEME_VALUE.MEANING
                    )
                    .from(CTX_SCHEME_VALUE)
                    .join(CTX_SCHEME).on(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.eq(CTX_SCHEME.CTX_SCHEME_ID))
                    .leftJoin(BIZ_CTX_VALUE).on(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.eq(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID));
        }

        RecordMapper<org.jooq.Record, ContextSchemeValueSummaryRecord> mapper() {
            return record -> new ContextSchemeValueSummaryRecord(
                    new ContextSchemeValueId(record.get(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID).toBigInteger()),
                    new ContextSchemeId(record.get(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID).toBigInteger()),
                    new Guid(record.get(CTX_SCHEME_VALUE.GUID)),
                    record.get(CTX_SCHEME_VALUE.VALUE),
                    record.get(CTX_SCHEME_VALUE.MEANING)
            );
        }
    }

    @Override
    public boolean hasDuplicate(String schemeId, String schemeAgencyId, String schemeVersionId) {

        return dslContext().selectCount()
                .from(CTX_SCHEME)
                .where(and(
                        CTX_SCHEME.SCHEME_ID.eq(schemeId),
                        CTX_SCHEME.SCHEME_AGENCY_ID.eq(schemeAgencyId),
                        CTX_SCHEME.SCHEME_VERSION_ID.eq(schemeVersionId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateExcludingCurrent(
            ContextSchemeId contextSchemeId, String schemeId, String schemeAgencyId, String schemeVersionId) {

        return dslContext().selectCount()
                .from(CTX_SCHEME)
                .where(and(
                        CTX_SCHEME.CTX_SCHEME_ID.notEqual(valueOf(contextSchemeId)),
                        CTX_SCHEME.SCHEME_ID.eq(schemeId),
                        CTX_SCHEME.SCHEME_AGENCY_ID.eq(schemeAgencyId),
                        CTX_SCHEME.SCHEME_VERSION_ID.eq(schemeVersionId)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateName(String schemeName, String schemeId, String schemeAgencyId, String schemeVersionId) {

        return dslContext().selectCount()
                .from(CTX_SCHEME)
                .where(and(
                        CTX_SCHEME.SCHEME_ID.eq(schemeId),
                        CTX_SCHEME.SCHEME_AGENCY_ID.eq(schemeAgencyId),
                        CTX_SCHEME.SCHEME_VERSION_ID.notEqual(schemeVersionId),
                        CTX_SCHEME.SCHEME_NAME.notEqual(schemeName)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasDuplicateNameExcludingCurrent(
            ContextSchemeId contextSchemeId, String schemeName, String schemeId, String schemeAgencyId, String schemeVersionId) {

        return dslContext().selectCount()
                .from(CTX_SCHEME)
                .where(and(
                        CTX_SCHEME.CTX_SCHEME_ID.notEqual(valueOf(contextSchemeId)),
                        CTX_SCHEME.SCHEME_ID.eq(schemeId),
                        CTX_SCHEME.SCHEME_AGENCY_ID.eq(schemeAgencyId),
                        CTX_SCHEME.SCHEME_VERSION_ID.notEqual(schemeVersionId),
                        CTX_SCHEME.SCHEME_NAME.notEqual(schemeName)
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }
}
