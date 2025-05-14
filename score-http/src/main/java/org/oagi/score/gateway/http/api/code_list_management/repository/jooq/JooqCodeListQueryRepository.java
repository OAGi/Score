package org.oagi.score.gateway.http.api.code_list_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.*;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.criteria.CodeListListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.*;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.Revised;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;
import static org.springframework.util.StringUtils.hasLength;

public class JooqCodeListQueryRepository extends JooqBaseRepository implements CodeListQueryRepository {

    private final AgencyIdListQueryRepository agencyIdListQueryRepository;

    public JooqCodeListQueryRepository(DSLContext dslContext, ScoreUser requester,
                                       RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        agencyIdListQueryRepository = repositoryFactory.agencyIdListQueryRepository(requester);
    }

    @Override
    public CodeListSummaryRecord getCodeListSummary(CodeListManifestId codeListManifestId) {
        if (codeListManifestId == null) {
            return null;
        }

        var queryBuilder = new GetCodeListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<CodeListSummaryRecord> getCodeListSummaryList() {
        var queryBuilder = new GetCodeListSummaryQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<CodeListSummaryRecord> getCodeListSummaryList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetCodeListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<CodeListSummaryRecord> getCodeListSummaryList(Collection<ReleaseId> releaseIdList) {
        if (releaseIdList == null || releaseIdList.isEmpty()) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetCodeListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<CodeListSummaryRecord> getCodeListSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(CODE_LIST.STATE.eq(state.name()));
        }
        var queryBuilder = new GetCodeListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private class GetCodeListSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                            CODE_LIST.CODE_LIST_ID,
                            CODE_LIST.GUID, CODE_LIST.ENUM_TYPE_GUID,
                            CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                            CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            CODE_LIST.NAME, CODE_LIST.LIST_ID, CODE_LIST.VERSION_ID,
                            CODE_LIST.DEFINITION, CODE_LIST.DEFINITION_SOURCE,
                            CODE_LIST.NAMESPACE_ID,
                            CODE_LIST.IS_DEPRECATED,
                            CODE_LIST.STATE,
                            CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                            CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID
                    ), ownerFields()))
                    .from(CODE_LIST_MANIFEST)
                    .join(CODE_LIST)
                    .on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(CODE_LIST.OWNER_USER_ID.eq(ownerTablePk()));
        }

        RecordMapper<org.jooq.Record, CodeListSummaryRecord> mapper() {
            return record -> {
                CodeListManifestId codeListManifestId =
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());

                CodeListManifestId basedCodeListManifestId =
                        (record.get(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID).toBigInteger()) : null;
                AgencyIdListValueManifestId agencyIdListValueManifestId =
                        (record.get(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null;

                return new CodeListSummaryRecord(
                        codeListManifestId,
                        new CodeListId(record.get(CODE_LIST.CODE_LIST_ID).toBigInteger()),
                        new Guid(record.get(CODE_LIST.GUID)),
                        record.get(CODE_LIST.ENUM_TYPE_GUID),
                        basedCodeListManifestId,
                        agencyIdListValueManifestId,
                        record.get(CODE_LIST.NAME),
                        record.get(CODE_LIST.LIST_ID),
                        record.get(CODE_LIST.VERSION_ID),
                        new Definition(record.get(CODE_LIST.DEFINITION), record.get(CODE_LIST.DEFINITION_SOURCE)),
                        (record.get(CODE_LIST.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(CODE_LIST.NAMESPACE_ID).toBigInteger()) : null,
                        (byte) 1 == record.get(CODE_LIST.IS_DEPRECATED),
                        CcState.valueOf(record.get(CODE_LIST.STATE)),

                        fetchOwnerSummary(record),

                        (record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID).toBigInteger()) : null,

                        getCodeListValueSummaryList(codeListManifestId)
                );
            };
        }
    }

    @Override
    public List<CodeListSummaryRecord> availableCodeListByDtManifestId(DtManifestId dtManifestId, List<CcState> states) {

        var dtQuery = repositoryFactory().dtQueryRepository(requester());
        DtSummaryRecord dt = dtQuery.getDtSummary(dtManifestId);

        Result<Record2<ULong, ULong>> result = dslContext().selectDistinct(
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                        DT_MANIFEST.RELEASE_ID)
                .from(DT_MANIFEST)
                .join(DT_AWD_PRI).on(and(
                        DT_MANIFEST.RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID),
                        DT_MANIFEST.DT_ID.eq(DT_AWD_PRI.DT_ID)
                ))
                .join(CODE_LIST_MANIFEST).on(and(
                        DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID),
                        DT_MANIFEST.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID)
                ))
                .join(CODE_LIST).on(and(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                            availableCodeListByCodeListManifestId(
                                    new CodeListManifestId(e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()), states))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(CodeListSummaryRecord::name))
                    .collect(Collectors.toList());

        } else {
            return availableCodeListByReleaseId(dt.release().releaseId(), states);
        }
    }

    @Override
    public List<CodeListSummaryRecord> availableCodeListByDtScManifestId(DtScManifestId dtScManifestId, List<CcState> states) {

        var dtQuery = repositoryFactory().dtQueryRepository(requester());
        DtScSummaryRecord dtSc = dtQuery.getDtScSummary(dtScManifestId);

        Result<Record2<ULong, ULong>> result = dslContext().selectDistinct(
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                        DT_SC_MANIFEST.RELEASE_ID)
                .from(DT_SC_MANIFEST)
                .join(DT_SC_AWD_PRI).on(and(
                        DT_SC_MANIFEST.RELEASE_ID.eq(DT_SC_AWD_PRI.RELEASE_ID),
                        DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC_AWD_PRI.DT_SC_ID)
                ))
                .join(CODE_LIST_MANIFEST).on(and(
                        DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID),
                        DT_SC_MANIFEST.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID)
                ))
                .join(CODE_LIST).on(and(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtSc.dtScManifestId())))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                            availableCodeListByCodeListManifestId(
                                    new CodeListManifestId(e.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()), states))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(CodeListSummaryRecord::name))
                    .collect(Collectors.toList());

        } else {
            return availableCodeListByReleaseId(dtSc.release().releaseId(), states);
        }
    }

    private List<CodeListSummaryRecord> availableCodeListByCodeListManifestId(
            CodeListManifestId codeListManifestId, List<CcState> states) {
        if (codeListManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetCodeListSummaryQueryBuilder();
        List<CodeListSummaryRecord> availableCodeLists = queryBuilder.select()
                .where(and(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .fetch(queryBuilder.mapper());

        List<CodeListManifestId> associatedCodeLists = dslContext().selectDistinct(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(and(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(
                        availableCodeLists.stream()
                                .filter(e -> e.basedCodeListManifestId() != null)
                                .map(e -> e.basedCodeListManifestId())
                                .distinct()
                                .collect(Collectors.toList())
                ))
                .fetchStream().map(record ->
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()))
                .collect(Collectors.toList());

        List<CodeListSummaryRecord> mergedCodeLists = new ArrayList();
        mergedCodeLists.addAll(availableCodeLists);
        for (CodeListManifestId associatedCodeListId : associatedCodeLists) {
            mergedCodeLists.addAll(
                    availableCodeListByCodeListManifestId(associatedCodeListId, states)
            );
        }

        // #1094: Add Code list which is base availableCodeLists
        List<CodeListSummaryRecord> baseCodeLists = queryBuilder.select()
                .where(and(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)),
                        states.isEmpty() ? trueCondition() : CODE_LIST.STATE.in(states)))
                .fetch(queryBuilder.mapper());

        mergedCodeLists.addAll(baseCodeLists);
        return mergedCodeLists.stream().distinct().collect(Collectors.toList());
    }

    private List<CodeListSummaryRecord> availableCodeListByReleaseId(ReleaseId releaseId, List<CcState> states) {

        List<Condition> conditions = new ArrayList();

        conditions.add(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)));
        if (!states.isEmpty()) {
            conditions.add(CODE_LIST.STATE.in(states));
        }

        var queryBuilder = new GetCodeListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private List<CodeListValueSummaryRecord> getCodeListValueSummaryList(
            CodeListManifestId codeListManifestId) {

        var queryBuilder = new GetCodeListValueSummaryQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private CodeListValueSummaryRecord getCodeListValueSummary(
            CodeListValueManifestId codeListValueManifestId) {

        var queryBuilder = new GetCodeListValueSummaryQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.eq(valueOf(codeListValueManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetCodeListValueSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID,
                            CODE_LIST_VALUE.CODE_LIST_VALUE_ID,
                            CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,

                            CODE_LIST_VALUE.GUID,
                            CODE_LIST_VALUE.VALUE,
                            CODE_LIST_VALUE.MEANING,

                            CODE_LIST.STATE)
                    .from(CODE_LIST_VALUE_MANIFEST)
                    .join(CODE_LIST_MANIFEST).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .join(CODE_LIST_VALUE).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.CODE_LIST_VALUE_ID))
                    .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID));
        }

        RecordMapper<org.jooq.Record, CodeListValueSummaryRecord> mapper() {

            return record -> new CodeListValueSummaryRecord(
                    new CodeListValueManifestId(record.get(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID).toBigInteger()),
                    new CodeListValueId(record.get(CODE_LIST_VALUE.CODE_LIST_VALUE_ID).toBigInteger()),
                    new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()),

                    new Guid(record.get(CODE_LIST_VALUE.GUID)),
                    record.get(CODE_LIST_VALUE.VALUE),
                    record.get(CODE_LIST_VALUE.MEANING),
                    CcState.valueOf(record.get(CODE_LIST.STATE))
            );
        }

    }

    @Override
    public CodeListDetailsRecord getCodeListDetails(CodeListManifestId codeListManifestId) {

        if (codeListManifestId == null) {
            return null;
        }

        var queryBuilder = new GetCodeListDetailsQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetCodeListDetailsQueryBuilder {

        SelectOnConditionStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                            CODE_LIST.CODE_LIST_ID,
                            CODE_LIST.GUID,
                            CODE_LIST.ENUM_TYPE_GUID,
                            CODE_LIST.NAME,
                            CODE_LIST.LIST_ID,
                            CODE_LIST.VERSION_ID,

                            CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id"),
                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,

                            CODE_LIST.DEFINITION,
                            CODE_LIST.DEFINITION_SOURCE,
                            CODE_LIST.REMARK,
                            CODE_LIST.IS_DEPRECATED,
                            CODE_LIST.EXTENSIBLE_INDICATOR,
                            iif(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNull(), true, false).as("new_component"),
                            CODE_LIST.STATE,
                            CODE_LIST.CREATION_TIMESTAMP,
                            CODE_LIST.LAST_UPDATE_TIMESTAMP,

                            NAMESPACE.NAMESPACE_ID,
                            NAMESPACE.URI,
                            NAMESPACE.PREFIX,
                            NAMESPACE.IS_STD_NMSP,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.LOG_ID,
                            LOG.REVISION_NUM,
                            LOG.REVISION_TRACKING_NUM,

                            MODULE.PATH,

                            CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                            CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(CODE_LIST_MANIFEST)
                    .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(CODE_LIST.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(CODE_LIST.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CODE_LIST.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(NAMESPACE).on(CODE_LIST.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(CODE_LIST_MANIFEST.as("based_code_list_manifest")).on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID))
                    .leftJoin(CODE_LIST.as("based_code_list")).on(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_ID.eq(CODE_LIST.as("based_code_list").CODE_LIST_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .leftJoin(MODULE_CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        RecordMapper<Record, CodeListDetailsRecord> mapper() {
            return record -> {
                CodeListManifestId codeListManifestId =
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
                CodeListManifestId basedCodeListManifestId =
                        (record.get(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id")) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id")).toBigInteger()) : null;
                AgencyIdListValueManifestId agencyIdListValueManifestId =
                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null;
                LibrarySummaryRecord library = new LibrarySummaryRecord(
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(LIBRARY.NAME.as("library_name")),
                        record.get(LIBRARY.STATE.as("library_state")),
                        (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                );
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                CcState state = CcState.valueOf(record.get(CODE_LIST.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new CodeListDetailsRecord(
                        library, release,
                        codeListManifestId,
                        new CodeListId(record.get(CODE_LIST.CODE_LIST_ID).toBigInteger()),
                        new Guid(record.get(CODE_LIST.GUID)),
                        record.get(CODE_LIST.ENUM_TYPE_GUID),
                        (basedCodeListManifestId != null) ? getCodeListSummary(basedCodeListManifestId) : null,
                        (agencyIdListValueManifestId != null) ? agencyIdListQueryRepository.getAgencyIdListValueSummary(agencyIdListValueManifestId) : null,
                        record.get(CODE_LIST.NAME),
                        record.get(CODE_LIST.LIST_ID),
                        record.get(CODE_LIST.VERSION_ID),

                        new Definition(record.get(CODE_LIST.DEFINITION), record.get(CODE_LIST.DEFINITION_SOURCE)),
                        record.get(CODE_LIST.REMARK),
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        (byte) 1 == record.get(CODE_LIST.IS_DEPRECATED),
                        (byte) 1 == record.get(CODE_LIST.EXTENSIBLE_INDICATOR),
                        record.get(field("new_component", Boolean.class)),
                        state,
                        AccessPrivilege.toAccessPrivilege(requester(), owner.userId(), state, release.isWorkingRelease()),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,
                        record.get(MODULE.PATH),

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CODE_LIST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CODE_LIST.LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID).toBigInteger()) : null,

                        getCodeListValueDetailsList(codeListManifestId)
                );
            };
        }
    }

    @Override
    public CodeListDetailsRecord getPrevCodeListDetails(CodeListManifestId codeListManifestId) {

        if (codeListManifestId == null) {
            return null;
        }

        var queryBuilder = new GetCodeListDetailsQueryBuilder();
        CodeListDetailsRecord prevCodeListDetails = queryBuilder.select()
                .where(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetchOne(queryBuilder.mapper());
        if (prevCodeListDetails == null) {
            // In the case of an end-user, the new revision is created within the same Manifest and does not have a previous Manifest.
            // Therefore, the previous record must be retrieved based on the log.
            var prevQueryBuilder = new GetPrevCodeListDetailsQueryBuilder();
            prevCodeListDetails = prevQueryBuilder.select()
                    .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                    .fetchOne(prevQueryBuilder.mapper());
        }
        return prevCodeListDetails;
    }

    private class GetPrevCodeListDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                            CODE_LIST.as("prev").CODE_LIST_ID,
                            CODE_LIST.as("prev").GUID,
                            CODE_LIST.as("prev").ENUM_TYPE_GUID,
                            CODE_LIST.as("prev").NAME,
                            CODE_LIST.as("prev").LIST_ID,
                            CODE_LIST.as("prev").VERSION_ID,

                            CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id"),
                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,

                            CODE_LIST.as("prev").DEFINITION,
                            CODE_LIST.as("prev").DEFINITION_SOURCE,
                            CODE_LIST.as("prev").REMARK,
                            CODE_LIST.as("prev").IS_DEPRECATED,
                            CODE_LIST.as("prev").EXTENSIBLE_INDICATOR,
                            iif(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNull(), true, false).as("new_component"),
                            CODE_LIST.as("prev").STATE,
                            CODE_LIST.as("prev").CREATION_TIMESTAMP,
                            CODE_LIST.as("prev").LAST_UPDATE_TIMESTAMP,

                            NAMESPACE.NAMESPACE_ID,
                            NAMESPACE.URI,
                            NAMESPACE.PREFIX,
                            NAMESPACE.IS_STD_NMSP,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.as("prev_log").LOG_ID,
                            LOG.as("prev_log").REVISION_NUM,
                            LOG.as("prev_log").REVISION_TRACKING_NUM,

                            MODULE.PATH,

                            CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                            CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(CODE_LIST_MANIFEST)
                    .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .join(CODE_LIST.as("prev")).on(and(
                            CODE_LIST.PREV_CODE_LIST_ID.eq(CODE_LIST.as("prev").CODE_LIST_ID),
                            CODE_LIST.CODE_LIST_ID.eq(CODE_LIST.as("prev").NEXT_CODE_LIST_ID)
                    ))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(CODE_LIST.as("prev").OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(CODE_LIST.as("prev").CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CODE_LIST.as("prev").LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(LOG.as("revised_log")).on(and(
                            LOG.REFERENCE.eq(LOG.as("revised_log").REFERENCE),
                            LOG.REVISION_NUM.eq(LOG.as("revised_log").REVISION_NUM),
                            LOG.as("revised_log").REVISION_TRACKING_NUM.eq(UInteger.valueOf(1)),
                            LOG.as("revised_log").LOG_ACTION.eq(Revised.name())
                    ))
                    .leftJoin(LOG.as("prev_log")).on(
                            LOG.as("revised_log").PREV_LOG_ID.eq(LOG.as("prev_log").LOG_ID),
                            LOG.as("revised_log").LOG_ID.eq(LOG.as("prev_log").NEXT_LOG_ID)
                    )
                    .leftJoin(NAMESPACE).on(CODE_LIST.as("prev").NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(CODE_LIST_MANIFEST.as("based_code_list_manifest")).on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID))
                    .leftJoin(CODE_LIST.as("based_code_list")).on(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_ID.eq(CODE_LIST.as("based_code_list").CODE_LIST_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .leftJoin(MODULE_CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        RecordMapper<org.jooq.Record, CodeListDetailsRecord> mapper() {
            return record -> {
                CodeListManifestId codeListManifestId =
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
                CodeListManifestId basedCodeListManifestId =
                        (record.get(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id")) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id")).toBigInteger()) : null;
                AgencyIdListValueManifestId agencyIdListValueManifestId =
                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null;
                LibrarySummaryRecord library = new LibrarySummaryRecord(
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(LIBRARY.NAME.as("library_name")),
                        record.get(LIBRARY.STATE.as("library_state")),
                        (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                );
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                CcState state = CcState.valueOf(record.get(CODE_LIST.as("prev").STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new CodeListDetailsRecord(
                        library, release,
                        codeListManifestId,
                        new CodeListId(record.get(CODE_LIST.as("prev").CODE_LIST_ID).toBigInteger()),
                        new Guid(record.get(CODE_LIST.as("prev").GUID)),
                        record.get(CODE_LIST.as("prev").ENUM_TYPE_GUID),
                        (basedCodeListManifestId != null) ? getCodeListSummary(basedCodeListManifestId) : null,
                        (agencyIdListValueManifestId != null) ? agencyIdListQueryRepository.getAgencyIdListValueSummary(agencyIdListValueManifestId) : null,
                        record.get(CODE_LIST.as("prev").NAME),
                        record.get(CODE_LIST.as("prev").LIST_ID),
                        record.get(CODE_LIST.as("prev").VERSION_ID),

                        new Definition(record.get(CODE_LIST.as("prev").DEFINITION), record.get(CODE_LIST.as("prev").DEFINITION_SOURCE)),
                        record.get(CODE_LIST.as("prev").REMARK),
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        (byte) 1 == record.get(CODE_LIST.as("prev").IS_DEPRECATED),
                        (byte) 1 == record.get(CODE_LIST.as("prev").EXTENSIBLE_INDICATOR),
                        record.get(field("new_component", Boolean.class)),
                        state,
                        AccessPrivilege.toAccessPrivilege(requester(), owner.userId(), state, release.isWorkingRelease()),

                        (record.get(LOG.as("prev_log").LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.as("prev_log").LOG_ID).toBigInteger()),
                                record.get(LOG.as("prev_log").REVISION_NUM).intValue(),
                                record.get(LOG.as("prev_log").REVISION_TRACKING_NUM).intValue()) : null,
                        record.get(MODULE.PATH),

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CODE_LIST.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CODE_LIST.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID).toBigInteger()) : null,

                        getPrevCodeListValueDetailsList(codeListManifestId)
                );
            };
        }
    }

    @Override
    public List<CodeListValueDetailsRecord> getCodeListValueDetailsList(CodeListManifestId codeListManifestId) {

        var queryBuilder = new GetCodeListValueDetailsQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public CodeListValueDetailsRecord getCodeListValueDetails(CodeListValueManifestId codeListValueManifestId) {

        var queryBuilder = new GetCodeListValueDetailsQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.eq(valueOf(codeListValueManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetCodeListValueDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID,
                            CODE_LIST_VALUE.CODE_LIST_VALUE_ID,
                            CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,

                            CODE_LIST_VALUE.GUID,
                            CODE_LIST_VALUE.VALUE,
                            CODE_LIST_VALUE.MEANING,
                            CODE_LIST_VALUE.DEFINITION,
                            CODE_LIST_VALUE.DEFINITION_SOURCE,
                            CODE_LIST_VALUE.IS_DEPRECATED,
                            CODE_LIST.STATE,
                            CODE_LIST_VALUE.CREATION_TIMESTAMP,
                            CODE_LIST_VALUE.LAST_UPDATE_TIMESTAMP,

                            CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID,
                            CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(CODE_LIST_VALUE_MANIFEST)
                    .join(CODE_LIST_MANIFEST).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .join(CODE_LIST_VALUE).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.CODE_LIST_VALUE_ID))
                    .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .join(ownerTable()).on(CODE_LIST_VALUE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(CODE_LIST_VALUE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CODE_LIST_VALUE.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, CodeListValueDetailsRecord> mapper() {
            return record -> {
                CodeListValueManifestId codeListValueManifestId =
                        new CodeListValueManifestId(record.get(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID).toBigInteger());
                return new CodeListValueDetailsRecord(
                        codeListValueManifestId,
                        new CodeListValueId(record.get(CODE_LIST_VALUE.CODE_LIST_VALUE_ID).toBigInteger()),
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()),

                        new Guid(record.get(CODE_LIST_VALUE.GUID)),
                        record.get(CODE_LIST_VALUE.VALUE),
                        record.get(CODE_LIST_VALUE.MEANING),
                        new Definition(record.get(CODE_LIST_VALUE.DEFINITION), record.get(CODE_LIST_VALUE.DEFINITION_SOURCE)),
                        (byte) 1 == record.get(CODE_LIST_VALUE.IS_DEPRECATED),
                        isUsed(codeListValueManifestId),
                        CcState.valueOf(record.get(CODE_LIST.STATE)),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CODE_LIST_VALUE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CODE_LIST_VALUE.LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID) != null) ?
                                new CodeListValueManifestId(record.get(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID) != null) ?
                                new CodeListValueManifestId(record.get(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    private boolean isUsed(CodeListManifestId codeListManifestId) {
        return dslContext().selectCount()
                .from(DT_AWD_PRI)
                .where(DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(DT_SC_AWD_PRI)
                        .where(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                        .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(BBIE)
                        .where(BBIE.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                        .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(BBIE_SC)
                        .where(BBIE_SC.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                        .fetchOneInto(Integer.class) > 0;
    }

    private boolean isUsed(CodeListValueManifestId codeListValueManifestId) {
        return dslContext().selectCount()
                .from(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.BASED_CODE_LIST_VALUE_MANIFEST_ID.eq(valueOf(codeListValueManifestId)))
                .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(CTX_SCHEME)
                        .join(CODE_LIST).on(CTX_SCHEME.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                        .join(CODE_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID))
                        .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.eq(valueOf(codeListValueManifestId)))
                        .fetchOneInto(Integer.class) > 0;
    }

    private List<CodeListValueDetailsRecord> getPrevCodeListValueDetailsList(
            CodeListManifestId codeListManifestId) {

        var queryBuilder = new GetPrevCodeListValueDetailsQueryBuilder();
        return queryBuilder.select()
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetPrevCodeListValueDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().selectDistinct(concat(fields(
                            CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID,
                            CODE_LIST_VALUE.as("prev").CODE_LIST_VALUE_ID,
                            CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,

                            CODE_LIST_VALUE.as("prev").GUID,
                            CODE_LIST_VALUE.as("prev").VALUE,
                            CODE_LIST_VALUE.as("prev").MEANING,
                            CODE_LIST_VALUE.as("prev").DEFINITION,
                            CODE_LIST_VALUE.as("prev").DEFINITION_SOURCE,
                            CODE_LIST_VALUE.as("prev").IS_DEPRECATED,
                            CODE_LIST.as("prev_code_list").STATE,
                            CODE_LIST_VALUE.as("prev").CREATION_TIMESTAMP,
                            CODE_LIST_VALUE.as("prev").LAST_UPDATE_TIMESTAMP,

                            CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID,
                            CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(CODE_LIST_VALUE_MANIFEST)
                    .join(CODE_LIST_MANIFEST).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .join(CODE_LIST_VALUE).on(and(
                            CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.CODE_LIST_VALUE_ID),
                            CODE_LIST_VALUE.PREV_CODE_LIST_VALUE_ID.isNotNull()
                    ))
                    .join(CODE_LIST_VALUE.as("prev")).on(and(
                            CODE_LIST_VALUE.PREV_CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.as("prev").CODE_LIST_VALUE_ID),
                            CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.as("prev").NEXT_CODE_LIST_VALUE_ID)
                    ))
                    .join(CODE_LIST.as("prev_code_list")).on(CODE_LIST_VALUE.as("prev").CODE_LIST_ID.eq(CODE_LIST.as("prev_code_list").CODE_LIST_ID))
                    .join(ownerTable()).on(CODE_LIST_VALUE.as("prev").OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(CODE_LIST_VALUE.as("prev").CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CODE_LIST_VALUE.as("prev").LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, CodeListValueDetailsRecord> mapper() {
            return record -> {
                CodeListValueManifestId codeListValueManifestId =
                        new CodeListValueManifestId(record.get(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID).toBigInteger());
                return new CodeListValueDetailsRecord(
                        codeListValueManifestId,
                        new CodeListValueId(record.get(CODE_LIST_VALUE.as("prev").CODE_LIST_VALUE_ID).toBigInteger()),
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()),

                        new Guid(record.get(CODE_LIST_VALUE.as("prev").GUID)),
                        record.get(CODE_LIST_VALUE.as("prev").VALUE),
                        record.get(CODE_LIST_VALUE.as("prev").MEANING),
                        new Definition(record.get(CODE_LIST_VALUE.as("prev").DEFINITION), record.get(CODE_LIST_VALUE.as("prev").DEFINITION_SOURCE)),
                        (byte) 1 == record.get(CODE_LIST_VALUE.as("prev").IS_DEPRECATED),
                        isUsed(codeListValueManifestId),
                        CcState.valueOf(record.get(CODE_LIST.as("prev_code_list").STATE)),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CODE_LIST_VALUE.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CODE_LIST_VALUE.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID) != null) ?
                                new CodeListValueManifestId(record.get(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID) != null) ?
                                new CodeListValueManifestId(record.get(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public ResultAndCount<CodeListListEntryRecord> getCodeListList(
            CodeListListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetCodeListListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<CodeListListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetCodeListListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                            CODE_LIST.CODE_LIST_ID,
                            CODE_LIST.GUID,
                            CODE_LIST.ENUM_TYPE_GUID,
                            CODE_LIST.NAME,
                            CODE_LIST.LIST_ID,
                            CODE_LIST.VERSION_ID,

                            CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id"),
                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,

                            CODE_LIST.DEFINITION,
                            CODE_LIST.DEFINITION_SOURCE,
                            CODE_LIST.REMARK,
                            CODE_LIST.NAMESPACE_ID,
                            CODE_LIST.IS_DEPRECATED,
                            CODE_LIST.EXTENSIBLE_INDICATOR,
                            iif(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNull(), true, false).as("new_component"),
                            CODE_LIST.STATE,
                            CODE_LIST.CREATION_TIMESTAMP,
                            CODE_LIST.LAST_UPDATE_TIMESTAMP,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.LOG_ID,
                            LOG.REVISION_NUM,
                            LOG.REVISION_TRACKING_NUM,

                            MODULE.PATH,

                            CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                            CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(CODE_LIST_MANIFEST)
                    .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(CODE_LIST.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(CODE_LIST.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(CODE_LIST.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(CODE_LIST_MANIFEST.as("based_code_list_manifest")).on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID))
                    .leftJoin(CODE_LIST.as("based_code_list")).on(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_ID.eq(CODE_LIST.as("based_code_list").CODE_LIST_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .leftJoin(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(MODULE_CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        public List<Condition> conditions(CodeListListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();
            conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));
            conditions.add(RELEASE.RELEASE_ID.eq(valueOf(filterCriteria.releaseId())));

            if (hasLength(filterCriteria.name())) {
                conditions.addAll(contains(filterCriteria.name(), CODE_LIST.NAME));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.addAll(contains(filterCriteria.module(), MODULE.PATH));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(contains(filterCriteria.definition(), CODE_LIST.DEFINITION));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(CODE_LIST.STATE.in(filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())));
            }
            if (filterCriteria.deprecated() != null) {
                conditions.add(CODE_LIST.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.extensible() != null) {
                conditions.add(CODE_LIST.EXTENSIBLE_INDICATOR.eq((byte) (filterCriteria.extensible() ? 1 : 0)));
            }
            if (filterCriteria.ownedByDeveloper() != null) {
                conditions.add(ownerTable().IS_DEVELOPER.eq((byte) (filterCriteria.ownedByDeveloper() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent() ?
                        CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNull() :
                        CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNotNull());
            }
            if (filterCriteria.access() != null) {
                switch (filterCriteria.access()) {
                    case CanEdit:
                        conditions.add(ownerTablePk().eq(valueOf(requester().userId())));
                        break;

                    case CanView:
                        conditions.add(
                                or(
                                        CODE_LIST.STATE.in(Arrays.asList(QA, Production)
                                                .stream().map(e -> e.name()).collect(Collectors.toSet())),
                                        ownerTablePk().eq(valueOf(requester().userId()))
                                )
                        );
                        break;
                }
            }
            if (filterCriteria.namespaces() != null && !filterCriteria.namespaces().isEmpty()) {
                conditions.add(CODE_LIST.NAMESPACE_ID.in(valueOf(filterCriteria.namespaces())));
            }
            if (filterCriteria.ownerLoginIdSet() != null && !filterCriteria.ownerLoginIdSet().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdSet()));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(CODE_LIST.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(CODE_LIST.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "codeListName":
                        field = CODE_LIST.NAME;
                        break;

                    case "basedCodeListName":
                        field = CODE_LIST.as("based_code_list").NAME;
                        break;

                    case "agencyId":
                        field = AGENCY_ID_LIST_VALUE.NAME;
                        break;

                    case "version":
                    case "versionId":
                        field = CODE_LIST.VERSION_ID;
                        break;

                    case "extensible":
                        field = CODE_LIST.EXTENSIBLE_INDICATOR;
                        break;

                    case "revision":
                        field = LOG.REVISION_NUM;
                        break;

                    case "owner":
                        field = APP_USER.as("owner").LOGIN_ID;
                        break;

                    case "module":
                    case "modulePath":
                        field = MODULE.PATH;
                        break;

                    case "lastUpdateTimestamp":
                        field = CODE_LIST.LAST_UPDATE_TIMESTAMP;
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

        public List<CodeListListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        RecordMapper<org.jooq.Record, CodeListListEntryRecord> mapper() {
            return record -> {
                CodeListManifestId codeListManifestId =
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger());
                CodeListManifestId basedCodeListManifestId =
                        (record.get(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id")) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.as("based_code_list_manifest").CODE_LIST_MANIFEST_ID.as("based_code_list_manifest_id")).toBigInteger()) : null;
                AgencyIdListValueManifestId agencyIdListValueManifestId =
                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null;
                LibrarySummaryRecord library = new LibrarySummaryRecord(
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(LIBRARY.NAME.as("library_name")),
                        record.get(LIBRARY.STATE.as("library_state")),
                        (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                );
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                CcState state = CcState.valueOf(record.get(CODE_LIST.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new CodeListListEntryRecord(
                        library, release,
                        codeListManifestId,
                        new CodeListId(record.get(CODE_LIST.CODE_LIST_ID).toBigInteger()),

                        new Guid(record.get(CODE_LIST.GUID)),
                        record.get(CODE_LIST.ENUM_TYPE_GUID),
                        record.get(CODE_LIST.NAME),
                        record.get(CODE_LIST.LIST_ID),
                        record.get(CODE_LIST.VERSION_ID),
                        (basedCodeListManifestId != null) ? getCodeListSummary(basedCodeListManifestId) : null,
                        (agencyIdListValueManifestId != null) ? agencyIdListQueryRepository.getAgencyIdListValueSummary(agencyIdListValueManifestId) : null,
                        record.get(CODE_LIST.DEFINITION),
                        record.get(CODE_LIST.DEFINITION_SOURCE),
                        record.get(CODE_LIST.REMARK),
                        (record.get(CODE_LIST.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(CODE_LIST.NAMESPACE_ID).toBigInteger()) : null,
                        (byte) 1 == record.get(CODE_LIST.IS_DEPRECATED),
                        (byte) 1 == record.get(CODE_LIST.EXTENSIBLE_INDICATOR),
                        record.get(field("new_component", Boolean.class)),
                        state,
                        AccessPrivilege.toAccessPrivilege(requester(), owner.userId(), state, release.isWorkingRelease()),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,
                        record.get(MODULE.PATH),

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(CODE_LIST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(CODE_LIST.LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID) != null) ?
                                new CodeListManifestId(record.get(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public boolean hasRecordsByNamespaceId(NamespaceId namespaceId) {
        return dslContext().selectCount()
                .from(CODE_LIST)
                .where(CODE_LIST.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasSameCodeList(ReleaseId releaseId,
                                   CodeListManifestId codeListManifestId,
                                   AgencyIdListValueManifestId agencyIdListValueManifestId,
                                   String listId, String versionId) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                CODE_LIST.STATE.notEqual(Deleted.name())
        ));

        if (codeListManifestId != null) {
            conditions.add(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.ne(valueOf(codeListManifestId)));
        }
        conditions.add(and(
                CODE_LIST.LIST_ID.eq(listId),
                CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(valueOf(agencyIdListValueManifestId)),
                CODE_LIST.VERSION_ID.eq(versionId)
        ));

        return dslContext().selectCount()
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(conditions).fetchOneInto(Integer.class) > 0;
    }

    @Override
    public boolean hasSameNameCodeList(ReleaseId releaseId,
                                       CodeListManifestId codeListManifestId,
                                       String codeListName) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                CODE_LIST.STATE.notEqual(Deleted.name())
        ));

        if (codeListManifestId != null) {
            conditions.add(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.ne(valueOf(codeListManifestId)));
        }
        conditions.add(CODE_LIST.NAME.eq(codeListName));

        return dslContext().selectCount()
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(conditions).fetchOneInto(Integer.class) > 0;
    }
}
