package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.repository.BieQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcChangesResponse;
import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.*;
import org.oagi.score.gateway.http.api.cc_management.repository.criteria.CcListFilterCriteria;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCc;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExt;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.repository.TagQueryRepository;
import org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AccManifestRecord;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Routines.levenshtein;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Acc.ACC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Bccp.BCCP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccpManifest.BCCP_MANIFEST;
import static org.springframework.util.StringUtils.hasLength;

public class JooqCcQueryRepository extends JooqBaseRepository implements CcQueryRepository {

    private AccQueryRepository accQueryRepository;

    private SeqKeyQueryRepository seqKeyQueryRepository;

    private AsccpQueryRepository asccpQueryRepository;

    private BccpQueryRepository bccpQueryRepository;

    private DtQueryRepository dtQueryRepository;

    private BieQueryRepository bieQueryRepository;

    private ReleaseQueryRepository releaseQueryRepository;

    private TagQueryRepository tagQueryRepository;

    public JooqCcQueryRepository(DSLContext dslContext,
                                 ScoreUser requester,
                                 RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.accQueryRepository = repositoryFactory.accQueryRepository(requester);
        this.seqKeyQueryRepository = repositoryFactory.seqKeyQueryRepository(requester);
        this.asccpQueryRepository = repositoryFactory.asccpQueryRepository(requester);
        this.bccpQueryRepository = repositoryFactory.bccpQueryRepository(requester);
        this.dtQueryRepository = repositoryFactory.dtQueryRepository(requester);
        this.bieQueryRepository = repositoryFactory.bieQueryRepository(requester);

        this.releaseQueryRepository = repositoryFactory.releaseQueryRepository(requester);
        this.tagQueryRepository = repositoryFactory.tagQueryRepository(requester);
    }

    @Override
    public ResultAndCount<CcListEntryRecord> getCcList(
            CcListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new CcListQueryBuilder(filterCriteria);
        var where = queryBuilder.select();
        int count = dslContext().fetchCount(where);
        List<CcListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    @Override
    public List<CcListEntryRecord> getBaseAccList(AccManifestId accManifestId) {

        var accQuery = repositoryFactory().accQueryRepository(requester());
        AccSummaryRecord acc = accQuery.getAccSummary(accManifestId);

        List<AccManifestRecord> accManifestRecordList = dslContext().selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(acc.release().releaseId()))).fetch();

        AccManifestRecord accManifestRecord = accManifestRecordList.stream()
                .filter(e -> e.getAccManifestId().equals(valueOf(accManifestId)))
                .findFirst().orElse(null);

        if (accManifestRecord == null) {
            throw new IllegalArgumentException("The ACC manifest record with ID " + accManifestId + " could not be found.");
        }

        List<ULong> accManifestIdList = new ArrayList<>();

        while (accManifestRecord.getBasedAccManifestId() != null) {
            ULong cur = accManifestRecord.getBasedAccManifestId();
            accManifestIdList.add(cur);
            accManifestRecord = accManifestRecordList.stream().filter(e -> e.getAccManifestId().equals(cur)).findFirst().orElse(null);
        }

        Collections.reverse(accManifestIdList);

        CcListFilterCriteria filterCriteria =
                CcListFilterCriteria.builder(acc.release().releaseId())
                        .build();

        var queryBuilder = new AccListQueryBuilder(filterCriteria);
        return queryBuilder.select()
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(accManifestIdList))
                .fetch(new CcListQueryBuilder(filterCriteria).mapper());
    }

    private class CcListQueryBuilder {

        private Set<ReleaseSummaryRecord> includedReleaseSummarySet;
        private CcListFilterCriteria filterCriteria;

        CcListQueryBuilder(CcListFilterCriteria filterCriteria) {
            this.includedReleaseSummarySet =
                    releaseQueryRepository.getIncludedReleaseSummaryList(filterCriteria.releaseId());
            this.filterCriteria = filterCriteria;
        }

        SelectOrderByStep<? extends org.jooq.Record> select() {

            SelectOrderByStep select = null;
            if (filterCriteria.types().isAcc() && filterCriteria.asccpManifestIds().isEmpty()) {
                var queryBuilder = new AccListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(
                        queryBuilder.conditions(includedReleaseSummarySet)
                );
                select = (select != null) ? select.union(where) : where;
            }
            if (filterCriteria.types().isAscc() && filterCriteria.asccpManifestIds().isEmpty()) {
                var queryBuilder = new AsccListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(
                        queryBuilder.conditions(includedReleaseSummarySet)
                );
                select = (select != null) ? select.union(where) : where;
            }
            if (filterCriteria.types().isBcc() && filterCriteria.asccpManifestIds().isEmpty()) {
                var queryBuilder = new BccListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(
                        queryBuilder.conditions(includedReleaseSummarySet)
                );
                select = (select != null) ? select.union(where) : where;
            }
            if (filterCriteria.types().isAsccp()) {
                var queryBuilder = new AsccpListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(
                        queryBuilder.conditions(includedReleaseSummarySet)
                );
                select = (select != null) ? select.union(where) : where;
            }
            if (filterCriteria.types().isBccp() && filterCriteria.asccpManifestIds().isEmpty()) {
                var queryBuilder = new BccpListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(
                        queryBuilder.conditions(includedReleaseSummarySet)
                );
                select = (select != null) ? select.union(where) : where;
            }
            if (filterCriteria.types().isDt() && filterCriteria.asccpManifestIds().isEmpty()) {
                var queryBuilder = new DtListQueryBuilder(filterCriteria);
                var where = queryBuilder.select().where(
                        queryBuilder.conditions(includedReleaseSummarySet)
                );
                select = (select != null) ? select.union(where) : where;
            }
            return select;
        }

        public List<SortField<?>> sortFields(PageRequest pageRequest) {
            List<SortField<?>> sortFields = new ArrayList<>();

            for (Sort sort : pageRequest.sorts()) {
                Field field;
                switch (sort.field()) {
                    case "type":
                        field = field("type");
                        break;

                    case "state":
                        field = field("state");
                        break;

                    case "den":
                        field = field("den");
                        break;

                    case "valueDomain":
                        field = field("default_value_domain");
                        break;

                    case "sixDigitId":
                        field = field("six_digit_id");
                        break;

                    case "revision":
                        field = field(LOG.REVISION_NUM);
                        break;

                    case "owner":
                        field = ownerTable().LOGIN_ID;
                        break;

                    case "module":
                        field = field("module_path");
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

            if (filterCriteria != null && hasLength(filterCriteria.den())) {
                sortFields.add(field("score").desc());
            }

            return sortFields;
        }

        public List<CcListEntryRecord> fetch(SelectOrderByStep<?> conditionStep, PageRequest pageRequest) {
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

        private RecordMapper<org.jooq.Record, CcListEntryRecord> mapper() {
            return record -> {
                CcType type = CcType.valueOf(record.getValue("type", String.class));
                Id manifestId = toManifestId(type, record.getValue("manifest_id", BigInteger.class));
                BigInteger basedManifestId = record.getValue("based_manifest_id", BigInteger.class);
                Integer componentType = record.getValue("oagis_component_type", Integer.class);
                return new CcListEntryRecord(
                        type,
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

                        manifestId,
                        new Guid(record.getValue("guid", String.class)),
                        (basedManifestId != null) ? toManifestId(type, basedManifestId) : null,
                        record.getValue("den", String.class),
                        record.getValue("term", String.class),
                        new Definition(
                                stripToNull(record.getValue("definition", String.class)),
                                stripToNull(record.getValue("definition_source", String.class))),
                        record.getValue("module_path", String.class),

                        (componentType != null) ? OagisComponentType.valueOf(componentType) : null,
                        CcState.valueOf(record.getValue("state", String.class)),
                        record.getValue("is_deprecated", Byte.class) == 1,
                        record.getValue("new_component", Byte.class) == 1,

                        (type == CcType.DT) ? ((basedManifestId != null) ? "BDT" : "CDT") : "",
                        record.getValue("six_digit_id", String.class),
                        record.getValue("default_value_domain", String.class),

                        getTagSummaryList(manifestId),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,
                        fetchOwnerSummary(record),
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

        private Id toManifestId(CcType type, BigInteger id) {
            switch (type) {
                case ACC:
                    return new AccManifestId(id);
                case ASCCP:
                    return new AsccpManifestId(id);
                case BCCP:
                    return new BccpManifestId(id);
                case ASCC:
                    return new AsccManifestId(id);
                case BCC:
                    return new BccManifestId(id);
                case DT:
                    return new DtManifestId(id);
                default:
                    throw new IllegalStateException();
            }
        }

        private Id toId(CcType type, BigInteger id) {
            switch (type) {
                case ACC:
                    return new AccId(id);
                case ASCCP:
                    return new AsccpId(id);
                case BCCP:
                    return new BccpId(id);
                case ASCC:
                    return new AsccId(id);
                case BCC:
                    return new BccId(id);
                case DT:
                    return new DtId(id);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private List<TagSummaryRecord> getTagSummaryList(Id manifestId) {
        if (manifestId == null) {
            return Collections.emptyList();
        }

        if (manifestId instanceof AccManifestId) {
            return this.tagQueryRepository.getTagSummaryList((AccManifestId) manifestId);
        } else if (manifestId instanceof AsccpManifestId) {
            return this.tagQueryRepository.getTagSummaryList((AsccpManifestId) manifestId);
        } else if (manifestId instanceof BccpManifestId) {
            return this.tagQueryRepository.getTagSummaryList((BccpManifestId) manifestId);
        } else if (manifestId instanceof DtManifestId) {
            return this.tagQueryRepository.getTagSummaryList((DtManifestId) manifestId);
        } else {
            return Collections.emptyList();
        }
    }

    private class AccListQueryBuilder {
        
        private CcListFilterCriteria filterCriteria;

        public AccListQueryBuilder(CcListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("ACC").as("type"),
                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
                    ACC.ACC_ID.as("id"),
                    ACC_MANIFEST.BASED_ACC_MANIFEST_ID.as("based_manifest_id"),
                    ACC.GUID,
                    ACC_MANIFEST.DEN,
                    ACC.DEFINITION,
                    ACC.DEFINITION_SOURCE,
                    ACC.OBJECT_CLASS_TERM.as("term"),
                    ACC.OAGIS_COMPONENT_TYPE.as("oagis_component_type"),
                    ACC.STATE,
                    ACC.IS_DEPRECATED,
                    ACC.CREATION_TIMESTAMP,
                    ACC.LAST_UPDATE_TIMESTAMP,
                    MODULE.PATH.as("module_path"),

                    LOG.LOG_ID,
                    LOG.REVISION_NUM,
                    LOG.REVISION_TRACKING_NUM,

                    val((String) null).as("six_digit_id"),
                    val((String) null).as("default_value_domain"),
                    iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull()), true, false).as("new_component")));
            if (filterCriteria != null && hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(ACC_MANIFEST.DEN), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(ACC_MANIFEST.DEN), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().select(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(ACC)
                    .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                    .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(ACC.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(ACC.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(ACC.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(ACC_MANIFEST_TAG).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST_TAG.ACC_MANIFEST_ID))
                    .leftJoin(TAG).on(ACC_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                    .leftJoin(MODULE_ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        List<Condition> conditions(Collection<ReleaseSummaryRecord> includedReleases) {
            List<Condition> conditions = new ArrayList();

            conditions.add(ACC_MANIFEST.RELEASE_ID.in(valueOf(
                    includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet())
            )));
            if (includedReleases.iterator().next().isWorkingRelease()) {
                conditions.add(ACC.OAGIS_COMPONENT_TYPE.notEqual(UserExtensionGroup.getValue()));
            }
            if (filterCriteria.deprecated() != null) {
                conditions.add(ACC.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent()
                        ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull())
                        : or(RELEASE.PREV_RELEASE_ID.isNull(), ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNotNull()));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(ACC.STATE.in(
                        filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())
                ));
            }
            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(ACC.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(ACC.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            if (filterCriteria.excludes() != null && !filterCriteria.excludes().isEmpty()) {
                conditions.add(ACC_MANIFEST.ACC_MANIFEST_ID.notIn(
                        filterCriteria.excludes().stream().map(e -> ULong.valueOf(e)).collect(Collectors.toSet())
                ));
            }
            if (hasLength(filterCriteria.den())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.den(), ACC_MANIFEST.DEN));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.definition(), ACC.DEFINITION));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.add(MODULE.PATH.containsIgnoreCase(filterCriteria.module()));
            }
            if (filterCriteria.tags() != null && !filterCriteria.tags().isEmpty()) {
                conditions.add(TAG.NAME.in(filterCriteria.tags()));
            }
            if (filterCriteria.namespaceIds() != null && !filterCriteria.namespaceIds().isEmpty()) {
                conditions.add(ACC.NAMESPACE_ID.in(valueOf(filterCriteria.namespaceIds())));
            }
            if (filterCriteria.componentTypes() != null && !filterCriteria.componentTypes().isEmpty()) {
                List<OagisComponentType> usualComponentTypes = filterCriteria.componentTypes().stream()
                        .filter(e -> !Arrays.asList(BOD, Verb, Noun).contains(e))
                        .collect(Collectors.toList());

                if (!usualComponentTypes.isEmpty()) {
                    conditions.add(ACC.OAGIS_COMPONENT_TYPE.in(usualComponentTypes.stream()
                            .map(e -> e.getValue()).collect(Collectors.toSet())));
                }

                List<OagisComponentType> unusualComponentTypes = filterCriteria.componentTypes().stream()
                        .filter(e -> Arrays.asList(BOD, Verb, Noun).contains(e))
                        .collect(Collectors.toList());
                if (!unusualComponentTypes.isEmpty()) {
                    var query = repositoryFactory().accQueryRepository(requester());
                    for (OagisComponentType unusualComponentType : unusualComponentTypes) {
                        switch (unusualComponentType) {
                            case BOD:
                                AccSummaryRecord bodAcc = query.getAccSummaryList(
                                                includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet()),
                                                "Business Object Document")
                                        .stream().findAny().orElse(null);
                                if (bodAcc == null) {
                                    throw new IllegalStateException("'Business Object Document' not found");
                                }
                                conditions.add(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(valueOf(bodAcc.accManifestId())));
                                break;

                            case Verb:
                                AccSummaryRecord verbAcc = query.getAccSummaryList(
                                                includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet()),
                                                "Verb")
                                        .stream().findAny().orElse(null);
                                if (verbAcc == null) {
                                    throw new IllegalStateException("'Verb' not found");
                                }

                                Set<AccManifestId> verbManifestIds = new HashSet();
                                verbManifestIds.add(verbAcc.accManifestId());

                                List<AccManifestId> basedAccManifestIds = new ArrayList();
                                basedAccManifestIds.add(verbAcc.accManifestId());

                                while (!basedAccManifestIds.isEmpty()) {
                                    basedAccManifestIds = basedAccManifestIds.stream().map(basedAccManifestId ->
                                                    query.getInheritedAccSummaryList(basedAccManifestId).stream().map(e -> e.accManifestId()).collect(Collectors.toSet()))
                                            .flatMap(Collection::stream).collect(Collectors.toList());
                                    verbManifestIds.addAll(basedAccManifestIds);
                                }

                                conditions.add(ACC_MANIFEST.ACC_MANIFEST_ID.in(valueOf(verbManifestIds)));

                                break;

                            case Noun:
                                // TODO:

                                break;
                        }
                    }

                }
            }
            return conditions;
        }

    }

    private class AsccListQueryBuilder {

        private CcListFilterCriteria filterCriteria;

        public AsccListQueryBuilder(CcListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("ASCC").as("type"),
                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    ASCC_MANIFEST.ASCC_MANIFEST_ID.as("manifest_id"),
                    ASCC.ASCC_ID.as("id"),
                    val((Integer) null).as("based_manifest_id"),
                    ASCC.GUID,
                    ASCC_MANIFEST.DEN,
                    ASCC.DEFINITION,
                    ASCC.DEFINITION_SOURCE,
                    val((String) null).as("term"),
                    val((String) null).as("oagis_component_type"),
                    ACC.STATE,
                    ASCC.IS_DEPRECATED,
                    ASCC.CREATION_TIMESTAMP,
                    ASCC.LAST_UPDATE_TIMESTAMP,
                    MODULE.PATH.as("module_path"),

                    LOG.LOG_ID,
                    LOG.REVISION_NUM,
                    LOG.REVISION_TRACKING_NUM,

                    val((String) null).as("six_digit_id"),
                    val((String) null).as("default_value_domain"),
                    iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull()), true, false).as("new_component")));
            if (filterCriteria != null && hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(ASCC_MANIFEST.DEN), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(ASCC_MANIFEST.DEN), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().select(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASCC)
                    .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                    .join(ACC_MANIFEST)
                    .on(and(
                            ASCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)
                    ))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(ASCC.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(ASCC.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(ASCC.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(MODULE_ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        List<Condition> conditions(Collection<ReleaseSummaryRecord> includedReleases) {
            List<Condition> conditions = new ArrayList();

            conditions.add(ASCC_MANIFEST.RELEASE_ID.in(valueOf(
                    includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet())
            )));
            conditions.add(ASCC_MANIFEST.DEN.notContains("User Extension Group"));

            if (filterCriteria.deprecated() != null) {
                conditions.add(ASCC.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent()
                        ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull())
                        : or(RELEASE.PREV_RELEASE_ID.isNull(), ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNotNull()));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(ACC.STATE.in(
                        filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())
                ));
            }
            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(ASCC.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(ASCC.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            if (filterCriteria.excludes() != null && !filterCriteria.excludes().isEmpty()) {
                conditions.add(ASCC_MANIFEST.ASCC_MANIFEST_ID.notIn(
                        filterCriteria.excludes().stream().map(e -> ULong.valueOf(e)).collect(Collectors.toSet())
                ));
            }
            if (hasLength(filterCriteria.den())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.den(), ASCC_MANIFEST.DEN));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.definition(), ASCC.DEFINITION));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.add(MODULE.PATH.containsIgnoreCase(filterCriteria.module()));
            }
            if (filterCriteria.namespaceIds() != null && !filterCriteria.namespaceIds().isEmpty()) {
                conditions.add(ACC.NAMESPACE_ID.in(valueOf(filterCriteria.namespaceIds())));
            }
            return conditions;
        }

    }

    private class BccListQueryBuilder {
        
        private CcListFilterCriteria filterCriteria;

        public BccListQueryBuilder(CcListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("BCC").as("type"),
                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    BCC_MANIFEST.BCC_MANIFEST_ID.as("manifest_id"),
                    BCC.BCC_ID.as("id"),
                    val((Integer) null).as("based_manifest_id"),
                    BCC.GUID,
                    BCC_MANIFEST.DEN,
                    BCC.DEFINITION,
                    BCC.DEFINITION_SOURCE,
                    val((String) null).as("term"),
                    val((String) null).as("oagis_component_type"),
                    ACC.STATE,
                    BCC.IS_DEPRECATED,
                    BCC.CREATION_TIMESTAMP,
                    BCC.LAST_UPDATE_TIMESTAMP,
                    MODULE.PATH.as("module_path"),

                    LOG.LOG_ID,
                    LOG.REVISION_NUM,
                    LOG.REVISION_TRACKING_NUM,

                    val((String) null).as("six_digit_id"),
                    val((String) null).as("default_value_domain"),
                    iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull()), true, false).as("new_component")));
            if (filterCriteria != null && hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(BCC_MANIFEST.DEN), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(BCC_MANIFEST.DEN), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().select(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(BCC)
                    .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                    .join(ACC_MANIFEST)
                    .on(and(
                            BCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)
                    ))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(BCC.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(BCC.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BCC.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(MODULE_ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        List<Condition> conditions(Collection<ReleaseSummaryRecord> includedReleases) {
            List<Condition> conditions = new ArrayList();

            conditions.add(BCC_MANIFEST.RELEASE_ID.in(valueOf(
                    includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet())
            )));
            conditions.add(BCC_MANIFEST.DEN.notContains("User Extension Group"));

            if (filterCriteria.deprecated() != null) {
                conditions.add(BCC.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent()
                        ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull())
                        : or(RELEASE.PREV_RELEASE_ID.isNull(), BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNotNull()));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(ACC.STATE.in(
                        filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())
                ));
            }
            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(BCC.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(BCC.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            if (filterCriteria.excludes() != null && !filterCriteria.excludes().isEmpty()) {
                conditions.add(BCC_MANIFEST.BCC_MANIFEST_ID.notIn(filterCriteria.excludes()));
            }
            if (hasLength(filterCriteria.den())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.den(), BCC_MANIFEST.DEN));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.definition(), BCC.DEFINITION));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.add(MODULE.PATH.containsIgnoreCase(filterCriteria.module()));
            }
            if (filterCriteria.namespaceIds() != null && !filterCriteria.namespaceIds().isEmpty()) {
                conditions.add(ACC.NAMESPACE_ID.in(valueOf(filterCriteria.namespaceIds())));
            }
            return conditions;
        }

    }

    private class AsccpListQueryBuilder {
        
        private CcListFilterCriteria filterCriteria;

        public AsccpListQueryBuilder(CcListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("ASCCP").as("type"),
                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifest_id"),
                    ASCCP.ASCCP_ID.as("id"),
                    val((Integer) null).as("based_manifest_id"),
                    ASCCP.GUID,
                    ASCCP_MANIFEST.DEN,
                    ASCCP.DEFINITION,
                    ASCCP.DEFINITION_SOURCE,
                    ASCCP.PROPERTY_TERM.as("term"),
                    val((Integer) null).as("oagis_component_type"),
                    ASCCP.STATE,
                    ASCCP.IS_DEPRECATED,
                    ASCCP.CREATION_TIMESTAMP,
                    ASCCP.LAST_UPDATE_TIMESTAMP,
                    MODULE.PATH.as("module_path"),

                    LOG.LOG_ID,
                    LOG.REVISION_NUM,
                    LOG.REVISION_TRACKING_NUM,

                    val((String) null).as("six_digit_id"),
                    val((String) null).as("default_value_domain"),
                    iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull()), true, false).as("new_component")));
            if (filterCriteria != null && hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(ASCCP_MANIFEST.DEN), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(ASCCP_MANIFEST.DEN), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().select(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASCCP)
                    .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                    .join(ACC_MANIFEST)
                    .on(and(
                            ASCCP_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                            ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)
                    ))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(ASCCP.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(ASCCP.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(ASCCP.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(ASCCP_MANIFEST_TAG).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID))
                    .leftJoin(TAG).on(ASCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                    .leftJoin(MODULE_ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        List<Condition> conditions(Collection<ReleaseSummaryRecord> includedReleases) {
            List<Condition> conditions = new ArrayList();

            conditions.add(ASCCP_MANIFEST.RELEASE_ID.in(valueOf(
                    includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet())
            )));
            conditions.add(ASCCP_MANIFEST.DEN.notContains("User Extension Group"));
            if (filterCriteria.deprecated() != null) {
                conditions.add(ASCCP.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.reusable() != null) {
                conditions.add(ASCCP.REUSABLE_INDICATOR.eq((byte) (filterCriteria.reusable() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent()
                        ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull())
                        : or(RELEASE.PREV_RELEASE_ID.isNull(), ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNotNull()));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(ASCCP.STATE.in(
                        filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())
                ));
            }
            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(ASCCP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(ASCCP.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            if (filterCriteria.excludes() != null && !filterCriteria.excludes().isEmpty()) {
                conditions.add(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.notIn(filterCriteria.excludes().stream()
                        .map(e -> ULong.valueOf(e)).collect(Collectors.toSet())));
            }
            if (filterCriteria.asccpManifestIds() != null && !filterCriteria.asccpManifestIds().isEmpty()) {
                conditions.add(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(valueOf(filterCriteria.asccpManifestIds())));
            } else if (hasLength(filterCriteria.den())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.den(), ASCCP_MANIFEST.DEN));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.definition(), ASCCP.DEFINITION));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.add(MODULE.PATH.containsIgnoreCase(filterCriteria.module()));
            }
            if (filterCriteria.tags() != null && !filterCriteria.tags().isEmpty()) {
                conditions.add(TAG.NAME.in(filterCriteria.tags()));
            }
            if (filterCriteria.namespaceIds() != null && !filterCriteria.namespaceIds().isEmpty()) {
                conditions.add(ASCCP.NAMESPACE_ID.in(valueOf(filterCriteria.namespaceIds())));
            }
            if (filterCriteria.isBIEUsable() != null && filterCriteria.isBIEUsable()) {
                conditions.add(ACC.OAGIS_COMPONENT_TYPE
                        .notIn(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue())));
            }
            if (filterCriteria.asccpTypes() != null && !filterCriteria.asccpTypes().isEmpty()) {
                conditions.add(ASCCP.TYPE.in(
                        filterCriteria.asccpTypes().stream().map(e -> e.name()).collect(Collectors.toSet())
                ));
            }
            return conditions;
        }

    }

    private class BccpListQueryBuilder {
        
        private CcListFilterCriteria filterCriteria;

        public BccpListQueryBuilder(CcListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("BCCP").as("type"),
                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifest_id"),
                    BCCP.BCCP_ID.as("id"),
                    val((Integer) null).as("based_manifest_id"),
                    BCCP.GUID,
                    BCCP_MANIFEST.DEN,
                    BCCP.DEFINITION,
                    BCCP.DEFINITION_SOURCE,
                    BCCP.PROPERTY_TERM.as("term"),
                    val((Integer) null).as("oagis_component_type"),
                    BCCP.STATE,
                    BCCP.IS_DEPRECATED,
                    BCCP.CREATION_TIMESTAMP,
                    BCCP.LAST_UPDATE_TIMESTAMP,
                    MODULE.PATH.as("module_path"),

                    LOG.LOG_ID,
                    LOG.REVISION_NUM,
                    LOG.REVISION_TRACKING_NUM,

                    val((String) null).as("six_digit_id"),
                    val((String) null).as("default_value_domain"),
                    iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull()), true, false).as("new_component")));
            if (filterCriteria != null && hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(BCCP_MANIFEST.DEN), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(BCCP_MANIFEST.DEN), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().select(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(BCCP)
                    .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                    .join(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(BCCP.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(BCCP.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(BCCP.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(BCCP_MANIFEST_TAG).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID))
                    .leftJoin(TAG).on(BCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                    .leftJoin(MODULE_BCCP_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        List<Condition> conditions(Collection<ReleaseSummaryRecord> includedReleases) {
            List<Condition> conditions = new ArrayList();

            conditions.add(BCCP_MANIFEST.RELEASE_ID.in(valueOf(
                    includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet())
            )));
            conditions.add(BCCP_MANIFEST.DEN.notContains("User Extension Group"));
            if (filterCriteria.deprecated() != null) {
                conditions.add(BCCP.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent()
                        ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull())
                        : or(RELEASE.PREV_RELEASE_ID.isNull(), BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNotNull()));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(BCCP.STATE.in(
                        filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())
                ));
            }
            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(BCCP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(BCCP.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            if (filterCriteria.excludes() != null && !filterCriteria.excludes().isEmpty()) {
                conditions.add(BCCP_MANIFEST.BCCP_MANIFEST_ID.notIn(filterCriteria.excludes()));
            }
            if (hasLength(filterCriteria.den())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.den(), BCCP_MANIFEST.DEN));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.definition(), BCCP.DEFINITION));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.add(MODULE.PATH.containsIgnoreCase(filterCriteria.module()));
            }
            if (filterCriteria.tags() != null && !filterCriteria.tags().isEmpty()) {
                conditions.add(TAG.NAME.in(filterCriteria.tags()));
            }
            if (filterCriteria.namespaceIds() != null && !filterCriteria.namespaceIds().isEmpty()) {
                conditions.add(BCCP.NAMESPACE_ID.in(valueOf(filterCriteria.namespaceIds())));
            }
            return conditions;
        }

    }

    private class DtListQueryBuilder {
        
        private CcListFilterCriteria filterCriteria;

        public DtListQueryBuilder(CcListFilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
        }

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            List<Field<?>> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(
                    inline("DT").as("type"),
                    LIBRARY.LIBRARY_ID,
                    LIBRARY.NAME.as("library_name"),
                    LIBRARY.STATE.as("library_state"),
                    LIBRARY.IS_READ_ONLY,

                    RELEASE.RELEASE_ID,
                    RELEASE.RELEASE_NUM,
                    RELEASE.STATE.as("release_state"),

                    DT_MANIFEST.DT_MANIFEST_ID.as("manifest_id"),
                    DT.DT_ID.as("id"),
                    DT_MANIFEST.BASED_DT_MANIFEST_ID.as("based_manifest_id"),
                    DT.GUID,
                    DT_MANIFEST.DEN,
                    DT.DEFINITION,
                    DT.DEFINITION_SOURCE,
                    DT.DATA_TYPE_TERM.as("term"),
                    val((Integer) null).as("oagis_component_type"),
                    DT.STATE,
                    DT.IS_DEPRECATED,
                    DT.CREATION_TIMESTAMP,
                    DT.LAST_UPDATE_TIMESTAMP,
                    MODULE.PATH.as("module_path"),

                    LOG.LOG_ID,
                    LOG.REVISION_NUM,
                    LOG.REVISION_TRACKING_NUM,

                    DT.SIX_DIGIT_ID,
                    ifnull(CDT_PRI.NAME, "").as("default_value_domain"),
                    iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull()), true, false).as("new_component")));
            if (filterCriteria != null && hasLength(filterCriteria.den())) {
                fields.add(
                        val(1).minus(levenshtein(lower(DT_MANIFEST.DEN), val(filterCriteria.den().toLowerCase()))
                                        .div(greatest(length(DT_MANIFEST.DEN), length(filterCriteria.den()))))
                                .as("score")
                );
            }

            return dslContext().selectDistinct(concat(fields.stream(), ownerFields(), creatorFields(), updaterFields()))
                    .from(DT)
                    .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                    .join(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(DT.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(DT.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(DT.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(DT_MANIFEST_TAG).on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST_TAG.DT_MANIFEST_ID))
                    .leftJoin(TAG).on(DT_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                    .leftJoin(MODULE_DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(MODULE_DT_MANIFEST.DT_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                    .leftJoin(DT_AWD_PRI).on(and(
                            DT_MANIFEST.RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID),
                            DT_MANIFEST.DT_ID.eq(DT_AWD_PRI.DT_ID),
                            DT_AWD_PRI.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(CDT_PRI).on(DT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                    .leftJoin(XBT_MANIFEST).on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                    .leftJoin(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                    .leftJoin(CODE_LIST_MANIFEST).on(DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .leftJoin(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .leftJoin(AGENCY_ID_LIST_MANIFEST).on(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID));
        }

        List<Condition> conditions(Collection<ReleaseSummaryRecord> includedReleases) {
            List<Condition> conditions = new ArrayList();

            conditions.add(DT_MANIFEST.RELEASE_ID.in(valueOf(
                    includedReleases.stream().map(e -> e.releaseId()).collect(Collectors.toSet())
            )));
            if (filterCriteria.deprecated() != null) {
                conditions.add(DT.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent()
                        ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull())
                        : or(RELEASE.PREV_RELEASE_ID.isNull(), DT_MANIFEST.PREV_DT_MANIFEST_ID.isNotNull()));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(DT.STATE.in(
                        filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())
                ));
            }
            if (filterCriteria.ownerLoginIdList() != null && !filterCriteria.ownerLoginIdList().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdList()));
            }
            if (filterCriteria.updaterLoginIdList() != null && !filterCriteria.updaterLoginIdList().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdList()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(DT.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(DT.LAST_UPDATE_TIMESTAMP.lessThan(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().before().getTime()).toLocalDateTime()));
                }
            }
            if (filterCriteria.excludes() != null && !filterCriteria.excludes().isEmpty()) {
                conditions.add(DT_MANIFEST.DT_MANIFEST_ID.notIn(filterCriteria.excludes()));
            }
            if (hasLength(filterCriteria.den())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.den(), DT_MANIFEST.DEN));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(ContainsFilterBuilder.contains(filterCriteria.definition(), DT.DEFINITION));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.add(MODULE.PATH.containsIgnoreCase(filterCriteria.module()));
            }
            if (filterCriteria.tags() != null && !filterCriteria.tags().isEmpty()) {
                conditions.add(TAG.NAME.in(filterCriteria.tags()));
            }
            if (filterCriteria.namespaceIds() != null && !filterCriteria.namespaceIds().isEmpty()) {
                conditions.add(DT.NAMESPACE_ID.in(valueOf(filterCriteria.namespaceIds())));
            }
            if (filterCriteria.commonlyUsed() != null) {
                conditions.add(DT.COMMONLY_USED.eq((byte) (filterCriteria.commonlyUsed() ? 1 : 0)));
            }
            return conditions;
        }

    }

    @Override
    public List<SummaryCc> getSummaryCcList(LibraryId libraryId) {
        List<SummaryCc> unionOfSummaryCcList = dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID.as("manifestId"),
                        inline("ACC").as("type"),
                        ACC.LAST_UPDATE_TIMESTAMP,
                        ACC.STATE,
                        ACC.OWNER_USER_ID,
                        APP_USER.LOGIN_ID.as("ownerUsername"),
                        ACC_MANIFEST.DEN)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .where(and(
                        LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())),
                        RELEASE.RELEASE_NUM.eq("Working"),
                        ACC.STATE.in("WIP", "Draft", "Candidate")))
                .union(dslContext().select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifestId"),
                                inline("ASCCP").as("type"),
                                ASCCP.LAST_UPDATE_TIMESTAMP,
                                ASCCP.STATE,
                                ASCCP.OWNER_USER_ID,
                                APP_USER.LOGIN_ID.as("ownerUsername"),
                                ASCCP_MANIFEST.DEN)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                        .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .where(and(
                                LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())),
                                RELEASE.RELEASE_NUM.eq("Working"),
                                ASCCP.STATE.in("WIP", "Draft", "Candidate"))))
                .union(dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifestId"),
                                inline("BCCP").as("type"),
                                BCCP.LAST_UPDATE_TIMESTAMP,
                                BCCP.STATE,
                                BCCP.OWNER_USER_ID,
                                APP_USER.LOGIN_ID.as("ownerUsername"),
                                BCCP_MANIFEST.DEN)
                        .from(BCCP_MANIFEST)
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                        .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .where(and(
                                LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())),
                                RELEASE.RELEASE_NUM.eq("Working"),
                                BCCP.STATE.in("WIP", "Draft", "Candidate"))))
                .union(dslContext().select(DT_MANIFEST.DT_MANIFEST_ID.as("manifestId"),
                                inline("BDT").as("type"),
                                DT.LAST_UPDATE_TIMESTAMP,
                                DT.STATE,
                                DT.OWNER_USER_ID,
                                APP_USER.LOGIN_ID.as("ownerUsername"),
                                DT_MANIFEST.DEN)
                        .from(DT_MANIFEST)
                        .join(DT).on(and(DT_MANIFEST.DT_ID.eq(DT.DT_ID), DT_MANIFEST.BASED_DT_MANIFEST_ID.isNotNull()))
                        .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                        .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .where(and(
                                LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())),
                                RELEASE.RELEASE_NUM.eq("Working"),
                                DT.STATE.in("WIP", "Draft", "Candidate"))))
                .fetchInto(SummaryCc.class);

        return unionOfSummaryCcList;
    }

    @Override
    public List<SummaryCcExt> getSummaryCcExtList(LibraryId libraryId, ReleaseId releaseId) {
        List<ULong> uegAccIds;
        if (releaseId != null && releaseId.value().longValue() > 0) {
            uegAccIds = dslContext().select(max(ACC.ACC_ID).as("id"))
                    .from(ACC)
                    .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                    .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .where(and(
                            ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                            ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId.value())),
                            LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value()))
                    ))
                    .groupBy(ACC.GUID)
                    .fetchInto(ULong.class);

        } else {
            uegAccIds = dslContext().select(max(ACC.ACC_ID).as("id"))
                    .from(ACC)
                    .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                    .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .where(and(
                            ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue()),
                            ACC_MANIFEST.RELEASE_ID.greaterThan(ULong.valueOf(0)),
                            LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value()))
                    ))
                    .groupBy(ACC.GUID)
                    .fetchInto(ULong.class);
        }

        return dslContext().select(ACC.ACC_ID,
                        ACC.OBJECT_CLASS_TERM,
                        ACC.STATE,
                        ACC.OWNER_USER_ID,
                        APP_USER.LOGIN_ID)
                .from(ACC)
                .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .where(ACC.ACC_ID.in(uegAccIds))
                .fetchStream().map(e -> {
                    SummaryCcExt item = new SummaryCcExt();
                    item.setAccId(new AccId(e.get(ACC.ACC_ID).toBigInteger()));
                    item.setObjectClassTerm(e.get(ACC.OBJECT_CLASS_TERM));
                    item.setState(CcState.valueOf(e.get(ACC.STATE)));
                    item.setOwnerUsername(e.get(APP_USER.LOGIN_ID));
                    item.setOwnerUserId(new UserId(e.get(ACC.OWNER_USER_ID).toBigInteger()));
                    return item;
                }).collect(Collectors.toList());
    }

    @Override
    public Collection<CcChangesResponse.CcChange> getCcChanges(ReleaseId releaseId) {
        List<CcChangesResponse.CcChange> response = new ArrayList<>();

        response.addAll(getNewAccList(releaseId));
        response.addAll(getChangedAccList(releaseId));

        response.addAll(getNewAsccpList(releaseId));
        response.addAll(getChangedAsccpList(releaseId));

        response.addAll(getNewBccpList(releaseId));
        response.addAll(getChangedBccpList(releaseId));

        response.addAll(getNewAsccList(releaseId));
        response.addAll(getChangedAsccList(releaseId));

        response.addAll(getNewBccList(releaseId));
        response.addAll(getChangedBccList(releaseId));

        response.addAll(getNewDtList(releaseId));
        response.addAll(getChangedDtList(releaseId));

        response.addAll(getNewCodeListList(releaseId));
        response.addAll(getChangedCodeListList(releaseId));

        response.addAll(getNewAgencyIdListList(releaseId));
        response.addAll(getChangedAgencyIdListList(releaseId));

        return response;
    }

    private Collection<CcChangesResponse.CcChange> getNewAccList(ReleaseId releaseId) {
        Map<AccManifestId, CcChangesResponse.CcChange> accChangesMap = new HashMap<>();
        dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    AccManifestId accManifestId = new AccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!accChangesMap.containsKey(accManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.ACC, accManifestId,
                                record.get(ACC_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(accManifestId));
                        accChangesMap.put(accManifestId, ccChange);
                    }
                });
        return accChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAccList(ReleaseId releaseId) {
        Map<AccManifestId, CcChangesResponse.CcChange> accChangesMap = new HashMap<>();
        dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN)
                .from(ACC_MANIFEST)
                .join(ACC_MANIFEST.as("prev_manifest")).on(
                        and(
                                ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev_manifest").ACC_MANIFEST_ID),
                                ACC_MANIFEST.ACC_ID.notEqual(ACC_MANIFEST.as("prev_manifest").ACC_ID)))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    AccManifestId accManifestId = new AccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!accChangesMap.containsKey(accManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.ACC, accManifestId,
                                record.get(ACC_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(accManifestId));
                        accChangesMap.put(accManifestId, ccChange);
                    }
                });
        return accChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewAsccpList(ReleaseId releaseId) {
        Map<AsccpManifestId, CcChangesResponse.CcChange> asccpChangesMap = new HashMap<>();
        dslContext().select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    AsccpManifestId asccpManifestId = new AsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!asccpChangesMap.containsKey(asccpManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.ASCCP, asccpManifestId,
                                record.get(ASCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(asccpManifestId));
                        asccpChangesMap.put(asccpManifestId, ccChange);
                    }
                });
        return asccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAsccpList(ReleaseId releaseId) {
        Map<AsccpManifestId, CcChangesResponse.CcChange> asccpChangesMap = new HashMap<>();
        dslContext().select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN)
                .from(ASCCP_MANIFEST)
                .join(ASCCP_MANIFEST.as("prev_manifest")).on(
                        and(
                                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID
                                        .eq(ASCCP_MANIFEST.as("prev_manifest").ASCCP_MANIFEST_ID),
                                ASCCP_MANIFEST.ASCCP_ID.notEqual(ASCCP_MANIFEST.as("prev_manifest").ASCCP_ID)))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    AsccpManifestId asccpManifestId = new AsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!asccpChangesMap.containsKey(asccpManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.ASCCP, asccpManifestId,
                                record.get(ASCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(asccpManifestId));
                        asccpChangesMap.put(asccpManifestId, ccChange);
                    }
                });
        return asccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewBccpList(ReleaseId releaseId) {
        Map<BccpManifestId, CcChangesResponse.CcChange> bccpChangesMap = new HashMap<>();
        dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    BccpManifestId bccpManifestId = new BccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!bccpChangesMap.containsKey(bccpManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.BCCP, bccpManifestId,
                                record.get(BCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(bccpManifestId));
                        bccpChangesMap.put(bccpManifestId, ccChange);
                    }
                });
        return bccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedBccpList(ReleaseId releaseId) {
        Map<BccpManifestId, CcChangesResponse.CcChange> bccpChangesMap = new HashMap<>();
        dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN)
                .from(BCCP_MANIFEST)
                .join(BCCP_MANIFEST.as("prev_manifest")).on(
                        and(
                                BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID
                                        .eq(BCCP_MANIFEST.as("prev_manifest").BCCP_MANIFEST_ID),
                                BCCP_MANIFEST.BCCP_ID.notEqual(BCCP_MANIFEST.as("prev_manifest").BCCP_ID)))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    BccpManifestId bccpManifestId = new BccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!bccpChangesMap.containsKey(bccpManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.BCCP, bccpManifestId,
                                record.get(BCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(bccpManifestId));
                        bccpChangesMap.put(bccpManifestId, ccChange);
                    }
                });
        return bccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewAsccList(ReleaseId releaseId) {
        Map<AsccManifestId, CcChangesResponse.CcChange> asccChangesMap = new HashMap<>();
        dslContext().select(ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.DEN)
                .from(ASCC_MANIFEST)
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    AsccManifestId asccManifestId = new AsccManifestId(record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!asccChangesMap.containsKey(asccManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.ASCC, asccManifestId,
                                record.get(ASCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        asccChangesMap.put(asccManifestId, ccChange);
                    }
                });
        return asccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAsccList(ReleaseId releaseId) {
        Map<AsccManifestId, CcChangesResponse.CcChange> asccChangesMap = new HashMap<>();
        dslContext().select(ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.DEN)
                .from(ASCC_MANIFEST)
                .join(ASCC_MANIFEST.as("prev_manifest")).on(
                        and(
                                ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID
                                        .eq(ASCC_MANIFEST.as("prev_manifest").ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.ASCC_ID.notEqual(ASCC_MANIFEST.as("prev_manifest").ASCC_ID)))
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    AsccManifestId asccManifestId = new AsccManifestId(record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!asccChangesMap.containsKey(asccManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.ASCC, asccManifestId,
                                record.get(ASCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        asccChangesMap.put(asccManifestId, ccChange);
                    }
                });
        return asccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewBccList(ReleaseId releaseId) {
        Map<BccManifestId, CcChangesResponse.CcChange> bccChangesMap = new HashMap<>();
        dslContext().select(BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.DEN)
                .from(BCC_MANIFEST)
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    BccManifestId bccManifestId = new BccManifestId(record.get(BCC_MANIFEST.BCC_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!bccChangesMap.containsKey(bccManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.BCC, bccManifestId,
                                record.get(BCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        bccChangesMap.put(bccManifestId, ccChange);
                    }
                });
        return bccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedBccList(ReleaseId releaseId) {
        Map<BccManifestId, CcChangesResponse.CcChange> bccChangesMap = new HashMap<>();
        dslContext().select(BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.DEN)
                .from(BCC_MANIFEST)
                .join(BCC_MANIFEST.as("prev_manifest")).on(
                        and(
                                BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev_manifest").BCC_MANIFEST_ID),
                                BCC_MANIFEST.BCC_ID.notEqual(BCC_MANIFEST.as("prev_manifest").BCC_ID)))
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    BccManifestId bccManifestId = new BccManifestId(record.get(BCC_MANIFEST.BCC_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!bccChangesMap.containsKey(bccManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.BCC, bccManifestId,
                                record.get(BCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        bccChangesMap.put(bccManifestId, ccChange);
                    }
                });
        return bccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewDtList(ReleaseId releaseId) {
        Map<DtManifestId, CcChangesResponse.CcChange> dtChangesMap = new HashMap<>();
        dslContext().select(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    DtManifestId dtManifestId = new DtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!dtChangesMap.containsKey(dtManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.DT, dtManifestId,
                                record.get(DT_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(dtManifestId));
                        dtChangesMap.put(dtManifestId, ccChange);
                    }
                });
        return dtChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedDtList(ReleaseId releaseId) {
        Map<DtManifestId, CcChangesResponse.CcChange> dtChangesMap = new HashMap<>();
        dslContext().select(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN)
                .from(DT_MANIFEST)
                .join(DT_MANIFEST.as("prev_manifest")).on(
                        and(
                                DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev_manifest").DT_MANIFEST_ID),
                                DT_MANIFEST.DT_ID.notEqual(DT_MANIFEST.as("prev_manifest").DT_ID)))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    DtManifestId dtManifestId = new DtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!dtChangesMap.containsKey(dtManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.DT, dtManifestId,
                                record.get(DT_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED,
                                repositoryFactory().tagQueryRepository(requester()).getTagSummaryList(dtManifestId));
                        dtChangesMap.put(dtManifestId, ccChange);
                    }
                });
        return dtChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewCodeListList(ReleaseId releaseId) {
        Map<CodeListManifestId, CcChangesResponse.CcChange> codeListChangesMap = new HashMap<>();
        dslContext().select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(and(
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    CodeListManifestId codeListManifestId = new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!codeListChangesMap.containsKey(codeListManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.CODE_LIST, codeListManifestId,
                                record.get(CODE_LIST.NAME), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                new ArrayList<>());
                        codeListChangesMap.put(codeListManifestId, ccChange);
                    }
                });
        return codeListChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedCodeListList(ReleaseId releaseId) {
        Map<CodeListManifestId, CcChangesResponse.CcChange> codeListChangesMap = new HashMap<>();
        dslContext().select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST_MANIFEST.as("prev_manifest")).on(
                        and(
                                CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID
                                        .eq(CODE_LIST_MANIFEST.as("prev_manifest").CODE_LIST_MANIFEST_ID),
                                CODE_LIST_MANIFEST.CODE_LIST_ID
                                        .notEqual(CODE_LIST_MANIFEST.as("prev_manifest").CODE_LIST_ID)))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    CodeListManifestId codeListManifestId = new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!codeListChangesMap.containsKey(codeListManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.CODE_LIST, codeListManifestId,
                                record.get(CODE_LIST.NAME), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        codeListChangesMap.put(codeListManifestId, ccChange);
                    }
                });
        return codeListChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewAgencyIdListList(ReleaseId releaseId) {
        Map<AgencyIdListManifestId, CcChangesResponse.CcChange> agencyIdListChangesMap = new HashMap<>();
        dslContext().select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    AgencyIdListManifestId agencyIdListManifestId = new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!agencyIdListChangesMap.containsKey(agencyIdListManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.AGENCY_ID_LIST, agencyIdListManifestId,
                                record.get(AGENCY_ID_LIST.NAME), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                new ArrayList<>());
                        agencyIdListChangesMap.put(agencyIdListManifestId, ccChange);
                    }
                });
        return agencyIdListChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAgencyIdListList(ReleaseId releaseId) {
        Map<AgencyIdListManifestId, CcChangesResponse.CcChange> agencyIdListChangesMap = new HashMap<>();
        dslContext().select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST_MANIFEST.as("prev_manifest")).on(
                        and(
                                AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID
                                        .eq(AGENCY_ID_LIST_MANIFEST.as("prev_manifest").AGENCY_ID_LIST_MANIFEST_ID),
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID
                                        .notEqual(AGENCY_ID_LIST_MANIFEST.as("prev_manifest").AGENCY_ID_LIST_ID)))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchStream().forEach(record -> {
                    AgencyIdListManifestId agencyIdListManifestId = new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                    CcChangesResponse.CcChange ccChange;
                    if (!agencyIdListChangesMap.containsKey(agencyIdListManifestId)) {
                        ccChange = new CcChangesResponse.CcChange(CcType.AGENCY_ID_LIST, agencyIdListManifestId,
                                record.get(AGENCY_ID_LIST.NAME), CcChangesResponse.CcChangeType.REVISED,
                                new ArrayList<>());
                        agencyIdListChangesMap.put(agencyIdListManifestId, ccChange);
                    }
                });
        return agencyIdListChangesMap.values();
    }

}
