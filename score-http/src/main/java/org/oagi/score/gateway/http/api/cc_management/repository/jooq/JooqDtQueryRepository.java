package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.*;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.Revised;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

public class JooqDtQueryRepository extends JooqBaseRepository implements DtQueryRepository {

    private final ReleaseQueryRepository releaseQueryRepository;

    public JooqDtQueryRepository(DSLContext dslContext,
                                 ScoreUser requester,
                                 RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.releaseQueryRepository = repositoryFactory.releaseQueryRepository(requester);
    }

    @Override
    public DtDetailsRecord getDtDetails(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return null;
        }

        var queryBuilder = new GetDtDetailsQueryBuilder();
        return queryBuilder.select()
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetDtDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            DT_MANIFEST.DT_MANIFEST_ID,
                            DT.DT_ID,
                            DT_MANIFEST.BASED_DT_MANIFEST_ID,
                            DT.GUID,
                            DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID,

                            DT_MANIFEST.DEN,
                            DT.DATA_TYPE_TERM,
                            DT.QUALIFIER,
                            DT.REPRESENTATION_TERM,
                            DT.SIX_DIGIT_ID,
                            DT.CONTENT_COMPONENT_DEFINITION,
                            DT.DEFINITION,
                            DT.DEFINITION_SOURCE,
                            DT.COMMONLY_USED,
                            DT.IS_DEPRECATED,
                            DT.STATE,
                            DT.CREATION_TIMESTAMP,
                            DT.LAST_UPDATE_TIMESTAMP,

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

                            DT_MANIFEST.PREV_DT_MANIFEST_ID,
                            DT_MANIFEST.NEXT_DT_MANIFEST_ID,
                            DT.PREV_DT_ID,
                            DT.NEXT_DT_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(DT_MANIFEST)
                    .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(DT.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(DT.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(DT.LAST_UPDATED_BY))
                    .leftJoin(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(NAMESPACE).on(DT.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID));
        }

        private RecordMapper<Record, DtDetailsRecord> mapper() {
            return record -> {
                DtManifestId dtManifestId = new DtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
                DtId dtId = new DtId(record.get(DT.DT_ID).toBigInteger());
                DtManifestId basedDtManifestId = (record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID).toBigInteger()) : null;
                DtManifestId replacementDtManifestId = (record.get(DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(DT.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new DtDetailsRecord(
                        library, release,
                        dtManifestId,
                        dtId,
                        new Guid(record.get(BCCP.GUID)),
                        (basedDtManifestId != null) ? getDtSummary(basedDtManifestId) : null,
                        getDtSummary(replacementDtManifestId),
                        getDtSummary(since(dtManifestId)),
                        getDtSummary(lastChanged(dtManifestId)),

                        record.get(DT_MANIFEST.DEN),
                        record.get(DT.DATA_TYPE_TERM),
                        record.get(DT.QUALIFIER),
                        record.get(DT.REPRESENTATION_TERM),
                        record.get(DT.SIX_DIGIT_ID),
                        hasChild(dtManifestId),
                        (byte) 1 == record.get(DT.COMMONLY_USED),
                        (byte) 1 == record.get(DT.IS_DEPRECATED),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        record.get(DT.CONTENT_COMPONENT_DEFINITION),
                        new Definition(
                                record.get(DT.DEFINITION),
                                record.get(DT.DEFINITION_SOURCE)),
                        getDtAwdPriDetailsList(release.releaseId(), dtId),
                        AccessPrivilege.toAccessPrivilege(
                                requester(), owner.userId(),
                                state, release.isWorkingRelease()),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(DT.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(DT.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID) != null) ?
                                new DtManifestId(record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID) != null) ?
                                new DtManifestId(record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(DT.PREV_DT_ID) != null) ?
                                new DtId(record.get(DT.PREV_DT_ID).toBigInteger()) : null,
                        (record.get(DT.NEXT_DT_ID) != null) ?
                                new DtId(record.get(DT.NEXT_DT_ID).toBigInteger()) : null
                );
            };
        }
    }

    private List<DtAwdPriDetailsRecord> getDtAwdPriDetailsList(ReleaseId releaseId, DtId dtId) {
        var queryBuilder = new GetDtAwdPriDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        DT_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_AWD_PRI.DT_ID.eq(valueOf(dtId))
                ))
                .fetch(queryBuilder.mapper());
    }

    private class GetDtAwdPriDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(DT_AWD_PRI.DT_AWD_PRI_ID,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            DT_AWD_PRI.DT_ID,
                            DT_AWD_PRI.XBT_MANIFEST_ID,
                            DT_AWD_PRI.CODE_LIST_MANIFEST_ID,
                            DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                            DT_AWD_PRI.IS_DEFAULT)
                    .from(DT_AWD_PRI)
                    .join(RELEASE).on(DT_AWD_PRI.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID));
        }

        private RecordMapper<Record, DtAwdPriDetailsRecord> mapper() {
            return record -> {
                DtAwdPriId dtAwdPriId = new DtAwdPriId(record.get(DT_AWD_PRI.DT_AWD_PRI_ID).toBigInteger());
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                DtId dtId = new DtId(record.get(DT_AWD_PRI.DT_ID).toBigInteger());
                XbtManifestId xbtManifestId = (record.get(DT_AWD_PRI.XBT_MANIFEST_ID) != null) ?
                        new XbtManifestId(record.get(DT_AWD_PRI.XBT_MANIFEST_ID).toBigInteger()) : null;
                CodeListManifestId codeListManifestId = (record.get(DT_AWD_PRI.CODE_LIST_MANIFEST_ID) != null) ?
                        new CodeListManifestId(record.get(DT_AWD_PRI.CODE_LIST_MANIFEST_ID).toBigInteger()) : null;
                AgencyIdListManifestId agencyIdListManifestId = (record.get(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                        new AgencyIdListManifestId(record.get(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null;

                return new DtAwdPriDetailsRecord(dtAwdPriId,
                        release,
                        dtId,
                        (xbtManifestId != null) ?
                                repositoryFactory().xbtQueryRepository(requester()).getXbtSummary(xbtManifestId) : null,
                        (codeListManifestId != null) ?
                                repositoryFactory().codeListQueryRepository(requester()).getCodeListSummary(codeListManifestId) : null,
                        (agencyIdListManifestId != null) ?
                                repositoryFactory().agencyIdListQueryRepository(requester()).getAgencyIdListSummary(agencyIdListManifestId) : null,
                        (byte) 1 == record.get(DT_AWD_PRI.IS_DEFAULT),
                        isInherited(dtId, xbtManifestId, codeListManifestId, agencyIdListManifestId)
                );
            };
        }
    }

    private boolean isInherited(DtId dtId,
                                XbtManifestId xbtManifestId,
                                CodeListManifestId codeListManifestId,
                                AgencyIdListManifestId agencyIdListManifestId) {

        ULong basedDtId = dslContext().select(DT.BASED_DT_ID)
                .from(DT)
                .where(DT.DT_ID.eq(valueOf(dtId)))
                .fetchOptionalInto(ULong.class).orElse(null);
        if (basedDtId == null) {
            return false;
        }

        return dslContext().selectCount()
                .from(DT_AWD_PRI)
                .where(and(
                                DT_AWD_PRI.DT_ID.eq(basedDtId)),
                        or(
                                DT_AWD_PRI.XBT_MANIFEST_ID.eq(valueOf(xbtManifestId)),
                                DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)),
                                DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId))
                        )
                )
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    private DtManifestId since(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return null;
        }
        Record2<ULong, ULong> record = dslContext().select(
                        DT_MANIFEST.DT_MANIFEST_ID,
                        DT_MANIFEST.PREV_DT_MANIFEST_ID)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ULong prevDtManifestId = record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID);
        if (prevDtManifestId != null) {
            return since(new DtManifestId(prevDtManifestId.toBigInteger()));
        } else {
            return new DtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
        }
    }

    private DtManifestId lastChanged(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return null;
        }
        Record4<ULong, ULong, ULong, ULong> record = dslContext().select(
                        DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DT_ID,
                        DT_MANIFEST.as("prev").DT_MANIFEST_ID, DT_MANIFEST.as("prev").DT_ID)
                .from(DT_MANIFEST)
                .join(DT_MANIFEST.as("prev")).on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID))
                .join(RELEASE).on(DT_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        if (record.get(DT_MANIFEST.DT_ID).equals(record.get(DT_MANIFEST.as("prev").DT_ID))) {
            return lastChanged(new DtManifestId(record.get(DT_MANIFEST.as("prev").DT_MANIFEST_ID).toBigInteger()));
        } else {
            return new DtManifestId(record.get(DT_MANIFEST.as("prev").DT_MANIFEST_ID).toBigInteger());
        }
    }

    @Override
    public DtDetailsRecord getPrevDtDetails(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return null;
        }
        var queryBuilder = new GetDtDetailsQueryBuilder();
        DtDetailsRecord prevDtDetails = queryBuilder.select()
                .where(DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetchOne(queryBuilder.mapper());
        if (prevDtDetails == null) {
            // In the case of an end-user, the new revision is created within the same Manifest and does not have a previous Manifest.
            // Therefore, the previous record must be retrieved based on the log.
            var prevQueryBuilder = new GetPrevDtDetailsQueryBuilder();
            prevDtDetails = prevQueryBuilder.select()
                    .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                    .fetchOne(prevQueryBuilder.mapper());
        }

        return prevDtDetails;
    }

    private class GetPrevDtDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            DT_MANIFEST.DT_MANIFEST_ID,
                            DT.as("prev").DT_ID,
                            DT_MANIFEST.BASED_DT_MANIFEST_ID,
                            DT.as("prev").GUID,
                            DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID,

                            DT_MANIFEST.DEN,
                            DT.as("prev").DATA_TYPE_TERM,
                            DT.as("prev").QUALIFIER,
                            DT.as("prev").REPRESENTATION_TERM,
                            DT.as("prev").SIX_DIGIT_ID,
                            DT.as("prev").CONTENT_COMPONENT_DEFINITION,
                            DT.as("prev").DEFINITION,
                            DT.as("prev").DEFINITION_SOURCE,
                            DT.as("prev").COMMONLY_USED,
                            DT.as("prev").IS_DEPRECATED,
                            DT.as("prev").STATE,
                            DT.as("prev").CREATION_TIMESTAMP,
                            DT.as("prev").LAST_UPDATE_TIMESTAMP,

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

                            DT_MANIFEST.PREV_DT_MANIFEST_ID,
                            DT_MANIFEST.NEXT_DT_MANIFEST_ID,
                            DT.as("prev").PREV_DT_ID,
                            DT.as("prev").NEXT_DT_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(DT_MANIFEST)
                    .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                    .join(DT.as("prev")).on(and(
                            DT.PREV_DT_ID.eq(DT.as("prev").DT_ID),
                            DT.DT_ID.eq(DT.as("prev").NEXT_DT_ID)
                    ))
                    .join(ownerTable()).on(ownerTablePk().eq(DT.as("prev").OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(DT.as("prev").CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(DT.as("prev").LAST_UPDATED_BY))
                    .leftJoin(NAMESPACE).on(DT.as("prev").NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(LOG.as("revised_log")).on(and(
                            LOG.REFERENCE.eq(LOG.as("revised_log").REFERENCE),
                            LOG.REVISION_NUM.eq(LOG.as("revised_log").REVISION_NUM),
                            LOG.as("revised_log").REVISION_TRACKING_NUM.eq(UInteger.valueOf(1)),
                            LOG.as("revised_log").LOG_ACTION.eq(Revised.name())
                    ))
                    .leftJoin(LOG.as("prev_log")).on(
                            LOG.as("revised_log").PREV_LOG_ID.eq(LOG.as("prev_log").LOG_ID),
                            LOG.as("revised_log").LOG_ID.eq(LOG.as("prev_log").NEXT_LOG_ID)
                    );
        }

        private RecordMapper<Record, DtDetailsRecord> mapper() {
            return record -> {
                DtManifestId dtManifestId = new DtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
                DtId dtId = new DtId(record.get(DT.as("prev").DT_ID).toBigInteger());
                DtManifestId basedDtManifestId = (record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID).toBigInteger()) : null;
                DtManifestId replacementDtManifestId = (record.get(DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(DT.as("prev").STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new DtDetailsRecord(
                        library, release,
                        dtManifestId,
                        dtId,
                        new Guid(record.get(BCCP.GUID)),
                        (basedDtManifestId != null) ? getDtSummary(basedDtManifestId) : null,
                        getDtSummary(replacementDtManifestId),
                        getDtSummary(since(dtManifestId)),
                        getDtSummary(lastChanged(dtManifestId)),

                        record.get(DT_MANIFEST.DEN),
                        record.get(DT.as("prev").DATA_TYPE_TERM),
                        record.get(DT.as("prev").QUALIFIER),
                        record.get(DT.as("prev").REPRESENTATION_TERM),
                        record.get(DT.as("prev").SIX_DIGIT_ID),
                        hasChild(dtManifestId),
                        (byte) 1 == record.get(DT.as("prev").COMMONLY_USED),
                        (byte) 1 == record.get(DT.as("prev").IS_DEPRECATED),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        record.get(DT.as("prev").CONTENT_COMPONENT_DEFINITION),
                        new Definition(
                                record.get(DT.as("prev").DEFINITION),
                                record.get(DT.as("prev").DEFINITION_SOURCE)),
                        getDtAwdPriDetailsList(release.releaseId(), dtId),
                        AccessPrivilege.toAccessPrivilege(
                                requester(), owner.userId(),
                                state, release.isWorkingRelease()),

                        (record.get(LOG.as("prev_log").LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.as("prev_log").LOG_ID).toBigInteger()),
                                record.get(LOG.as("prev_log").REVISION_NUM).intValue(),
                                record.get(LOG.as("prev_log").REVISION_TRACKING_NUM).intValue()) : null,

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(DT.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(DT.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID) != null) ?
                                new DtManifestId(record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID) != null) ?
                                new DtManifestId(record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(DT.as("prev").PREV_DT_ID) != null) ?
                                new DtId(record.get(DT.as("prev").PREV_DT_ID).toBigInteger()) : null,
                        (record.get(DT.as("prev").NEXT_DT_ID) != null) ?
                                new DtId(record.get(DT.as("prev").NEXT_DT_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public DtSummaryRecord getDtSummary(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return null;
        }

        var queryBuilder = new GetDtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<DtSummaryRecord> getDtSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetDtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtSummaryRecord> getDtSummaryList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtSummaryRecord> getDtSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(DT.STATE.eq(state.name()));
        }
        var queryBuilder = new GetDtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtSummaryRecord> getInheritedDtSummaryList(DtManifestId basedDtManifestId) {
        if (basedDtManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetDtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(valueOf(basedDtManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetDtSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            DT_MANIFEST.DT_MANIFEST_ID,
                            DT.DT_ID,
                            DT_MANIFEST.BASED_DT_MANIFEST_ID,
                            DT.GUID,

                            DT_MANIFEST.DEN,
                            DT.DATA_TYPE_TERM,
                            DT.QUALIFIER,
                            DT.REPRESENTATION_TERM,
                            DT.SIX_DIGIT_ID,
                            DT.CONTENT_COMPONENT_DEFINITION,
                            DT.DEFINITION,
                            DT.DEFINITION_SOURCE,
                            DT.COMMONLY_USED,
                            DT.IS_DEPRECATED,
                            DT.STATE,

                            DT.NAMESPACE_ID,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.REVISION_NUM,

                            DT_MANIFEST.PREV_DT_MANIFEST_ID,
                            DT_MANIFEST.NEXT_DT_MANIFEST_ID
                    ), ownerFields()))
                    .from(DT_MANIFEST)
                    .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(DT.OWNER_USER_ID))
                    .leftJoin(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
        }

        private RecordMapper<Record, DtSummaryRecord> mapper() {
            return record -> {
                DtManifestId dtManifestId = new DtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
                DtId dtId = new DtId(record.get(DT.DT_ID).toBigInteger());
                DtManifestId basedDtManifestId = (record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID).toBigInteger()) : null;
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
                return new DtSummaryRecord(
                        library, release,
                        dtManifestId,
                        dtId,
                        new Guid(record.get(DT.GUID)),
                        basedDtManifestId,

                        record.get(DT_MANIFEST.DEN),
                        record.get(DT.DATA_TYPE_TERM),
                        record.get(DT.QUALIFIER),
                        record.get(DT.REPRESENTATION_TERM),
                        record.get(DT.SIX_DIGIT_ID),
                        (byte) 1 == record.get(DT.COMMONLY_USED),
                        (byte) 1 == record.get(DT.IS_DEPRECATED),
                        CcState.valueOf(record.get(DT.STATE)),
                        (record.get(DT.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(DT.NAMESPACE_ID).toBigInteger()) : null,
                        record.get(DT.CONTENT_COMPONENT_DEFINITION),
                        new Definition(
                                record.get(DT.DEFINITION),
                                record.get(DT.DEFINITION_SOURCE)),

                        (record.get(LOG.REVISION_NUM) != null) ? record.get(LOG.REVISION_NUM).intValue() : 1,

                        fetchOwnerSummary(record),

                        (record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID) != null) ?
                                new DtManifestId(record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID) != null) ?
                                new DtManifestId(record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetDtAwdPriSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_AWD_PRI.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtAwdPriSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return Collections.emptyList();
        }

        DtSummaryRecord dt = getDtSummary(dtManifestId);
        if (dt == null) {
            return Collections.emptyList();
        }

        return getDtAwdPriSummaryList(dt.release().releaseId(), dt.dtId());
    }

    @Override
    public DtAwdPriSummaryRecord getDefaultDtAwdPriSummary(DtManifestId dtManifestId) {
        DtSummaryRecord dt = getDtSummary(dtManifestId);
        String dtDataTypeTerm = dt.dataTypeTerm();

        List<DtAwdPriSummaryRecord> dtAwdPriList = getDtAwdPriSummaryList(dtManifestId);

        /*
         * Issue #808
         */
        Stream<DtAwdPriSummaryRecord> stream;
        if ("Date Time".equals(dtDataTypeTerm)) {
            stream = dtAwdPriList.stream().filter(e -> "date time".equals(e.xbtName()));
        } else if ("Date".equals(dtDataTypeTerm)) {
            stream = dtAwdPriList.stream().filter(e -> "date".equals(e.xbtName()));
        } else if ("Time".equals(dtDataTypeTerm)) {
            stream = dtAwdPriList.stream().filter(e -> "time".equals(e.xbtName()));
        } else {
            stream = dtAwdPriList.stream().filter(e -> e.isDefault());
        }

        return stream.findFirst().orElse(null);
    }

    private List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(ReleaseId releaseId, DtId dtId) {
        if (releaseId == null || dtId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtAwdPriSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        DT_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_AWD_PRI.DT_ID.eq(valueOf(dtId))
                ))
                .fetch(queryBuilder.mapper());
    }

    private class GetDtAwdPriSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(DT_AWD_PRI.DT_AWD_PRI_ID,
                            RELEASE.RELEASE_ID,
                            DT_AWD_PRI.DT_ID,
                            DT_AWD_PRI.XBT_MANIFEST_ID,
                            CDT_PRI.NAME.as("cdt_pri_name"),
                            XBT.NAME.as("xbt_name"),
                            DT_AWD_PRI.CODE_LIST_MANIFEST_ID,
                            CODE_LIST.NAME.as("code_list_name"),
                            DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.NAME.as("agency_id_list_name"),
                            DT_AWD_PRI.IS_DEFAULT)
                    .from(DT_AWD_PRI)
                    .join(RELEASE).on(DT_AWD_PRI.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .leftJoin(XBT_MANIFEST).on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                    .leftJoin(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                    .leftJoin(CDT_PRI).on(XBT_MANIFEST.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                    .leftJoin(CODE_LIST_MANIFEST).on(DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .leftJoin(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .leftJoin(AGENCY_ID_LIST_MANIFEST).on(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID));
        }

        private RecordMapper<Record, DtAwdPriSummaryRecord> mapper() {
            return record -> {
                DtAwdPriId dtAwdPriId = new DtAwdPriId(record.get(DT_AWD_PRI.DT_AWD_PRI_ID).toBigInteger());
                ReleaseId releaseId = new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger());
                DtId dtId = new DtId(record.get(DT_AWD_PRI.DT_ID).toBigInteger());
                XbtManifestId xbtManifestId = (record.get(DT_AWD_PRI.XBT_MANIFEST_ID) != null) ?
                        new XbtManifestId(record.get(DT_AWD_PRI.XBT_MANIFEST_ID).toBigInteger()) : null;
                CodeListManifestId codeListManifestId = (record.get(DT_AWD_PRI.CODE_LIST_MANIFEST_ID) != null) ?
                        new CodeListManifestId(record.get(DT_AWD_PRI.CODE_LIST_MANIFEST_ID).toBigInteger()) : null;
                AgencyIdListManifestId agencyIdListManifestId = (record.get(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                        new AgencyIdListManifestId(record.get(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null;

                return new DtAwdPriSummaryRecord(dtAwdPriId,
                        releaseId,
                        dtId,
                        xbtManifestId,
                        record.get(CDT_PRI.NAME.as("cdt_pri_name")),
                        record.get(XBT.NAME.as("xbt_name")),
                        codeListManifestId,
                        record.get(CODE_LIST.NAME.as("code_list_name")),
                        agencyIdListManifestId,
                        record.get(AGENCY_ID_LIST.NAME.as("agency_id_list_name")),
                        (byte) 1 == record.get(DT_AWD_PRI.IS_DEFAULT)
                );
            };
        }
    }

    @Override
    public List<DtAwdPriDetailsRecord> getDefaultPrimitiveValues(String representationTerm) {

        if (!hasLength(representationTerm)) {
            throw new IllegalArgumentException("`representationTerm` must not be empty.");
        }

        LibrarySummaryRecord cctsLibrary = repositoryFactory().libraryQueryRepository(requester())
                .getLibrarySummaryByName("CCTS Data Type Catalogue v3");
        if (cctsLibrary == null) {
            throw new IllegalStateException("Could not find CCTS Data Type Catalogue");
        }
        ReleaseSummaryRecord ccts31Release = repositoryFactory().releaseQueryRepository(requester())
                .getReleaseSummary(cctsLibrary.libraryId(), "3.1");
        if (ccts31Release == null) {
            throw new IllegalStateException("Could not find CCTS Data Type Catalogue v3.1");
        }

        var query = repositoryFactory().dtQueryRepository(requester());
        DtSummaryRecord matchedDt = query.getDtSummaryList(ccts31Release.releaseId()).stream()
                .filter(e -> representationTerm.equals(e.representationTerm()))
                .findFirst().orElse(null);
        if (matchedDt == null) {
            throw new IllegalArgumentException("Could not find a matched DT record for the '" + representationTerm + "' representation term.");
        }

        return getDtAwdPriDetailsList(matchedDt.release().releaseId(), matchedDt.dtId());
    }

    @Override
    public DtScDetailsRecord getDtScDetails(DtScManifestId dtScManifestId) {
        if (dtScManifestId == null) {
            return null;
        }

        var queryBuilder = new GetDtScDetailsQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.in(valueOf(dtScManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<DtScDetailsRecord> getDtScDetailsList(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtScDetailsQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetDtScDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            DT_SC_MANIFEST.DT_SC_MANIFEST_ID,
                            DT_SC.DT_SC_ID,
                            DT_SC.GUID,

                            DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                            DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID,
                            DT_SC_MANIFEST.REPLACEMENT_DT_SC_MANIFEST_ID,

                            DT_SC.OBJECT_CLASS_TERM,
                            DT_SC.PROPERTY_TERM,
                            DT_SC.REPRESENTATION_TERM,
                            DT_SC.DEFINITION,
                            DT_SC.DEFINITION_SOURCE,
                            DT_SC.CARDINALITY_MIN,
                            DT_SC.CARDINALITY_MAX,
                            DT_SC.as("prev").CARDINALITY_MIN.as("prev_cardinality_min"),
                            DT_SC.as("prev").CARDINALITY_MAX.as("prev_cardinality_max"),
                            DT_SC.IS_DEPRECATED,
                            DT.STATE,
                            DT_SC.DEFAULT_VALUE,
                            DT_SC.FIXED_VALUE,
                            DT_SC.CREATION_TIMESTAMP,
                            DT_SC.LAST_UPDATE_TIMESTAMP,

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

                            DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID,
                            DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(DT_SC_MANIFEST)
                    .join(RELEASE).on(DT_SC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                    .join(DT_MANIFEST).on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                    .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(DT_SC.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(DT_SC.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(DT_SC.LAST_UPDATED_BY))
                    .leftJoin(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(DT_SC.as("prev")).on(DT_SC.PREV_DT_SC_ID.eq(DT_SC.as("prev").DT_SC_ID));
        }

        private RecordMapper<Record, DtScDetailsRecord> mapper() {
            return record -> {
                DtScManifestId dtScManifestId = new DtScManifestId(record.get(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).toBigInteger());
                DtScId dtScId = new DtScId(record.get(DT_SC.DT_SC_ID).toBigInteger());
                DtManifestId ownerDtManifestId = new DtManifestId(record.get(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID).toBigInteger());
                DtScManifestId basedDtScManifestId =
                        (record.get(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID) != null) ?
                                new DtScManifestId(record.get(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID).toBigInteger()) : null;
                DtScManifestId replacementDtScManifestId =
                        (record.get(DT_SC_MANIFEST.REPLACEMENT_DT_SC_MANIFEST_ID) != null) ?
                                new DtScManifestId(record.get(DT_SC_MANIFEST.REPLACEMENT_DT_SC_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(DT.STATE));
                Cardinality prevCardinality = null;
                if (record.get(DT_SC.as("prev").CARDINALITY_MIN.as("prev_cardinality_min")) != null &&
                        record.get(DT_SC.as("prev").CARDINALITY_MAX.as("prev_cardinality_max")) != null) {
                    prevCardinality = new Cardinality(
                            record.get(DT_SC.as("prev").CARDINALITY_MIN.as("prev_cardinality_min")),
                            record.get(DT_SC.as("prev").CARDINALITY_MAX.as("prev_cardinality_max"))
                    );
                }
                return new DtScDetailsRecord(
                        library, release,
                        dtScManifestId,
                        new DtScId(record.get(DT_SC.DT_SC_ID).toBigInteger()),
                        new Guid(record.get(DT_SC.GUID)),

                        getDtSummary(ownerDtManifestId),
                        getDtScSummary(basedDtScManifestId),
                        getDtScSummary(replacementDtScManifestId),
                        getDtScSummary(since(dtScManifestId)),
                        getDtScSummary(lastChanged(dtScManifestId)),

                        record.get(DT_SC.PROPERTY_TERM) + ". " + record.get(DT_SC.REPRESENTATION_TERM),
                        record.get(DT_SC.OBJECT_CLASS_TERM),
                        record.get(DT_SC.PROPERTY_TERM),
                        record.get(DT_SC.REPRESENTATION_TERM),
                        new Cardinality(
                                record.get(DT_SC.CARDINALITY_MIN),
                                record.get(DT_SC.CARDINALITY_MAX)),
                        prevCardinality,
                        (byte) 1 == record.get(DT_SC.IS_DEPRECATED),
                        state,
                        new ValueConstraint(
                                record.get(DT_SC.DEFAULT_VALUE),
                                record.get(DT_SC.FIXED_VALUE)),
                        new Definition(
                                record.get(DT_SC.DEFINITION),
                                record.get(DT_SC.DEFINITION_SOURCE)),
                        getDtScAwdPriDetailsList(release.releaseId(), dtScId),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(DT_SC.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(DT_SC.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID) != null) ?
                                new DtScManifestId(record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID) != null) ?
                                new DtScManifestId(record.get(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    private List<DtScAwdPriDetailsRecord> getDtScAwdPriDetailsList(ReleaseId releaseId, DtScId dtScId) {
        var queryBuilder = new GetDtScAwdPriDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        DT_SC_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_SC_AWD_PRI.DT_SC_ID.eq(valueOf(dtScId))
                ))
                .fetch(queryBuilder.mapper());
    }

    private class GetDtScAwdPriDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            DT_SC_AWD_PRI.DT_SC_ID,
                            DT_SC_AWD_PRI.XBT_MANIFEST_ID,
                            DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID,
                            DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                            DT_SC_AWD_PRI.IS_DEFAULT)
                    .from(DT_SC_AWD_PRI)
                    .join(RELEASE).on(DT_SC_AWD_PRI.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID));
        }

        private RecordMapper<Record, DtScAwdPriDetailsRecord> mapper() {
            return record -> {
                DtScAwdPriId dtScAwdPriId = new DtScAwdPriId(record.get(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID).toBigInteger());
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                DtScId dtScId = new DtScId(record.get(DT_SC_AWD_PRI.DT_SC_ID).toBigInteger());
                XbtManifestId xbtManifestId = (record.get(DT_SC_AWD_PRI.XBT_MANIFEST_ID) != null) ?
                        new XbtManifestId(record.get(DT_SC_AWD_PRI.XBT_MANIFEST_ID).toBigInteger()) : null;
                CodeListManifestId codeListManifestId = (record.get(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID) != null) ?
                        new CodeListManifestId(record.get(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID).toBigInteger()) : null;
                AgencyIdListManifestId agencyIdListManifestId = (record.get(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                        new AgencyIdListManifestId(record.get(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null;

                return new DtScAwdPriDetailsRecord(dtScAwdPriId,
                        release,
                        dtScId,
                        (xbtManifestId != null) ?
                                repositoryFactory().xbtQueryRepository(requester()).getXbtSummary(xbtManifestId) : null,
                        (codeListManifestId != null) ?
                                repositoryFactory().codeListQueryRepository(requester()).getCodeListSummary(codeListManifestId) : null,
                        (agencyIdListManifestId != null) ?
                                repositoryFactory().agencyIdListQueryRepository(requester()).getAgencyIdListSummary(agencyIdListManifestId) : null,
                        (byte) 1 == record.get(DT_SC_AWD_PRI.IS_DEFAULT),
                        isInherited(dtScId, xbtManifestId, codeListManifestId, agencyIdListManifestId)
                );
            };
        }
    }

    private boolean isInherited(DtScId dtScId,
                                XbtManifestId xbtManifestId,
                                CodeListManifestId codeListManifestId,
                                AgencyIdListManifestId agencyIdListManifestId) {

        ULong basedDtScId = dslContext().select(DT_SC.BASED_DT_SC_ID)
                .from(DT_SC)
                .where(DT_SC.DT_SC_ID.eq(valueOf(dtScId)))
                .fetchOptionalInto(ULong.class).orElse(null);
        if (basedDtScId == null) {
            return false;
        }

        return dslContext().selectCount()
                .from(DT_SC_AWD_PRI)
                .where(and(
                                DT_SC_AWD_PRI.DT_SC_ID.eq(basedDtScId)),
                        or(
                                DT_SC_AWD_PRI.XBT_MANIFEST_ID.eq(valueOf(xbtManifestId)),
                                DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(valueOf(codeListManifestId)),
                                DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(valueOf(agencyIdListManifestId))
                        )
                )
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    private DtScManifestId since(DtScManifestId dtScManifestId) {
        if (dtScManifestId == null) {
            return null;
        }
        Record2<ULong, ULong> record = dslContext().select(
                        DT_SC_MANIFEST.DT_SC_MANIFEST_ID,
                        DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID)
                .from(DT_SC_MANIFEST)
                .join(RELEASE).on(DT_SC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtScManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ULong prevDtScManifestId = record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID);
        if (prevDtScManifestId != null) {
            return since(new DtScManifestId(prevDtScManifestId.toBigInteger()));
        } else {
            return new DtScManifestId(record.get(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).toBigInteger());
        }
    }

    private DtScManifestId lastChanged(DtScManifestId dtScManifestId) {
        if (dtScManifestId == null) {
            return null;
        }
        Record4<ULong, ULong, ULong, ULong> record = dslContext().select(
                        DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.DT_SC_ID,
                        DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID, DT_SC_MANIFEST.as("prev").DT_SC_ID)
                .from(DT_SC_MANIFEST)
                .join(DT_SC_MANIFEST.as("prev")).on(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID))
                .join(RELEASE).on(DT_SC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(valueOf(dtScManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        if (record.get(DT_SC_MANIFEST.DT_SC_ID).equals(record.get(DT_SC_MANIFEST.as("prev").DT_SC_ID))) {
            return lastChanged(new DtScManifestId(record.get(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID).toBigInteger()));
        } else {
            return new DtScManifestId(record.get(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID).toBigInteger());
        }
    }

    @Override
    public DtScSummaryRecord getDtScSummary(DtScManifestId dtScManifestId) {
        if (dtScManifestId == null) {
            return null;
        }

        var queryBuilder = new GetDtScSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.in(valueOf(dtScManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<DtScSummaryRecord> getInheritedDtScSummaryList(DtScManifestId basedDtScManifestId) {
        if (basedDtScManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtScSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.in(valueOf(basedDtScManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtScSummaryRecord> getDtScSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetDtScSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtScSummaryRecord> getDtScSummaryList(DtManifestId ownerDtManifestId) {
        if (ownerDtManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtScSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.in(valueOf(ownerDtManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetDtScSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            DT_SC_MANIFEST.DT_SC_MANIFEST_ID,
                            DT_SC.DT_SC_ID,
                            DT_SC.GUID,

                            DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                            DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID,

                            DT_SC.OBJECT_CLASS_TERM,
                            DT_SC.PROPERTY_TERM,
                            DT_SC.REPRESENTATION_TERM,
                            DT_SC.DEFINITION,
                            DT_SC.DEFINITION_SOURCE,
                            DT_SC.CARDINALITY_MIN,
                            DT_SC.CARDINALITY_MAX,
                            DT_SC.IS_DEPRECATED,
                            DT.STATE,
                            DT_SC.DEFAULT_VALUE,
                            DT_SC.FIXED_VALUE,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.REVISION_NUM,

                            DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID,
                            DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID
                    ), ownerFields()))
                    .from(DT_SC_MANIFEST)
                    .join(RELEASE).on(DT_SC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                    .join(DT_MANIFEST).on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                    .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(DT_SC.OWNER_USER_ID))
                    .leftJoin(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
        }

        private RecordMapper<Record, DtScSummaryRecord> mapper() {
            return record -> {
                DtScManifestId dtScManifestId = new DtScManifestId(record.get(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).toBigInteger());
                DtScId dtScId = new DtScId(record.get(DT_SC.DT_SC_ID).toBigInteger());
                DtManifestId ownerDtManifestId = new DtManifestId(record.get(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID).toBigInteger());
                DtScManifestId basedDtScManifestId =
                        (record.get(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID) != null) ?
                                new DtScManifestId(record.get(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(DT.STATE));
                return new DtScSummaryRecord(
                        library, release,
                        dtScManifestId,
                        dtScId,
                        new Guid(record.get(DT_SC.GUID)),

                        ownerDtManifestId,
                        basedDtScManifestId,

                        record.get(DT_SC.PROPERTY_TERM) + ". " + record.get(DT_SC.REPRESENTATION_TERM),
                        record.get(DT_SC.OBJECT_CLASS_TERM),
                        record.get(DT_SC.PROPERTY_TERM),
                        record.get(DT_SC.REPRESENTATION_TERM),
                        new Cardinality(
                                record.get(DT_SC.CARDINALITY_MIN),
                                record.get(DT_SC.CARDINALITY_MAX)),
                        (byte) 1 == record.get(DT_SC.IS_DEPRECATED),
                        state,
                        new ValueConstraint(
                                record.get(DT_SC.DEFAULT_VALUE),
                                record.get(DT_SC.FIXED_VALUE)),
                        new Definition(
                                record.get(DT_SC.DEFINITION),
                                record.get(DT_SC.DEFINITION_SOURCE)),

                        (record.get(LOG.REVISION_NUM) != null) ? record.get(LOG.REVISION_NUM).intValue() : 1,

                        fetchOwnerSummary(record),

                        (record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID) != null) ?
                                new DtScManifestId(record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID) != null) ?
                                new DtScManifestId(record.get(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetDtScAwdPriSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_AWD_PRI.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(ReleaseId releaseId) {

        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtScAwdPriSummaryQueryBuilder();
        return queryBuilder.select()
                .where(DT_SC_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(DtScManifestId dtScManifestId) {

        if (dtScManifestId == null) {
            return Collections.emptyList();
        }

        DtScSummaryRecord dtSc = getDtScSummary(dtScManifestId);
        if (dtSc == null) {
            return Collections.emptyList();
        }

        return getDtScAwdPriSummaryList(dtSc.release().releaseId(), dtSc.dtScId());
    }

    @Override
    public DtScAwdPriSummaryRecord getDefaultDtScAwdPriSummary(DtScManifestId dtScManifestId) {
        DtScSummaryRecord dtSc = getDtScSummary(dtScManifestId);
        String dtScRepresentationTerm = dtSc.representationTerm();

        List<DtScAwdPriSummaryRecord> dtScAwdPriList = getDtScAwdPriSummaryList(dtScManifestId);

        /*
         * Issue #808
         */
        Stream<DtScAwdPriSummaryRecord> stream;
        if ("Date Time".equals(dtScRepresentationTerm)) {
            stream = dtScAwdPriList.stream().filter(e -> "date time".equals(e.xbtName()));
        } else if ("Date".equals(dtScRepresentationTerm)) {
            stream = dtScAwdPriList.stream().filter(e -> "date".equals(e.xbtName()));
        } else if ("Time".equals(dtScRepresentationTerm)) {
            stream = dtScAwdPriList.stream().filter(e -> "time".equals(e.xbtName()));
        } else {
            stream = dtScAwdPriList.stream().filter(e -> e.isDefault());
        }

        return stream.findFirst().orElse(null);
    }

    private List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(ReleaseId releaseId, DtScId dtScId) {

        if (releaseId == null || dtScId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetDtScAwdPriSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        DT_SC_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_SC_AWD_PRI.DT_SC_ID.eq(valueOf(dtScId))
                ))
                .fetch(queryBuilder.mapper());
    }

    private class GetDtScAwdPriSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID,
                            RELEASE.RELEASE_ID,
                            DT_SC_AWD_PRI.DT_SC_ID,
                            DT_SC_AWD_PRI.XBT_MANIFEST_ID,
                            CDT_PRI.NAME.as("cdt_pri_name"),
                            XBT.NAME.as("xbt_name"),
                            DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID,
                            CODE_LIST.NAME.as("code_list_name"),
                            DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                            AGENCY_ID_LIST.NAME.as("agency_id_list_name"),
                            DT_SC_AWD_PRI.IS_DEFAULT)
                    .from(DT_SC_AWD_PRI)
                    .join(RELEASE).on(DT_SC_AWD_PRI.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .leftJoin(XBT_MANIFEST).on(DT_SC_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                    .leftJoin(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                    .leftJoin(CDT_PRI).on(XBT_MANIFEST.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                    .leftJoin(CODE_LIST_MANIFEST).on(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                    .leftJoin(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                    .leftJoin(AGENCY_ID_LIST_MANIFEST).on(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                    .leftJoin(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID));
        }

        private RecordMapper<Record, DtScAwdPriSummaryRecord> mapper() {
            return record -> {
                DtScAwdPriId dtScAwdPriId = new DtScAwdPriId(record.get(DT_SC_AWD_PRI.DT_SC_AWD_PRI_ID).toBigInteger());
                ReleaseId releaseId = new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger());
                DtScId dtScId = new DtScId(record.get(DT_SC_AWD_PRI.DT_SC_ID).toBigInteger());
                XbtManifestId xbtManifestId = (record.get(DT_SC_AWD_PRI.XBT_MANIFEST_ID) != null) ?
                        new XbtManifestId(record.get(DT_SC_AWD_PRI.XBT_MANIFEST_ID).toBigInteger()) : null;
                CodeListManifestId codeListManifestId = (record.get(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID) != null) ?
                        new CodeListManifestId(record.get(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID).toBigInteger()) : null;
                AgencyIdListManifestId agencyIdListManifestId = (record.get(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                        new AgencyIdListManifestId(record.get(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null;

                return new DtScAwdPriSummaryRecord(dtScAwdPriId,
                        releaseId,
                        dtScId,
                        xbtManifestId,
                        record.get(CDT_PRI.NAME.as("cdt_pri_name")),
                        record.get(XBT.NAME.as("xbt_name")),
                        codeListManifestId,
                        record.get(CODE_LIST.NAME.as("code_list_name")),
                        agencyIdListManifestId,
                        record.get(AGENCY_ID_LIST.NAME.as("agency_id_list_name")),
                        (byte) 1 == record.get(DT_SC_AWD_PRI.IS_DEFAULT)
                );
            };
        }
    }

    @Override
    public boolean hasRecordsByNamespaceId(NamespaceId namespaceId) {
        if (namespaceId == null) {
            return false;
        }

        return dslContext().selectCount()
                .from(DT)
                .where(DT.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    private boolean hasChild(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return false;
        }

        int dtScCount = dslContext().selectCount()
                .from(DT_MANIFEST)
                .join(DT_SC_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID))
                .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .where(and(
                        DT_MANIFEST.DT_MANIFEST_ID.eq(valueOf(dtManifestId)),
                        or(
                                DT_SC.CARDINALITY_MIN.ne(0),
                                DT_SC.CARDINALITY_MAX.ne(0)
                        ))).fetchOneInto(Integer.class);
        return (dtScCount > 0);
    }

    @Override
    public boolean hasDuplicateSixDigitId(DtManifestId dtManifestId) {
        DtSummaryRecord dt = getDtSummary(dtManifestId);
        return dslContext().selectCount()
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(valueOf(dt.release().releaseId())),
                        DT.GUID.notEqual(dt.guid().value()),
                        DT.SIX_DIGIT_ID.eq(dt.sixDigitId())
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasSamePropertyTerm(DtScManifestId dtScManifestId, String propertyTerm) {
        DtScSummaryRecord dtSc = getDtScSummary(dtScManifestId);
        return dslContext().selectCount()
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(and(
                        DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(valueOf(dtSc.ownerDtManifestId())),
                        DT_SC.PROPERTY_TERM.eq(propertyTerm),
                        DT_SC.DT_SC_ID.notEqual(valueOf(dtSc.dtScId()))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public boolean hasSameDen(DtScManifestId dtScManifestId, String objectClassTerm, String propertyTerm, String representationTerm) {
        DtScSummaryRecord dtSc = getDtScSummary(dtScManifestId);
        return dslContext().selectCount()
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(and(
                        DT_SC.OBJECT_CLASS_TERM.eq(objectClassTerm),
                        DT_SC.PROPERTY_TERM.eq(propertyTerm),
                        DT_SC.REPRESENTATION_TERM.eq(representationTerm),
                        DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(valueOf(dtSc.ownerDtManifestId())),
                        DT_SC.DT_SC_ID.notEqual(valueOf(dtSc.dtScId()))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

}
