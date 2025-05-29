package org.oagi.score.gateway.http.api.agency_id_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.*;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.criteria.AgencyIdListListFilterCriteria;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
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
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;

import java.sql.Timestamp;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.Revised;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AgencyIdListManifest.AGENCY_ID_LIST_MANIFEST;
import static org.oagi.score.gateway.http.common.util.DSLUtils.contains;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAgencyIdListQueryRepository extends JooqBaseRepository implements AgencyIdListQueryRepository {

    public JooqAgencyIdListQueryRepository(DSLContext dslContext, ScoreUser requester,
                                           RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AgencyIdListSummaryRecord getAgencyIdListSummary(
            AgencyIdListManifestId agencyIdListManifestId) {

        if (agencyIdListManifestId == null) {
            return null;
        }

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList() {

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList(Collection<ReleaseId> releaseIdList) {
        if (releaseIdList == null || releaseIdList.isEmpty()) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(AGENCY_ID_LIST.STATE.eq(state.name()));
        }
        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private class GetAgencyIdListSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                            AGENCY_ID_LIST.GUID, AGENCY_ID_LIST.ENUM_TYPE_GUID,
                            AGENCY_ID_LIST.NAME, AGENCY_ID_LIST.LIST_ID, AGENCY_ID_LIST.VERSION_ID,
                            AGENCY_ID_LIST.DEFINITION, AGENCY_ID_LIST.DEFINITION_SOURCE,
                            AGENCY_ID_LIST.NAMESPACE_ID,
                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            AGENCY_ID_LIST_VALUE.NAME.as("agency_id_list_value_name"),
                            AGENCY_ID_LIST.IS_DEPRECATED,
                            AGENCY_ID_LIST.STATE,

                            AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID), ownerFields()))
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(ownerTablePk()))
                    .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID));

        }

        RecordMapper<org.jooq.Record, AgencyIdListSummaryRecord> mapper() {
            return record -> {
                AgencyIdListManifestId agencyIdListManifestId =
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                AgencyIdListValueManifestId agencyIdListValueManifestId =
                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null;

                return new AgencyIdListSummaryRecord(
                        agencyIdListManifestId,
                        new AgencyIdListId(record.get(AGENCY_ID_LIST.AGENCY_ID_LIST_ID).toBigInteger()),
                        new Guid(record.get(AGENCY_ID_LIST.GUID)),
                        record.get(AGENCY_ID_LIST.ENUM_TYPE_GUID),
                        record.get(AGENCY_ID_LIST.NAME),
                        record.get(AGENCY_ID_LIST.LIST_ID),
                        record.get(AGENCY_ID_LIST.VERSION_ID),
                        new Definition(record.get(AGENCY_ID_LIST.DEFINITION), record.get(AGENCY_ID_LIST.DEFINITION_SOURCE)),
                        (record.get(AGENCY_ID_LIST.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(AGENCY_ID_LIST.NAMESPACE_ID).toBigInteger()) : null,
                        agencyIdListValueManifestId,
                        record.get(AGENCY_ID_LIST_VALUE.NAME.as("agency_id_list_value_name")),
                        (byte) 1 == record.get(AGENCY_ID_LIST.IS_DEPRECATED),
                        CcState.valueOf(record.get(AGENCY_ID_LIST.STATE)),

                        fetchOwnerSummary(record),

                        (record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null,

                        getAgencyIdListValueSummaryList(agencyIdListManifestId)
                );
            };
        }
    }

    @Override
    public List<AgencyIdListSummaryRecord> availableAgencyIdListByDtManifestId(
            DtManifestId dtManifestId, List<CcState> states) {

        var dtQuery = repositoryFactory().dtQueryRepository(requester());
        DtSummaryRecord dt = dtQuery.getDtSummary(dtManifestId);

        Result<Record2<ULong, ULong>> result = dslContext().selectDistinct(
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        DT_MANIFEST.RELEASE_ID)
                .from(DT_MANIFEST)
                .join(DT_AWD_PRI).on(and(
                        DT_MANIFEST.RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID),
                        DT_MANIFEST.DT_ID.eq(DT_AWD_PRI.DT_ID)
                ))
                .join(AGENCY_ID_LIST_MANIFEST).on(and(
                        DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID),
                        DT_MANIFEST.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID)
                ))
                .join(AGENCY_ID_LIST).on(and(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID),
                        states.isEmpty() ? trueCondition() : AGENCY_ID_LIST.STATE.in(states)))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                            availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                                    new AgencyIdListManifestId(e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                                    new ReleaseId(e.get(AGENCY_ID_LIST_MANIFEST.RELEASE_ID).toBigInteger()), states))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(AgencyIdListSummaryRecord::name))
                    .collect(Collectors.toList());

        } else {
            return availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                    null, dt.release().releaseId(), states);
        }
    }

    @Override
    public List<AgencyIdListSummaryRecord> availableAgencyIdListByDtScManifestId(
            DtScManifestId dtScManifestId, List<CcState> states) {

        var dtQuery = repositoryFactory().dtQueryRepository(requester());
        DtScSummaryRecord dtSc = dtQuery.getDtScSummary(dtScManifestId);

        Result<Record2<ULong, ULong>> result = dslContext().selectDistinct(
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        DT_SC_MANIFEST.RELEASE_ID)
                .from(DT_SC_MANIFEST)
                .join(DT_SC_AWD_PRI).on(and(
                        DT_SC_MANIFEST.RELEASE_ID.eq(DT_SC_AWD_PRI.RELEASE_ID),
                        DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC_AWD_PRI.DT_SC_ID)
                ))
                .join(AGENCY_ID_LIST_MANIFEST).on(and(
                        DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID),
                        DT_SC_MANIFEST.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID)
                ))
                .join(AGENCY_ID_LIST).on(and(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID),
                        states.isEmpty() ? trueCondition() : AGENCY_ID_LIST.STATE.in(states)))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtScManifestId)))
                .fetch();

        if (result.size() > 0) {
            return result.stream().map(e ->
                            availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                                    new AgencyIdListManifestId(e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                                    new ReleaseId(e.get(AGENCY_ID_LIST_MANIFEST.RELEASE_ID).toBigInteger()), states))
                    .flatMap(e -> e.stream())
                    .distinct()
                    .sorted(Comparator.comparing(AgencyIdListSummaryRecord::name))
                    .collect(Collectors.toList());
        } else {
            return availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                    null, dtSc.release().releaseId(), states);
        }
    }

    private List<AgencyIdListSummaryRecord> availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
            AgencyIdListManifestId agencyIdListManifestId, ReleaseId releaseId, List<CcState> states) {

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        if (agencyIdListManifestId == null) {
            return queryBuilder.select()
                    .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                            states.isEmpty() ? trueCondition() : AGENCY_ID_LIST.STATE.in(states)
                    ))
                    .fetch(queryBuilder.mapper());
        }

        List<AgencyIdListSummaryRecord> availableAgencyIdLists = new ArrayList<>();
        availableAgencyIdLists.add(getAgencyIdListSummary(agencyIdListManifestId));

        List<AgencyIdListManifestId> associatedAgencyIdLists = dslContext().selectDistinct(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .from(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.in(
                        availableAgencyIdLists.stream()
                                .filter(e -> e.agencyIdListManifestId() != null)
                                .map(e -> e.agencyIdListManifestId())
                                .distinct()
                                .collect(Collectors.toList())
                ))
                .fetchStream().map(record ->
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()))
                .collect(Collectors.toList());

        List<AgencyIdListSummaryRecord> mergedAgencyIdLists = new ArrayList();
        mergedAgencyIdLists.addAll(availableAgencyIdLists);
        for (AgencyIdListManifestId associatedAgencyId : associatedAgencyIdLists) {
            mergedAgencyIdLists.addAll(
                    availableAgencyIdListByAgencyIdListManifestIdOrReleaseId(
                            associatedAgencyId, releaseId, states)
            );
        }
        return mergedAgencyIdLists.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryListInStates(
            ReleaseId releaseId, List<CcState> states) {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.RELEASE_ID.eq(valueOf(releaseId)));
        conditions.add(RELEASE.STATE.eq(ReleaseState.Published.name()));
        if (states != null && !states.isEmpty()) {
            conditions.add(or(
                    AGENCY_ID_LIST.STATE.in(states.stream().map(e -> e.name()).collect(Collectors.toSet())),
                    AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID.isNotNull()
            ));
        } else {
            conditions.add(AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID.isNotNull());
        }
        if (requester().isDeveloper()) {
            conditions.add(ownerTable().IS_DEVELOPER.eq((byte) 1));
        }

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryListByBccpManifestIdInStates(
            BccpManifestId bccpManifestId, List<CcState> states) {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)));
        if (states != null && !states.isEmpty()) {
            conditions.add(AGENCY_ID_LIST.STATE.in(
                    states.stream().map(e -> e.name()).collect(Collectors.toSet())
            ));
        }
        if (requester().isDeveloper()) {
            conditions.add(ownerTable().IS_DEVELOPER.eq((byte) 1));
        }

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .join(DT_AWD_PRI).on(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .join(DT_MANIFEST).on(and(
                        DT_AWD_PRI.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID),
                        DT_AWD_PRI.DT_ID.eq(DT_MANIFEST.DT_ID)
                ))
                .join(BCCP_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListSummaryRecord> getAgencyIdListSummaryListByDtScManifestIdInStates(
            DtScManifestId dtScManifestId, List<CcState> states) {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtScManifestId)));
        if (states != null && !states.isEmpty()) {
            conditions.add(AGENCY_ID_LIST.STATE.in(
                    states.stream().map(e -> e.name()).collect(Collectors.toSet())
            ));
        }
        if (requester().isDeveloper()) {
            conditions.add(ownerTable().IS_DEVELOPER.eq((byte) 1));
        }

        var queryBuilder = new GetAgencyIdListSummaryQueryBuilder();
        return queryBuilder.select()
                .join(DT_SC_AWD_PRI).on(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .join(DT_SC_MANIFEST).on(and(
                        DT_SC_AWD_PRI.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID),
                        DT_SC_AWD_PRI.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID)
                ))
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private List<AgencyIdListValueSummaryRecord> getAgencyIdListValueSummaryList(
            AgencyIdListManifestId agencyIdListManifestId) {

        var queryBuilder = new GetAgencyIdListValueSummaryQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListValueSummaryRecord> getAgencyIdListValueSummaryList() {
        var queryBuilder = new GetAgencyIdListValueSummaryQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AgencyIdListValueSummaryRecord> getAgencyIdListValueSummaryList(ReleaseId releaseId) {

        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetAgencyIdListValueSummaryQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public AgencyIdListValueSummaryRecord getAgencyIdListValueSummary(
            AgencyIdListValueManifestId agencyIdListValueManifestId) {

        var queryBuilder = new GetAgencyIdListValueSummaryQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(valueOf(agencyIdListValueManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAgencyIdListValueSummaryQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,

                            AGENCY_ID_LIST_VALUE.GUID,
                            AGENCY_ID_LIST_VALUE.VALUE,
                            AGENCY_ID_LIST_VALUE.NAME,
                            AGENCY_ID_LIST_VALUE.DEFINITION,
                            AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE,

                            AGENCY_ID_LIST_VALUE.IS_DEPRECATED,
                            AGENCY_ID_LIST_VALUE.IS_DEVELOPER_DEFAULT,
                            AGENCY_ID_LIST_VALUE.IS_USER_DEFAULT)
                    .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .join(AGENCY_ID_LIST_VALUE)
                    .on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID));
        }

        RecordMapper<org.jooq.Record, AgencyIdListValueSummaryRecord> mapper() {

            return record -> new AgencyIdListValueSummaryRecord(
                    new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()),
                    new AgencyIdListValueId(record.get(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID).toBigInteger()),
                    new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                    new Guid(record.get(AGENCY_ID_LIST_VALUE.GUID)),
                    record.get(AGENCY_ID_LIST_VALUE.VALUE),
                    record.get(AGENCY_ID_LIST_VALUE.NAME),
                    new Definition(record.get(AGENCY_ID_LIST_VALUE.DEFINITION), record.get(AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE)),
                    (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.IS_DEPRECATED),
                    (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.IS_DEVELOPER_DEFAULT),
                    (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.IS_USER_DEFAULT)
            );
        }

    }

    @Override
    public AgencyIdListDetailsRecord getAgencyIdListDetails(AgencyIdListManifestId agencyIdListManifestId) {

        if (agencyIdListManifestId == null) {
            return null;
        }

        var queryBuilder = new GetAgencyIdListDetailsQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAgencyIdListDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                            AGENCY_ID_LIST.GUID, AGENCY_ID_LIST.ENUM_TYPE_GUID,
                            AGENCY_ID_LIST.NAME,
                            AGENCY_ID_LIST.LIST_ID,
                            AGENCY_ID_LIST.VERSION_ID,

                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,

                            AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID.as("based_agency_id_list_manifest_id"),
                            AGENCY_ID_LIST.as("based_agency_id_list").AGENCY_ID_LIST_ID.as("based_agency_id_list_id"),
                            AGENCY_ID_LIST.as("based_agency_id_list").NAME.as("based_agency_id_list_name"),
                            AGENCY_ID_LIST.as("based_agency_id_list").STATE.as("based_agency_id_list_state"),

                            AGENCY_ID_LIST.DEFINITION, AGENCY_ID_LIST.DEFINITION_SOURCE,
                            AGENCY_ID_LIST.REMARK,
                            AGENCY_ID_LIST.IS_DEPRECATED,
                            iif(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull(), true, false).as("new_component"),
                            AGENCY_ID_LIST.STATE,
                            AGENCY_ID_LIST.CREATION_TIMESTAMP,
                            AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP,

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

                            AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(AGENCY_ID_LIST.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(AGENCY_ID_LIST.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(NAMESPACE).on(AGENCY_ID_LIST.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                    .leftJoin(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest")).on(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST.as("based_agency_id_list")).on(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.as("based_agency_id_list").AGENCY_ID_LIST_ID))
                    .leftJoin(MODULE_AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        RecordMapper<org.jooq.Record, AgencyIdListDetailsRecord> mapper() {
            return record -> {
                AgencyIdListManifestId agencyIdListManifestId =
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                AgencyIdListManifestId basedAgencyIdListManifestId =
                        (record.get(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID.as("based_agency_id_list_manifest_id")) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID.as("based_agency_id_list_manifest_id")).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(AGENCY_ID_LIST.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new AgencyIdListDetailsRecord(
                        library, release,
                        agencyIdListManifestId,
                        new AgencyIdListId(record.get(AGENCY_ID_LIST.AGENCY_ID_LIST_ID).toBigInteger()),
                        new Guid(record.get(AGENCY_ID_LIST.GUID)),
                        record.get(AGENCY_ID_LIST.ENUM_TYPE_GUID),
                        (basedAgencyIdListManifestId != null) ? getAgencyIdListSummary(basedAgencyIdListManifestId) : null,
                        record.get(AGENCY_ID_LIST.NAME),
                        record.get(AGENCY_ID_LIST.LIST_ID),
                        record.get(AGENCY_ID_LIST.VERSION_ID),

                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                getAgencyIdListValueSummary(new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger())) : null,

                        new Definition(record.get(AGENCY_ID_LIST.DEFINITION), record.get(AGENCY_ID_LIST.DEFINITION_SOURCE)),
                        record.get(AGENCY_ID_LIST.REMARK),
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        (byte) 1 == record.get(AGENCY_ID_LIST.IS_DEPRECATED),
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
                                toDate(record.get(AGENCY_ID_LIST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null,

                        getAgencyIdListValueDetailsList(agencyIdListManifestId)
                );
            };
        }
    }

    @Override
    public AgencyIdListDetailsRecord getPrevAgencyIdListDetails(AgencyIdListManifestId agencyIdListManifestId) {

        if (agencyIdListManifestId == null) {
            return null;
        }

        var queryBuilder = new GetAgencyIdListDetailsQueryBuilder();
        // For the record in the 'Release Draft' state,
        // since there are records with duplicate next manifest IDs from the existing previous release,
        // retrieve the first record after sorting by ID in descending order.
        AgencyIdListDetailsRecord prevAgencyIdListDetails = queryBuilder.select()
                .where(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .orderBy(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.desc())
                .fetchAny(queryBuilder.mapper());
        if (prevAgencyIdListDetails == null) {
            // In the case of an end-user, the new revision is created within the same Manifest and does not have a previous Manifest.
            // Therefore, the previous record must be retrieved based on the log.
            var prevQueryBuilder = new GetPrevAgencyIdListDetailsQueryBuilder();
            prevAgencyIdListDetails = prevQueryBuilder.select()
                    .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                    .fetchOne(prevQueryBuilder.mapper());
        }
        return prevAgencyIdListDetails;
    }

    private class GetPrevAgencyIdListDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.as("prev").AGENCY_ID_LIST_ID,
                            AGENCY_ID_LIST.as("prev").GUID,
                            AGENCY_ID_LIST.as("prev").ENUM_TYPE_GUID,
                            AGENCY_ID_LIST.as("prev").NAME,
                            AGENCY_ID_LIST.as("prev").LIST_ID,
                            AGENCY_ID_LIST.as("prev").VERSION_ID,

                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,

                            AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID.as("based_agency_id_list_manifest_id"),
                            AGENCY_ID_LIST.as("based_agency_id_list").AGENCY_ID_LIST_ID.as("based_agency_id_list_id"),
                            AGENCY_ID_LIST.as("based_agency_id_list").NAME.as("based_agency_id_list_name"),
                            AGENCY_ID_LIST.as("based_agency_id_list").STATE.as("based_agency_id_list_state"),

                            AGENCY_ID_LIST.as("prev").DEFINITION,
                            AGENCY_ID_LIST.as("prev").DEFINITION_SOURCE,
                            AGENCY_ID_LIST.as("prev").REMARK,
                            AGENCY_ID_LIST.as("prev").IS_DEPRECATED,
                            iif(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull(), true, false).as("new_component"),
                            AGENCY_ID_LIST.as("prev").STATE,
                            AGENCY_ID_LIST.as("prev").CREATION_TIMESTAMP,
                            AGENCY_ID_LIST.as("prev").LAST_UPDATE_TIMESTAMP,

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

                            AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(AGENCY_ID_LIST.as("prev")).on(and(
                            AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.as("prev").AGENCY_ID_LIST_ID),
                            AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.as("prev").NEXT_AGENCY_ID_LIST_ID)
                    ))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(AGENCY_ID_LIST.as("prev").OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(AGENCY_ID_LIST.as("prev").CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(AGENCY_ID_LIST.as("prev").LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
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
                    .leftJoin(NAMESPACE).on(AGENCY_ID_LIST.as("prev").NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                    .leftJoin(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest")).on(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST.as("based_agency_id_list")).on(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.as("based_agency_id_list").AGENCY_ID_LIST_ID))
                    .leftJoin(MODULE_AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        RecordMapper<org.jooq.Record, AgencyIdListDetailsRecord> mapper() {
            return record -> {
                AgencyIdListManifestId agencyIdListManifestId =
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                AgencyIdListManifestId basedAgencyIdListManifestId =
                        (record.get(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID.as("based_agency_id_list_manifest_id")) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.as("based_agency_id_list_manifest").AGENCY_ID_LIST_MANIFEST_ID.as("based_agency_id_list_manifest_id")).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(AGENCY_ID_LIST.as("prev").STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new AgencyIdListDetailsRecord(
                        library, release,
                        agencyIdListManifestId,
                        new AgencyIdListId(record.get(AGENCY_ID_LIST.as("prev").AGENCY_ID_LIST_ID).toBigInteger()),
                        new Guid(record.get(AGENCY_ID_LIST.as("prev").GUID)),
                        record.get(AGENCY_ID_LIST.as("prev").ENUM_TYPE_GUID),
                        (basedAgencyIdListManifestId != null) ? getAgencyIdListSummary(basedAgencyIdListManifestId) : null,
                        record.get(AGENCY_ID_LIST.as("prev").NAME),
                        record.get(AGENCY_ID_LIST.as("prev").LIST_ID),
                        record.get(AGENCY_ID_LIST.as("prev").VERSION_ID),

                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                getAgencyIdListValueSummary(new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger())) : null,

                        new Definition(record.get(AGENCY_ID_LIST.as("prev").DEFINITION), record.get(AGENCY_ID_LIST.as("prev").DEFINITION_SOURCE)),
                        record.get(AGENCY_ID_LIST.as("prev").REMARK),
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        (byte) 1 == record.get(AGENCY_ID_LIST.as("prev").IS_DEPRECATED),
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
                                toDate(record.get(AGENCY_ID_LIST.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(AGENCY_ID_LIST.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null,

                        getPrevAgencyIdListValueDetailsList(agencyIdListManifestId)
                );
            };
        }
    }

    @Override
    public List<AgencyIdListValueDetailsRecord> getAgencyIdListValueDetailsList(
            AgencyIdListManifestId agencyIdListManifestId) {

        var queryBuilder = new GetAgencyIdListValueDetailsQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public AgencyIdListValueDetailsRecord getAgencyIdListValueDetails(
            AgencyIdListValueManifestId agencyIdListValueManifestId) {

        var queryBuilder = new GetAgencyIdListValueDetailsQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(valueOf(agencyIdListValueManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAgencyIdListValueDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,

                            AGENCY_ID_LIST_VALUE.GUID,
                            AGENCY_ID_LIST_VALUE.VALUE,
                            AGENCY_ID_LIST_VALUE.NAME,
                            AGENCY_ID_LIST_VALUE.DEFINITION,
                            AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE,
                            AGENCY_ID_LIST_VALUE.IS_DEPRECATED,
                            AGENCY_ID_LIST_VALUE.IS_DEVELOPER_DEFAULT,
                            AGENCY_ID_LIST_VALUE.IS_USER_DEFAULT,
                            AGENCY_ID_LIST_VALUE.CREATION_TIMESTAMP,
                            AGENCY_ID_LIST_VALUE.LAST_UPDATE_TIMESTAMP,

                            AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                    .join(ownerTable()).on(AGENCY_ID_LIST_VALUE.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(AGENCY_ID_LIST_VALUE.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(AGENCY_ID_LIST_VALUE.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, AgencyIdListValueDetailsRecord> mapper() {
            return record -> {
                AgencyIdListValueManifestId agencyIdListValueManifestId =
                        new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger());
                return new AgencyIdListValueDetailsRecord(
                        agencyIdListValueManifestId,
                        new AgencyIdListValueId(record.get(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID).toBigInteger()),
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),

                        new Guid(record.get(AGENCY_ID_LIST_VALUE.GUID)),
                        record.get(AGENCY_ID_LIST_VALUE.VALUE),
                        record.get(AGENCY_ID_LIST_VALUE.NAME),

                        new Definition(record.get(AGENCY_ID_LIST_VALUE.DEFINITION), record.get(AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE)),
                        (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.IS_DEPRECATED),
                        (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.IS_DEVELOPER_DEFAULT),
                        (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.IS_USER_DEFAULT),
                        isUsed(agencyIdListValueManifestId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(AGENCY_ID_LIST_VALUE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(AGENCY_ID_LIST_VALUE.LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    private boolean isUsed(AgencyIdListManifestId agencyIdListManifestId) {
        return dslContext().selectCount()
                .from(DT_AWD_PRI)
                .where(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(DT_SC_AWD_PRI)
                        .where(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                        .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(BBIE)
                        .where(BBIE.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                        .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(BBIE_SC)
                        .where(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                        .fetchOneInto(Integer.class) > 0;
    }

    private boolean isUsed(AgencyIdListValueManifestId agencyIdListValueManifestId) {
        return dslContext().selectCount()
                .from(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(valueOf(agencyIdListValueManifestId)))
                .fetchOneInto(Integer.class) > 0 ||
                dslContext().selectCount()
                        .from(CODE_LIST_MANIFEST)
                        .where(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(valueOf(agencyIdListValueManifestId)))
                        .fetchOneInto(Integer.class) > 0;
    }

    private List<AgencyIdListValueDetailsRecord> getPrevAgencyIdListValueDetailsList(
            AgencyIdListManifestId agencyIdListManifestId) {

        var queryBuilder = new GetPrevAgencyIdListValueDetailsQueryBuilder();
        return queryBuilder.select()
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetPrevAgencyIdListValueDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            AGENCY_ID_LIST_VALUE.as("prev").AGENCY_ID_LIST_VALUE_ID,
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,

                            AGENCY_ID_LIST_VALUE.as("prev").GUID,
                            AGENCY_ID_LIST_VALUE.as("prev").VALUE,
                            AGENCY_ID_LIST_VALUE.as("prev").NAME,
                            AGENCY_ID_LIST_VALUE.as("prev").DEFINITION,
                            AGENCY_ID_LIST_VALUE.as("prev").DEFINITION_SOURCE,
                            AGENCY_ID_LIST_VALUE.as("prev").IS_DEPRECATED,
                            AGENCY_ID_LIST_VALUE.as("prev").IS_DEVELOPER_DEFAULT,
                            AGENCY_ID_LIST_VALUE.as("prev").IS_USER_DEFAULT,
                            AGENCY_ID_LIST_VALUE.as("prev").CREATION_TIMESTAMP,
                            AGENCY_ID_LIST_VALUE.as("prev").LAST_UPDATE_TIMESTAMP,

                            AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                    .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .join(AGENCY_ID_LIST_VALUE).on(and(
                            AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID),
                            AGENCY_ID_LIST_VALUE.PREV_AGENCY_ID_LIST_VALUE_ID.isNotNull()
                    ))
                    .join(AGENCY_ID_LIST_VALUE.as("prev")).on(and(
                            AGENCY_ID_LIST_VALUE.PREV_AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.as("prev").AGENCY_ID_LIST_VALUE_ID),
                            AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.as("prev").NEXT_AGENCY_ID_LIST_VALUE_ID)
                    ))
                    .join(ownerTable()).on(AGENCY_ID_LIST_VALUE.as("prev").OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(AGENCY_ID_LIST_VALUE.as("prev").CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(AGENCY_ID_LIST_VALUE.as("prev").LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<org.jooq.Record, AgencyIdListValueDetailsRecord> mapper() {
            return record -> {
                AgencyIdListValueManifestId agencyIdListValueManifestId =
                        new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger());
                return new AgencyIdListValueDetailsRecord(
                        agencyIdListValueManifestId,
                        new AgencyIdListValueId(record.get(AGENCY_ID_LIST_VALUE.as("prev").AGENCY_ID_LIST_VALUE_ID).toBigInteger()),
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),

                        new Guid(record.get(AGENCY_ID_LIST_VALUE.as("prev").GUID)),
                        record.get(AGENCY_ID_LIST_VALUE.as("prev").VALUE),
                        record.get(AGENCY_ID_LIST_VALUE.as("prev").NAME),
                        new Definition(record.get(AGENCY_ID_LIST_VALUE.as("prev").DEFINITION), record.get(AGENCY_ID_LIST_VALUE.as("prev").DEFINITION_SOURCE)),
                        (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.as("prev").IS_DEPRECATED),
                        (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.as("prev").IS_DEVELOPER_DEFAULT),
                        (byte) 1 == record.get(AGENCY_ID_LIST_VALUE.as("prev").IS_USER_DEFAULT),
                        isUsed(agencyIdListValueManifestId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(AGENCY_ID_LIST_VALUE.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(AGENCY_ID_LIST_VALUE.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) ?
                                new AgencyIdListValueManifestId(record.get(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }


    @Override
    public ResultAndCount<AgencyIdListListEntryRecord> getAgencyIdListList(
            AgencyIdListListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var queryBuilder = new GetAgencyIdListListQueryBuilder();
        var where = queryBuilder.select().where(
                queryBuilder.conditions(filterCriteria));
        int count = dslContext().fetchCount(where);
        List<AgencyIdListListEntryRecord> result = queryBuilder.fetch(where, pageRequest);
        return new ResultAndCount(result, count);
    }

    private class GetAgencyIdListListQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                            AGENCY_ID_LIST.GUID,
                            AGENCY_ID_LIST.ENUM_TYPE_GUID,
                            AGENCY_ID_LIST.NAME,
                            AGENCY_ID_LIST.LIST_ID,
                            AGENCY_ID_LIST.VERSION_ID,

                            AGENCY_ID_LIST.DEFINITION,
                            AGENCY_ID_LIST.DEFINITION_SOURCE,
                            AGENCY_ID_LIST.REMARK,
                            AGENCY_ID_LIST.NAMESPACE_ID,
                            AGENCY_ID_LIST.IS_DEPRECATED,
                            iif(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull(), true, false).as("new_component"),
                            AGENCY_ID_LIST.STATE,
                            AGENCY_ID_LIST.CREATION_TIMESTAMP,
                            AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP,

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

                            AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ownerTable()).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(ownerTablePk()))
                    .join(creatorTable()).on(AGENCY_ID_LIST.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(AGENCY_ID_LIST.LAST_UPDATED_BY.eq(updaterTablePk()))
                    .leftJoin(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                    .leftJoin(MODULE_AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(MODULE_SET_RELEASE).on(and(
                            MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID),
                            MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)
                    ))
                    .leftJoin(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
        }

        public List<Condition> conditions(AgencyIdListListFilterCriteria filterCriteria) {
            List<Condition> conditions = new ArrayList();
            conditions.add(LIBRARY.LIBRARY_ID.eq(valueOf(filterCriteria.libraryId())));
            conditions.add(RELEASE.RELEASE_ID.eq(valueOf(filterCriteria.releaseId())));

            if (hasLength(filterCriteria.name())) {
                conditions.addAll(contains(filterCriteria.name(), AGENCY_ID_LIST.NAME));
            }
            if (hasLength(filterCriteria.module())) {
                conditions.addAll(contains(filterCriteria.module(), MODULE.PATH));
            }
            if (hasLength(filterCriteria.definition())) {
                conditions.addAll(contains(filterCriteria.definition(), AGENCY_ID_LIST.DEFINITION));
            }
            if (filterCriteria.states() != null && !filterCriteria.states().isEmpty()) {
                conditions.add(AGENCY_ID_LIST.STATE.in(filterCriteria.states().stream().map(e -> e.name()).collect(Collectors.toSet())));
            }
            if (filterCriteria.deprecated() != null) {
                conditions.add(AGENCY_ID_LIST.IS_DEPRECATED.eq((byte) (filterCriteria.deprecated() ? 1 : 0)));
            }
            if (filterCriteria.ownedByDeveloper() != null) {
                conditions.add(ownerTable().IS_DEVELOPER.eq((byte) (filterCriteria.ownedByDeveloper() ? 1 : 0)));
            }
            if (filterCriteria.newComponent() != null) {
                conditions.add(filterCriteria.newComponent() ?
                        AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull() :
                        AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNotNull());
            }
            if (filterCriteria.namespaces() != null && !filterCriteria.namespaces().isEmpty()) {
                conditions.add(AGENCY_ID_LIST.NAMESPACE_ID.in(valueOf(filterCriteria.namespaces())));
            }
            if (filterCriteria.ownerLoginIdSet() != null && !filterCriteria.ownerLoginIdSet().isEmpty()) {
                conditions.add(ownerTable().LOGIN_ID.in(filterCriteria.ownerLoginIdSet()));
            }
            if (filterCriteria.updaterLoginIdSet() != null && !filterCriteria.updaterLoginIdSet().isEmpty()) {
                conditions.add(updaterTable().LOGIN_ID.in(filterCriteria.updaterLoginIdSet()));
            }
            if (filterCriteria.lastUpdatedTimestampRange() != null) {
                if (filterCriteria.lastUpdatedTimestampRange().after() != null) {
                    conditions.add(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                            new Timestamp(filterCriteria.lastUpdatedTimestampRange().after().getTime()).toLocalDateTime()));
                }
                if (filterCriteria.lastUpdatedTimestampRange().before() != null) {
                    conditions.add(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP.lessThan(
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
                    case "state":
                        field = AGENCY_ID_LIST.STATE;
                        break;

                    case "name":
                        field = AGENCY_ID_LIST.NAME;
                        break;

                    case "version":
                    case "versionId":
                        field = AGENCY_ID_LIST.VERSION_ID;
                        break;

                    case "revision":
                        field = LOG.REVISION_NUM;
                        break;

                    case "owner":
                        field = APP_USER.as("owner").LOGIN_ID;
                        break;

                    case "module":
                        field = MODULE.PATH;
                        break;

                    case "lastUpdateTimestamp":
                        field = AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP;
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

        public List<AgencyIdListListEntryRecord> fetch(SelectConditionStep<?> conditionStep, PageRequest pageRequest) {
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

        RecordMapper<org.jooq.Record, AgencyIdListListEntryRecord> mapper() {
            return record -> {
                AgencyIdListManifestId agencyIdListManifestId =
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
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
                CcState state = CcState.valueOf(record.get(AGENCY_ID_LIST.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new AgencyIdListListEntryRecord(
                        library, release,
                        agencyIdListManifestId,
                        new AgencyIdListId(record.get(AGENCY_ID_LIST.AGENCY_ID_LIST_ID).toBigInteger()),

                        new Guid(record.get(AGENCY_ID_LIST.GUID)),
                        record.get(AGENCY_ID_LIST.ENUM_TYPE_GUID),
                        record.get(AGENCY_ID_LIST.NAME),
                        record.get(AGENCY_ID_LIST.LIST_ID),
                        record.get(AGENCY_ID_LIST.VERSION_ID),
                        record.get(AGENCY_ID_LIST.DEFINITION),
                        record.get(AGENCY_ID_LIST.DEFINITION_SOURCE),
                        record.get(AGENCY_ID_LIST.REMARK),
                        (record.get(AGENCY_ID_LIST.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(AGENCY_ID_LIST.NAMESPACE_ID).toBigInteger()) : null,
                        (byte) 1 == record.get(AGENCY_ID_LIST.IS_DEPRECATED),
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
                                toDate(record.get(AGENCY_ID_LIST.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP))
                        ),

                        (record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public boolean hasRecordsByNamespaceId(NamespaceId namespaceId) {
        return dslContext().selectCount()
                .from(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public int countActiveAgencyIdLists(AgencyIdListManifestId excludedAgencyIdListManifestId) {
        var releaseIdToCheck = dslContext()
                .select(AGENCY_ID_LIST_MANIFEST.RELEASE_ID)
                .from(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(excludedAgencyIdListManifestId)))
                .fetchOneInto(ULong.class);

        return dslContext().selectCount()
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(Tables.AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseIdToCheck),
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.notEqual(valueOf(excludedAgencyIdListManifestId)),
                        AGENCY_ID_LIST.STATE.notEqual("Deleted")
                ))
                .fetchOptionalInto(Integer.class).orElse(0);
    }

    @Override
    public boolean hasSameAgencyIdList(ReleaseId releaseId,
                                       AgencyIdListManifestId agencyIdListManifestId,
                                       AgencyIdListValueManifestId agencyIdListValueManifestId,
                                       String listId, String versionId) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                AGENCY_ID_LIST.STATE.notEqual(CcState.Deleted.name())
        ));
        if (agencyIdListManifestId != null) {
            conditions.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.ne(valueOf(agencyIdListManifestId)));
        }
        if (agencyIdListValueManifestId == null) {
            conditions.add(and(
                    AGENCY_ID_LIST.LIST_ID.eq(listId),
                    AGENCY_ID_LIST.VERSION_ID.eq(versionId),
                    AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID.isNull()
            ));
            return dslContext().selectCount()
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .where(conditions).fetchOneInto(Integer.class) > 0;
        } else {
            AgencyIdListValueSummaryRecord agencyIdListValueSummary =
                    getAgencyIdListValueSummary(agencyIdListValueManifestId);

            conditions.add(and(
                    AGENCY_ID_LIST.LIST_ID.eq(listId),
                    AGENCY_ID_LIST.VERSION_ID.eq(versionId),
                    AGENCY_ID_LIST_VALUE.NAME.eq(agencyIdListValueSummary.name()),
                    AGENCY_ID_LIST_VALUE.VALUE.eq(agencyIdListValueSummary.value())
            ));

            return dslContext().selectCount()
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                    .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                    .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                    .where(conditions).fetchOneInto(Integer.class) > 0;
        }
    }

    @Override
    public boolean hasSameNameAgencyIdList(ReleaseId releaseId,
                                           AgencyIdListManifestId agencyIdListManifestId,
                                           String agencyIdListName) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                AGENCY_ID_LIST.STATE.notEqual(CcState.Deleted.name())
        ));

        if (agencyIdListManifestId != null) {
            conditions.add(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.ne(valueOf(agencyIdListManifestId)));
        }
        conditions.add(AGENCY_ID_LIST.NAME.eq(agencyIdListName));

        return dslContext().selectCount()
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(conditions).fetchOneInto(Integer.class) > 0;
    }

}
