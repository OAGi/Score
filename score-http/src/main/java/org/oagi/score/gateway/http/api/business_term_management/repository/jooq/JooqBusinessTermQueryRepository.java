package org.oagi.score.gateway.http.api.business_term_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.business_term_management.model.*;
import org.oagi.score.gateway.http.api.business_term_management.repository.BusinessTermQueryRepository;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AsbieBbieListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.AssignedBusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.api.business_term_management.repository.criteria.BusinessTermListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Routines.levenshtein;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;
import static org.springframework.util.StringUtils.hasLength;

public class JooqBusinessTermQueryRepository
        extends JooqBaseRepository
        implements BusinessTermQueryRepository {

    public JooqBusinessTermQueryRepository(DSLContext dslContext,
                                           ScoreUser requester,
                                           RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ResultAndCount<BusinessTermListEntryRecord> getBusinessTermList(
            BusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetBusinessTermListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<BusinessTermListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetBusinessTermListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BUSINESS_TERM.BUSINESS_TERM_ID,
                            BUSINESS_TERM.GUID,
                            BUSINESS_TERM.BUSINESS_TERM_,
                            BUSINESS_TERM.DEFINITION,
                            BUSINESS_TERM.COMMENT,
                            BUSINESS_TERM.EXTERNAL_REF_ID,
                            BUSINESS_TERM.EXTERNAL_REF_URI,
                            BUSINESS_TERM.CREATION_TIMESTAMP,
                            BUSINESS_TERM.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(BUSINESS_TERM)
                    .join(creatorTable()).on(BUSINESS_TERM.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BUSINESS_TERM.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions(BusinessTermListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();

            if (StringUtils.hasLength(filterCriteria.businessTerm())) {
                conditions.addAll(contains(filterCriteria.businessTerm(), BUSINESS_TERM.BUSINESS_TERM_));
            }
            if (StringUtils.hasLength(filterCriteria.externalReferenceUri())) {
                conditions.addAll(contains(filterCriteria.externalReferenceUri(), BUSINESS_TERM.EXTERNAL_REF_URI));
            }
            if (StringUtils.hasLength(filterCriteria.externalReferenceId())) {
                conditions.addAll(contains(filterCriteria.externalReferenceId(), BUSINESS_TERM.EXTERNAL_REF_ID));
            }
            if (StringUtils.hasLength(filterCriteria.definition())) {
                conditions.addAll(contains(filterCriteria.definition(), BUSINESS_TERM.DEFINITION));
            }
            if (StringUtils.hasLength(filterCriteria.comment())) {
                conditions.addAll(contains(filterCriteria.comment(), BUSINESS_TERM.COMMENT));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "businessTerm":
                        field = BUSINESS_TERM.BUSINESS_TERM_;
                        break;

                    case "externalReferenceUri":
                        field = BUSINESS_TERM.EXTERNAL_REF_URI;
                        break;

                    case "externalReferenceId":
                        field = BUSINESS_TERM.EXTERNAL_REF_ID;
                        break;

                    case "definition":
                        field = BUSINESS_TERM.DEFINITION;
                        break;

                    case "comment":
                        field = BUSINESS_TERM.COMMENT;
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

        public List<BusinessTermListEntryRecord> fetch(
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

        private RecordMapper<org.jooq.Record, BusinessTermListEntryRecord> mapper() {
            return record -> new BusinessTermListEntryRecord(
                    new BusinessTermId(record.get(BUSINESS_TERM.BUSINESS_TERM_ID).toBigInteger()),
                    new Guid(record.get(BUSINESS_TERM.GUID)),
                    record.get(BUSINESS_TERM.BUSINESS_TERM_),
                    record.get(BUSINESS_TERM.DEFINITION),
                    record.get(BUSINESS_TERM.COMMENT),
                    record.get(BUSINESS_TERM.EXTERNAL_REF_ID),
                    record.get(BUSINESS_TERM.EXTERNAL_REF_URI),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(BUSINESS_TERM.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }
    }

    @Override
    public ResultAndCount<BusinessTermListEntryRecord> getBusinessTermListByAssignedBieList(
            BusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetBusinessTermListQueryBuilder();
        SelectOrderByStep where = null;
        if (!filterCriteria.byAssignedAsbieIdList().isEmpty()) {
            List<Condition> conditions = queryBuilder.conditions(filterCriteria);
            conditions.add(ASBIE_BIZTERM.ASBIE_ID.in(valueOf(filterCriteria.byAssignedAsbieIdList())));
            SelectOrderByStep asbieSelect = queryBuilder.select()
                    .join(ASCC_BIZTERM).on(ASCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID))
                    .join(ASBIE_BIZTERM).on(ASBIE_BIZTERM.ASCC_BIZTERM_ID.eq(ASCC_BIZTERM.ASCC_BIZTERM_ID))
                    .where(conditions);
            where = (where != null) ? where.union(asbieSelect) : asbieSelect;
        }
        if (!filterCriteria.byAssignedBbieIdList().isEmpty()) {
            List<Condition> conditions = queryBuilder.conditions(filterCriteria);
            conditions.add(BBIE_BIZTERM.BBIE_ID.in(valueOf(filterCriteria.byAssignedBbieIdList())));
            SelectOrderByStep bbieSelect = queryBuilder.select()
                    .join(BCC_BIZTERM).on(BCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID))
                    .join(BBIE_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                    .where(conditions);
            where = (where != null) ? where.union(bbieSelect) : bbieSelect;
        }
        int count = dslContext().fetchCount(where);
        List<BusinessTermListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    @Override
    public boolean checkUniqueness(BusinessTermId businessTermId, String businessTerm, String externalReferenceUri) {

        if (!hasLength(businessTerm)) {
            throw new IllegalArgumentException("`businessTerm` parameter must not be empty.");
        }

        if (!hasLength(externalReferenceUri)) {
            throw new IllegalArgumentException("`externalReferenceUri` parameter must not be empty.");
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(BUSINESS_TERM.BUSINESS_TERM_.eq(businessTerm));
        conditions.add(BUSINESS_TERM.EXTERNAL_REF_URI.eq(externalReferenceUri));
        if (businessTermId != null) {
            conditions.add(BUSINESS_TERM.BUSINESS_TERM_ID.ne(valueOf(businessTermId)));
        }

        return dslContext().selectCount()
                .from(BUSINESS_TERM)
                .where(conditions)
                .fetchOneInto(Integer.class) == 0;
    }

    @Override
    public boolean checkNameUniqueness(BusinessTermId businessTermId, String businessTerm) {

        if (!hasLength(businessTerm)) {
            throw new IllegalArgumentException("`businessTerm` parameter must not be empty.");
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(BUSINESS_TERM.BUSINESS_TERM_.eq(businessTerm));
        if (businessTermId != null) {
            conditions.add(BUSINESS_TERM.BUSINESS_TERM_ID.ne(valueOf(businessTermId)));
        }

        return dslContext().selectCount()
                .from(BUSINESS_TERM)
                .where(conditions)
                .fetchOneInto(Integer.class) == 0;
    }

    @Override
    public BusinessTermDetailsRecord getBusinessTermDetails(BusinessTermId businessTermId) {

        if (businessTermId == null) {
            return null;
        }

        var queryBuilder = new GetBusinessTermDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BUSINESS_TERM.BUSINESS_TERM_ID.eq(valueOf(businessTermId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetBusinessTermDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BUSINESS_TERM.BUSINESS_TERM_ID,
                            BUSINESS_TERM.GUID,
                            BUSINESS_TERM.BUSINESS_TERM_,
                            BUSINESS_TERM.DEFINITION,
                            BUSINESS_TERM.COMMENT,
                            BUSINESS_TERM.EXTERNAL_REF_ID,
                            BUSINESS_TERM.EXTERNAL_REF_URI,
                            BUSINESS_TERM.CREATION_TIMESTAMP,
                            BUSINESS_TERM.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(BUSINESS_TERM)
                    .join(creatorTable()).on(BUSINESS_TERM.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BUSINESS_TERM.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        private RecordMapper<org.jooq.Record, BusinessTermDetailsRecord> mapper() {
            return record -> new BusinessTermDetailsRecord(
                    new BusinessTermId(record.get(BUSINESS_TERM.BUSINESS_TERM_ID).toBigInteger()),
                    new Guid(record.get(BUSINESS_TERM.GUID)),
                    record.get(BUSINESS_TERM.BUSINESS_TERM_),
                    record.get(BUSINESS_TERM.DEFINITION),
                    record.get(BUSINESS_TERM.COMMENT),
                    record.get(BUSINESS_TERM.EXTERNAL_REF_ID),
                    record.get(BUSINESS_TERM.EXTERNAL_REF_URI),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(BUSINESS_TERM.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }
    }

    @Override
    public ResultAndCount<AssignedBusinessTermListEntryRecord> getAssignedBusinessTermList(
            AssignedBusinessTermListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetAssignedBusinessTermListQueryBuilder(filterCriteria);
        var where = queryBuilder.select();
        int count = dslContext().fetchCount(where);
        List<AssignedBusinessTermListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetAssignedBusinessTermListQueryBuilder {

        private AssignedBusinessTermListFilterCriteria filterCriteria;

        public GetAssignedBusinessTermListQueryBuilder(AssignedBusinessTermListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOrderByStep<? extends org.jooq.Record> select() {

            SelectOrderByStep select = null;

            if (filterCriteria.bieTypeList() == null || filterCriteria.bieTypeList().isEmpty() ||
                    filterCriteria.bieTypeList().contains("ASBIE")) {
                var queryBuilder = new AsbieAssignedBusinessTermListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(queryBuilder.conditions());
                select = (select != null) ? select.union(where) : where;
            }
            if (filterCriteria.bieTypeList() == null || filterCriteria.bieTypeList().isEmpty() ||
                    filterCriteria.bieTypeList().contains("BBIE")) {
                var queryBuilder = new BbieAssignedBusinessTermListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(queryBuilder.conditions());
                select = (select != null) ? select.union(where) : where;
            }

            return select;
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
                    case "bieType":
                        field = field("bie_type");
                        break;

                    case "bieDen":
                        field = field("den");
                        break;

                    case "businessTerm":
                        field = BUSINESS_TERM.BUSINESS_TERM_;
                        break;

                    case "primaryIndicator":
                        field = field("primary_indicator");
                        break;

                    case "externalReferenceUri":
                        field = BUSINESS_TERM.EXTERNAL_REF_URI;
                        break;

                    case "externalReferenceId":
                        field = BUSINESS_TERM.EXTERNAL_REF_ID;
                        break;

                    case "typeCode":
                        field = field("type_code");
                        break;

                    case "lastUpdateTimestamp":
                        field = field("last_update_timestamp");
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

        public List<AssignedBusinessTermListEntryRecord> fetch(
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

        private RecordMapper<org.jooq.Record, AssignedBusinessTermListEntryRecord> mapper() {
            return record -> {
                return new AssignedBusinessTermListEntryRecord(
                        new BusinessTermId(record.get("assigned_business_term_id", BigInteger.class)),
                        record.getValue("bie_id", BigInteger.class),
                        record.getValue("bie_type", String.class),
                        record.getValue("den", String.class),
                        (byte) 1 == record.getValue("primary_indicator", Byte.class),
                        record.getValue("type_code", String.class),
                        record.getValue("business_term_id", BigInteger.class),
                        record.getValue("business_term", String.class),
                        record.getValue("external_reference_uri", String.class),
                        Collections.emptyList(),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.getValue("creation_timestamp", LocalDateTime.class))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.getValue("last_update_timestamp", LocalDateTime.class))
                        )
                );
            };
        }
    }

    private class AsbieAssignedBusinessTermListQueryBuilder {

        private AssignedBusinessTermListFilterCriteria filterCriteria;

        public AsbieAssignedBusinessTermListQueryBuilder(AssignedBusinessTermListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            inline("ASBIE").as("bie_type"),
                            ASBIE_BIZTERM.ASBIE_BIZTERM_ID.as("assigned_business_term_id"),
                            ASBIE_BIZTERM.ASBIE_ID.as("bie_id"),
                            ASBIE_BIZTERM.PRIMARY_INDICATOR.as("primary_indicator"),
                            ASBIE_BIZTERM.TYPE_CODE.as("type_code"),
                            ASCC_MANIFEST.DEN.as("den"),
                            BUSINESS_TERM.BUSINESS_TERM_ID,
                            BUSINESS_TERM.BUSINESS_TERM_,
                            BUSINESS_TERM.EXTERNAL_REF_URI.as("external_reference_uri"),
                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            ASBIE_BIZTERM.CREATION_TIMESTAMP.as("creation_timestamp"),
                            ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.as("last_update_timestamp")
                    ), creatorFields(), updaterFields()))
                    .from(ASBIE_BIZTERM)
                    .join(ASCC_BIZTERM).on(ASBIE_BIZTERM.ASCC_BIZTERM_ID.eq(ASCC_BIZTERM.ASCC_BIZTERM_ID))
                    .join(ASCC).on(ASCC_BIZTERM.ASCC_ID.eq(ASCC.ASCC_ID))
                    .join(BUSINESS_TERM).on(and(
                            ASCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID)
                    ))
//               next 3 joins to get release information
                    .join(ASBIE).on(ASBIE_BIZTERM.ASBIE_ID.eq(ASBIE.ASBIE_ID))
                    .join(ASCC_MANIFEST).on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                    .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
//              next joins to get user information
                    .join(creatorTable()).on(ASBIE_BIZTERM.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(ASBIE_BIZTERM.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions() {
            List<Condition> conditions = new ArrayList<>();

            if (filterCriteria.assignedBusinessTermId() != null) {
                conditions.add(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(ULong.valueOf(filterCriteria.assignedBusinessTermId())));
            }
            if (hasLength(filterCriteria.businessTerm())) {
                conditions.addAll(contains(filterCriteria.businessTerm(), BUSINESS_TERM.BUSINESS_TERM_));
            }
            if (filterCriteria.bieId() != null) {
                conditions.add(ASBIE.ASBIE_ID.eq(ULong.valueOf(filterCriteria.bieId())));
            }
            if (hasLength(filterCriteria.bieDen())) {
                conditions.add(ASCC_MANIFEST.DEN.contains(filterCriteria.bieDen()));
            }
            if (filterCriteria.primaryIndicator() != null) {
                conditions.add(ASBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) (filterCriteria.primaryIndicator() ? 1 : 0)));
            }
            if (hasLength(filterCriteria.typeCode())) {
                conditions.add(ASBIE_BIZTERM.TYPE_CODE.contains(filterCriteria.typeCode()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            return conditions;
        }

    }

    private class BbieAssignedBusinessTermListQueryBuilder {

        private AssignedBusinessTermListFilterCriteria filterCriteria;

        public BbieAssignedBusinessTermListQueryBuilder(AssignedBusinessTermListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            inline("BBIE").as("bie_type"),
                            BBIE_BIZTERM.BBIE_BIZTERM_ID.as("assigned_business_term_id"),
                            BBIE_BIZTERM.BBIE_ID.as("bie_id"),
                            BBIE_BIZTERM.PRIMARY_INDICATOR.as("primary_indicator"),
                            BBIE_BIZTERM.TYPE_CODE.as("type_code"),
                            BCC_MANIFEST.DEN.as("den"),
                            BUSINESS_TERM.BUSINESS_TERM_ID,
                            BUSINESS_TERM.BUSINESS_TERM_,
                            BUSINESS_TERM.EXTERNAL_REF_URI.as("external_reference_uri"),
                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            BBIE_BIZTERM.CREATION_TIMESTAMP.as("creation_timestamp"),
                            BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.as("last_update_timestamp")
                    ), creatorFields(), updaterFields()))
                    .from(BBIE_BIZTERM)
                    .join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                    .join(BCC).on(BCC_BIZTERM.BCC_ID.eq(BCC.BCC_ID))
                    .join(BUSINESS_TERM).on(and(
                            BCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID)
                    ))
//               next 3 joins to get release information
                    .join(BBIE).on(BBIE_BIZTERM.BBIE_ID.eq(BBIE.BBIE_ID))
                    .join(BCC_MANIFEST).on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                    .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
//              next joins to get user information
                    .join(creatorTable()).on(BBIE_BIZTERM.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BBIE_BIZTERM.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        public List<Condition> conditions() {
            List<Condition> conditions = new ArrayList<>();

            if (filterCriteria.assignedBusinessTermId() != null) {
                conditions.add(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(ULong.valueOf(filterCriteria.assignedBusinessTermId())));
            }
            if (hasLength(filterCriteria.businessTerm())) {
                conditions.addAll(contains(filterCriteria.businessTerm(), BUSINESS_TERM.BUSINESS_TERM_));
            }
            if (filterCriteria.bieId() != null) {
                conditions.add(BBIE.BBIE_ID.eq(ULong.valueOf(filterCriteria.bieId())));
            }
            if (hasLength(filterCriteria.bieDen())) {
                conditions.add(BCC_MANIFEST.DEN.contains(filterCriteria.bieDen()));
            }
            if (filterCriteria.primaryIndicator() != null) {
                conditions.add(BBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) (filterCriteria.primaryIndicator() ? 1 : 0)));
            }
            if (hasLength(filterCriteria.typeCode())) {
                conditions.add(BBIE_BIZTERM.TYPE_CODE.contains(filterCriteria.typeCode()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            return conditions;
        }

    }

    @Override
    public boolean checkAssignmentUniqueness(
            AsbieId asbieId, BusinessTermId businessTermId, String typeCode, boolean primaryIndicator) {

        return dslContext().selectCount()
                .from(ASBIE_BIZTERM)
                .join(ASCC_BIZTERM).on(ASBIE_BIZTERM.ASCC_BIZTERM_ID.eq(ASCC_BIZTERM.ASCC_BIZTERM_ID))
                .where(and(
                        ASBIE_BIZTERM.ASBIE_ID.eq(valueOf(asbieId))),
                        ASCC_BIZTERM.BUSINESS_TERM_ID.eq(valueOf(businessTermId)),
                        ((hasLength(typeCode)) ?
                                ASBIE_BIZTERM.TYPE_CODE.eq(typeCode) :
                                or(ASBIE_BIZTERM.TYPE_CODE.isNull(), ASBIE_BIZTERM.TYPE_CODE.eq(""))),
                        ASBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) (primaryIndicator ? 1 : 0))
                )
                .fetchOneInto(Integer.class) == 0;
    }

    @Override
    public boolean checkAssignmentUniqueness(
            BbieId bbieId, BusinessTermId businessTermId, String typeCode, boolean primaryIndicator) {

        return dslContext().selectCount()
                .from(BBIE_BIZTERM)
                .join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                .where(and(
                        BBIE_BIZTERM.BBIE_ID.eq(valueOf(bbieId))),
                        BCC_BIZTERM.BUSINESS_TERM_ID.eq(valueOf(businessTermId)),
                        ((hasLength(typeCode)) ?
                                BBIE_BIZTERM.TYPE_CODE.eq(typeCode) :
                                or(BBIE_BIZTERM.TYPE_CODE.isNull(), BBIE_BIZTERM.TYPE_CODE.eq(""))),
                        BBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) (primaryIndicator ? 1 : 0))
                )
                .fetchOneInto(Integer.class) == 0;
    }

    @Override
    public AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(AsbieBusinessTermId asbieBusinessTermId) {

        var queryBuilder = new GetAssignedAsbieBusinessTermDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(valueOf(asbieBusinessTermId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAssignedAsbieBusinessTermDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            inline("ASBIE").as("bie_type"),
                            ASBIE_BIZTERM.ASBIE_BIZTERM_ID.as("assigned_business_term_id"),
                            ASBIE_BIZTERM.ASBIE_ID.as("bie_id"),
                            ASBIE_BIZTERM.PRIMARY_INDICATOR.as("primary_indicator"),
                            ASBIE_BIZTERM.TYPE_CODE.as("type_code"),
                            ASCC_MANIFEST.DEN.as("den"),
                            BUSINESS_TERM.BUSINESS_TERM_ID,
                            BUSINESS_TERM.BUSINESS_TERM_,
                            BUSINESS_TERM.EXTERNAL_REF_URI.as("external_reference_uri"),
                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            ASBIE_BIZTERM.CREATION_TIMESTAMP.as("creation_timestamp"),
                            ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.as("last_update_timestamp")
                    ), creatorFields(), updaterFields()))
                    .from(ASBIE_BIZTERM)
                    .join(ASCC_BIZTERM).on(ASBIE_BIZTERM.ASCC_BIZTERM_ID.eq(ASCC_BIZTERM.ASCC_BIZTERM_ID))
                    .join(ASCC).on(ASCC_BIZTERM.ASCC_ID.eq(ASCC.ASCC_ID))
                    .join(BUSINESS_TERM).on(and(
                            ASCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID)
                    ))
//               next 3 joins to get release information
                    .join(ASBIE).on(ASBIE_BIZTERM.ASBIE_ID.eq(ASBIE.ASBIE_ID))
                    .join(ASCC_MANIFEST).on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                    .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
//              next joins to get user information
                    .join(creatorTable()).on(ASBIE_BIZTERM.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(ASBIE_BIZTERM.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, AssignedBusinessTermDetailsRecord> mapper() {
            return record -> new AssignedBusinessTermDetailsRecord(
                    new BusinessTermId(record.get("assigned_business_term_id", BigInteger.class)),
                    record.getValue("bie_id", BigInteger.class),
                    record.getValue("bie_type", String.class),
                    record.getValue("den", String.class),
                    (byte) 1 == record.getValue("primary_indicator", Byte.class),
                    record.getValue("type_code", String.class),
                    record.getValue("business_term_id", BigInteger.class),
                    record.getValue("business_term", String.class),
                    record.getValue("external_reference_uri", String.class),
                    Collections.emptyList(),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.getValue("creation_timestamp", LocalDateTime.class))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.getValue("last_update_timestamp", LocalDateTime.class))
                    ));
        }

    }

    @Override
    public AssignedBusinessTermDetailsRecord getAssignedBusinessTermDetails(BbieBusinessTermId bbieBusinessTermId) {

        var queryBuilder = new GetAssignedBbieBusinessTermDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(valueOf(bbieBusinessTermId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAssignedBbieBusinessTermDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            inline("BBIE").as("bie_type"),
                            BBIE_BIZTERM.BBIE_BIZTERM_ID.as("assigned_business_term_id"),
                            BBIE_BIZTERM.BBIE_ID.as("bie_id"),
                            BBIE_BIZTERM.PRIMARY_INDICATOR.as("primary_indicator"),
                            BBIE_BIZTERM.TYPE_CODE.as("type_code"),
                            BCC_MANIFEST.DEN.as("den"),
                            BUSINESS_TERM.BUSINESS_TERM_ID,
                            BUSINESS_TERM.BUSINESS_TERM_,
                            BUSINESS_TERM.EXTERNAL_REF_URI.as("external_reference_uri"),
                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            BBIE_BIZTERM.CREATION_TIMESTAMP.as("creation_timestamp"),
                            BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.as("last_update_timestamp")
                    ), creatorFields(), updaterFields()))
                    .from(BBIE_BIZTERM)
                    .join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                    .join(BCC).on(BCC_BIZTERM.BCC_ID.eq(BCC.BCC_ID))
                    .join(BUSINESS_TERM).on(and(
                            BCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID)
                    ))
//               next 3 joins to get release information
                    .join(BBIE).on(BBIE_BIZTERM.BBIE_ID.eq(BBIE.BBIE_ID))
                    .join(BCC_MANIFEST).on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                    .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
//              next joins to get user information
                    .join(creatorTable()).on(BBIE_BIZTERM.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BBIE_BIZTERM.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, AssignedBusinessTermDetailsRecord> mapper() {
            return record -> new AssignedBusinessTermDetailsRecord(
                    new BusinessTermId(record.get("assigned_business_term_id", BigInteger.class)),
                    record.getValue("bie_id", BigInteger.class),
                    record.getValue("bie_type", String.class),
                    record.getValue("den", String.class),
                    (byte) 1 == record.getValue("primary_indicator", Byte.class),
                    record.getValue("type_code", String.class),
                    record.getValue("business_term_id", BigInteger.class),
                    record.getValue("business_term", String.class),
                    record.getValue("external_reference_uri", String.class),
                    Collections.emptyList(),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.getValue("creation_timestamp", LocalDateTime.class))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.getValue("last_update_timestamp", LocalDateTime.class))
                    ));
        }

    }

    @Override
    public ResultAndCount<AsbieBbieListEntryRecord> getAsbieBbieList(
            AsbieBbieListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new AsbieBbieListQueryBuilder(filterCriteria);
        var where = queryBuilder.select();
        int count = dslContext().fetchCount(where);
        List<AsbieBbieListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    @Override
    public List<AsbieBbieListEntryRecord> getAsbieBbieList(
            Collection<AsbieId> asbieIdList, Collection<BbieId> bbieIdList) {

        var queryBuilder = new AsbieBbieListQueryBuilder(asbieIdList, bbieIdList);
        var where = queryBuilder.select();
        List<AsbieBbieListEntryRecord> result = queryBuilder.fetch(where,
                pageRequest(0, asbieIdList.size() + bbieIdList.size(),
                        null, null));
        return result;
    }

    private class AsbieListQueryBuilder {

        private AsbieBbieListFilterCriteria filterCriteria;

        public AsbieListQueryBuilder(AsbieBbieListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("ASBIE").as("type"),
                    ASBIE.ASBIE_ID.as("bie_id"),
                    ASBIE.GUID.as("guid"),
                    TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,

                    ASCC_MANIFEST.DEN.as("den"),
                    ASCCP.PROPERTY_TERM.as("property_term"),
                    TOP_LEVEL_ASBIEP.VERSION,
                    TOP_LEVEL_ASBIEP.STATUS,
                    ASBIEP.BIZ_TERM.as("biz_term"),
                    ASBIE.REMARK.as("remark"),
                    TOP_LEVEL_ASBIEP.STATE,

                    ASBIE.CREATION_TIMESTAMP.as("creation_timestamp"),
                    ASBIE.LAST_UPDATE_TIMESTAMP.as("last_update_timestamp"),

                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state")));
            if (hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(ASCCP.PROPERTY_TERM), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().selectDistinct(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASBIE)
                    .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                    .join(ASCC_MANIFEST).on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                    .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                    .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(ASBIE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(ASBIE.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                    .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID))
                    .leftJoin(TENANT_BUSINESS_CTX).on(BIZ_CTX.BIZ_CTX_ID.eq(TENANT_BUSINESS_CTX.BIZ_CTX_ID));
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

            if (filterCriteria.asbieIdList() != null && !filterCriteria.asbieIdList().isEmpty()) {
                if (filterCriteria.asbieIdList().size() == 1) {
                    conditions.add(ASBIE.ASBIE_ID.eq(valueOf(filterCriteria.asbieIdList().iterator().next())));
                } else {
                    conditions.add(ASBIE.ASBIE_ID.in(valueOf(filterCriteria.asbieIdList())));
                }
            }

            String den = filterCriteria.den();
            if (hasLength(den)) {
                conditions.add(or(
                        ASCC_MANIFEST.DEN.contains(den),
                        ASCCP_MANIFEST.DEN.contains(den)
                ));
            }

            if (hasLength(filterCriteria.propertyTerm())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.propertyTerm(), ASCCP.PROPERTY_TERM));
            }

            if (hasLength(filterCriteria.version())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.version(), TOP_LEVEL_ASBIEP.VERSION));
            }

            if (hasLength(filterCriteria.remark())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.remark(), ASBIE.REMARK));
            }

            if (filterCriteria.businessContextNameList() != null && !filterCriteria.businessContextNameList().isEmpty()) {
                conditions.add(or(filterCriteria.businessContextNameList().stream()
                        .map(e -> and(ContainsFilterBuilder.contains(e, BIZ_CTX.NAME)))
                        .collect(Collectors.toList())));
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

            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(ASBIE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(ASBIE.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }

            return conditions;
        }

    }

    private class BbieListQueryBuilder {

        private AsbieBbieListFilterCriteria filterCriteria;

        public BbieListQueryBuilder(AsbieBbieListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("BBIE").as("type"),
                    BBIE.BBIE_ID.as("bie_id"),
                    BBIE.GUID.as("guid"),
                    TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,

                    BCC_MANIFEST.DEN.as("den"),
                    BCCP.PROPERTY_TERM.as("property_term"),
                    TOP_LEVEL_ASBIEP.VERSION,
                    TOP_LEVEL_ASBIEP.STATUS,
                    BBIEP.BIZ_TERM.as("biz_term"),
                    BBIE.REMARK.as("remark"),
                    TOP_LEVEL_ASBIEP.STATE,

                    BBIE.CREATION_TIMESTAMP.as("creation_timestamp"),
                    BBIE.LAST_UPDATE_TIMESTAMP.as("last_update_timestamp"),

                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state")));
            if (hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(BCCP.PROPERTY_TERM), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(BCCP.PROPERTY_TERM), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().selectDistinct(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(BBIE)
                    .join(BBIEP).on(BBIE.TO_BBIEP_ID.eq(BBIEP.BBIEP_ID))
                    .join(BCC_MANIFEST).on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                    .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                    .join(BCCP_MANIFEST).on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                    .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                    .join(TOP_LEVEL_ASBIEP).on(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                    .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ownerTable()).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(BBIE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BBIE.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(BIZ_CTX_ASSIGNMENT).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID))
                    .join(BIZ_CTX).on(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID))
                    .leftJoin(TENANT_BUSINESS_CTX).on(BIZ_CTX.BIZ_CTX_ID.eq(TENANT_BUSINESS_CTX.BIZ_CTX_ID));
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

            if (filterCriteria.bbieIdList() != null && !filterCriteria.bbieIdList().isEmpty()) {
                if (filterCriteria.bbieIdList().size() == 1) {
                    conditions.add(BBIE.BBIE_ID.eq(valueOf(filterCriteria.bbieIdList().iterator().next())));
                } else {
                    conditions.add(BBIE.BBIE_ID.in(valueOf(filterCriteria.bbieIdList())));
                }
            }

            String den = filterCriteria.den();
            if (hasLength(den)) {
                conditions.add(or(
                        BCC_MANIFEST.DEN.contains(den),
                        ASCCP_MANIFEST.DEN.contains(den)
                ));
            }

            if (hasLength(filterCriteria.propertyTerm())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.propertyTerm(), BCCP.PROPERTY_TERM));
            }

            if (hasLength(filterCriteria.version())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.version(), TOP_LEVEL_ASBIEP.VERSION));
            }

            if (hasLength(filterCriteria.remark())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.remark(), BBIE.REMARK));
            }

            if (filterCriteria.businessContextNameList() != null && !filterCriteria.businessContextNameList().isEmpty()) {
                conditions.add(or(filterCriteria.businessContextNameList().stream()
                        .map(e -> and(ContainsFilterBuilder.contains(e, BIZ_CTX.NAME)))
                        .collect(Collectors.toList())));
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

            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(BBIE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(BBIE.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }

            return conditions;
        }
    }

    private class AsbieBbieListQueryBuilder {

        private final AsbieBbieListFilterCriteria filterCriteria;

        public AsbieBbieListQueryBuilder(AsbieBbieListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        public AsbieBbieListQueryBuilder(Collection<AsbieId> asbieIdList, Collection<BbieId> bbieIdList) {
            this(AsbieBbieListFilterCriteria.builder()
                    .asbieIdList(asbieIdList)
                    .bbieIdList(bbieIdList)
                    .build());
        }

        SelectOrderByStep<? extends org.jooq.Record> select() {

            SelectOrderByStep select = null;

            if (filterCriteria.typeList() == null || filterCriteria.typeList().isEmpty() ||
                    filterCriteria.typeList().contains("ASBIE")) {
                var queryBuilder = new AsbieListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(queryBuilder.conditions());
                select = (select != null) ? select.union(where) : where;
            }
            if (filterCriteria.typeList() == null || filterCriteria.typeList().isEmpty() ||
                    filterCriteria.typeList().contains("BBIE")) {
                var queryBuilder = new BbieListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(queryBuilder.conditions());
                select = (select != null) ? select.union(where) : where;
            }

            return select;
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
                        field = field("property_term");
                        break;

                    case "den":
                        field = field("den");
                        break;

                    case "releaseNum":
                        field = RELEASE.RELEASE_NUM;
                        break;

                    case "owner":
                        field = field("owner_login_id");
                        break;

                    case "version":
                        field = TOP_LEVEL_ASBIEP.VERSION;
                        break;

                    case "status":
                        field = TOP_LEVEL_ASBIEP.STATUS;
                        break;

                    case "bizTerm":
                        field = field("biz_term");
                        break;

                    case "remark":
                        field = field("remark");
                        break;

                    case "lastUpdateTimestamp":
                        field = field("last_update_timestamp");
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

        public List<AsbieBbieListEntryRecord> fetch(
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

        private RecordMapper<org.jooq.Record, AsbieBbieListEntryRecord> mapper() {
            return record -> {
                TopLevelAsbiepId topLevelAsbiepId = new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                BigInteger bieId = record.getValue("bie_id", BigInteger.class);
                BieState state = BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);

                var bizCtxQuery = repositoryFactory().businessContextQueryRepository(requester());

                return new AsbieBbieListEntryRecord(
                        record.getValue("type", String.class),

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

                        bieId,
                        new Guid(record.getValue("guid", String.class)),
                        topLevelAsbiepId,

                        record.getValue("den", String.class),
                        record.getValue("property_term", String.class),
                        record.get(TOP_LEVEL_ASBIEP.VERSION),
                        record.get(TOP_LEVEL_ASBIEP.STATUS),
                        record.getValue("biz_term", String.class),
                        record.getValue("remark", String.class),
                        bizCtxQuery.getBusinessContextSummaryList(topLevelAsbiepId),
                        state,
                        AccessPrivilege.toAccessPrivilege(requester(), owner.userId(), state),

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.getValue("creation_timestamp", LocalDateTime.class))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.getValue("last_update_timestamp", LocalDateTime.class))
                        )
                );
            };
        }
    }

}
