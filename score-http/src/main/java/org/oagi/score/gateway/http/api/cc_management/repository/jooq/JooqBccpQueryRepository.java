package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
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

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.Revised;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqBccpQueryRepository extends JooqBaseRepository implements BccpQueryRepository {

    private final DtQueryRepository dtQueryRepository;
    private final ReleaseQueryRepository releaseQueryRepository;

    public JooqBccpQueryRepository(DSLContext dslContext,
                                   ScoreUser requester,
                                   RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.dtQueryRepository = repositoryFactory.dtQueryRepository(requester);
        this.releaseQueryRepository = repositoryFactory.releaseQueryRepository(requester);
    }

    @Override
    public BccpDetailsRecord getBccpDetails(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }

        var queryBuilder = new GetBccpDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.in(valueOf(bccpManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetBccpDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            BCCP_MANIFEST.BCCP_MANIFEST_ID,
                            BCCP.BCCP_ID,
                            BCCP_MANIFEST.BDT_MANIFEST_ID,
                            BCCP.GUID,
                            BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID,

                            BCCP_MANIFEST.DEN,
                            BCCP.PROPERTY_TERM,
                            BCCP.REPRESENTATION_TERM,
                            BCCP.DEFINITION,
                            BCCP.DEFINITION_SOURCE,
                            BCCP.IS_DEPRECATED,
                            BCCP.IS_NILLABLE,
                            BCCP.STATE,
                            BCCP.DEFAULT_VALUE,
                            BCCP.FIXED_VALUE,
                            BCCP.CREATION_TIMESTAMP,
                            BCCP.LAST_UPDATE_TIMESTAMP,

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

                            BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID,
                            BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID,
                            BCCP.PREV_BCCP_ID,
                            BCCP.NEXT_BCCP_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BCCP_MANIFEST)
                    .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(BCCP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BCCP.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BCCP.LAST_UPDATED_BY))
                    .leftJoin(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(NAMESPACE).on(BCCP.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID));
        }

        private RecordMapper<Record, BccpDetailsRecord> mapper() {
            return record -> {
                BccpManifestId bccpManifestId = new BccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
                DtManifestId dtManifestId = (record.get(BCCP_MANIFEST.BDT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(BCCP_MANIFEST.BDT_MANIFEST_ID).toBigInteger()) : null;
                BccpManifestId replacementBccpManifestId = (record.get(BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID) != null) ?
                        new BccpManifestId(record.get(BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(BCCP.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new BccpDetailsRecord(
                        library, release,
                        bccpManifestId,
                        new BccpId(record.get(BCCP.BCCP_ID).toBigInteger()),
                        new Guid(record.get(BCCP.GUID)),
                        (dtManifestId != null) ? dtQueryRepository.getDtSummary(dtManifestId) : null,
                        getBccpSummary(replacementBccpManifestId),
                        getBccpSummary(since(bccpManifestId)),
                        getBccpSummary(lastChanged(bccpManifestId)),

                        record.get(BCCP_MANIFEST.DEN),
                        record.get(BCCP.PROPERTY_TERM),
                        record.get(BCCP.REPRESENTATION_TERM),
                        hasChild(bccpManifestId),
                        (byte) 1 == record.get(BCCP.IS_DEPRECATED),
                        (byte) 1 == record.get(BCCP.IS_NILLABLE),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        new ValueConstraint(
                                record.get(BCCP.DEFAULT_VALUE),
                                record.get(BCCP.FIXED_VALUE)),
                        new Definition(
                                record.get(BCCP.DEFINITION),
                                record.get(BCCP.DEFINITION_SOURCE)),
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
                                toDate(record.get(BCCP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BCCP.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID) != null) ?
                                new BccpManifestId(record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID) != null) ?
                                new BccpManifestId(record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(BCCP.PREV_BCCP_ID) != null) ?
                                new BccpId(record.get(BCCP.PREV_BCCP_ID).toBigInteger()) : null,
                        (record.get(BCCP.NEXT_BCCP_ID) != null) ?
                                new BccpId(record.get(BCCP.NEXT_BCCP_ID).toBigInteger()) : null
                );
            };
        }
    }

    private BccpManifestId since(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }
        Record2<ULong, ULong> record = dslContext().select(
                        BCCP_MANIFEST.BCCP_MANIFEST_ID,
                        BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ULong prevBccpManifestId = record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID);
        if (prevBccpManifestId != null) {
            return since(new BccpManifestId(prevBccpManifestId.toBigInteger()));
        } else {
            return new BccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
        }
    }

    private BccpManifestId lastChanged(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }
        Record4<ULong, ULong, ULong, ULong> record = dslContext().select(
                        BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_ID,
                        BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID, BCCP_MANIFEST.as("prev").BCCP_ID)
                .from(BCCP_MANIFEST)
                .join(BCCP_MANIFEST.as("prev")).on(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID))
                .join(RELEASE).on(BCCP_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        if (record.get(BCCP_MANIFEST.BCCP_ID).equals(record.get(BCCP_MANIFEST.as("prev").BCCP_ID))) {
            return lastChanged(new BccpManifestId(record.get(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID).toBigInteger()));
        } else {
            return new BccpManifestId(record.get(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID).toBigInteger());
        }
    }

    @Override
    public BccpDetailsRecord getPrevBccpDetails(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }
        var queryBuilder = new GetBccpDetailsQueryBuilder();
        BccpDetailsRecord prevBccpDetails = queryBuilder.select()
                .where(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .fetchOne(queryBuilder.mapper());
        if (prevBccpDetails == null) {
            // In the case of an end-user, the new revision is created within the same Manifest and does not have a previous Manifest.
            // Therefore, the previous record must be retrieved based on the log.
            var prevQueryBuilder = new GetPrevBccpDetailsQueryBuilder();
            prevBccpDetails = prevQueryBuilder.select()
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                    .fetchOne(prevQueryBuilder.mapper());
        }

        return prevBccpDetails;
    }

    private class GetPrevBccpDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            BCCP_MANIFEST.BCCP_MANIFEST_ID,
                            BCCP.as("prev").BCCP_ID,
                            BCCP_MANIFEST.BDT_MANIFEST_ID,
                            BCCP.as("prev").GUID,
                            BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID,

                            BCCP_MANIFEST.DEN,
                            BCCP.as("prev").PROPERTY_TERM,
                            BCCP.as("prev").REPRESENTATION_TERM,
                            BCCP.as("prev").DEFINITION,
                            BCCP.as("prev").DEFINITION_SOURCE,
                            BCCP.as("prev").IS_DEPRECATED,
                            BCCP.as("prev").IS_NILLABLE,
                            BCCP.as("prev").STATE,
                            BCCP.as("prev").DEFAULT_VALUE,
                            BCCP.as("prev").FIXED_VALUE,
                            BCCP.as("prev").CREATION_TIMESTAMP,
                            BCCP.as("prev").LAST_UPDATE_TIMESTAMP,

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

                            BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID,
                            BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID,
                            BCCP.as("prev").PREV_BCCP_ID,
                            BCCP.as("prev").NEXT_BCCP_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BCCP_MANIFEST)
                    .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                    .join(BCCP.as("prev")).on(and(
                            BCCP.PREV_BCCP_ID.eq(BCCP.as("prev").BCCP_ID),
                            BCCP.BCCP_ID.eq(BCCP.as("prev").NEXT_BCCP_ID)
                    ))
                    .join(ownerTable()).on(ownerTablePk().eq(BCCP.as("prev").OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BCCP.as("prev").CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BCCP.as("prev").LAST_UPDATED_BY))
                    .leftJoin(NAMESPACE).on(BCCP.as("prev").NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
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

        private RecordMapper<Record, BccpDetailsRecord> mapper() {
            return record -> {
                BccpManifestId bccpManifestId = new BccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
                DtManifestId dtManifestId = (record.get(BCCP_MANIFEST.BDT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(BCCP_MANIFEST.BDT_MANIFEST_ID).toBigInteger()) : null;
                BccpManifestId replacementBccpManifestId = (record.get(BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID) != null) ?
                        new BccpManifestId(record.get(BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(BCCP.as("prev").STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new BccpDetailsRecord(
                        library, release,
                        bccpManifestId,
                        new BccpId(record.get(BCCP.as("prev").BCCP_ID).toBigInteger()),
                        new Guid(record.get(BCCP.as("prev").GUID)),
                        (dtManifestId != null) ? dtQueryRepository.getDtSummary(dtManifestId) : null,
                        getBccpSummary(replacementBccpManifestId),
                        getBccpSummary(since(bccpManifestId)),
                        getBccpSummary(lastChanged(bccpManifestId)),

                        record.get(BCCP_MANIFEST.DEN),
                        record.get(BCCP.as("prev").PROPERTY_TERM),
                        record.get(BCCP.as("prev").REPRESENTATION_TERM),
                        hasChild(bccpManifestId),
                        (byte) 1 == record.get(BCCP.as("prev").IS_DEPRECATED),
                        (byte) 1 == record.get(BCCP.as("prev").IS_NILLABLE),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        new ValueConstraint(
                                record.get(BCCP.as("prev").DEFAULT_VALUE),
                                record.get(BCCP.as("prev").FIXED_VALUE)),
                        new Definition(
                                record.get(BCCP.as("prev").DEFINITION),
                                record.get(BCCP.as("prev").DEFINITION_SOURCE)),
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
                                toDate(record.get(BCCP.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BCCP.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID) != null) ?
                                new BccpManifestId(record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID) != null) ?
                                new BccpManifestId(record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(BCCP.as("prev").PREV_BCCP_ID) != null) ?
                                new BccpId(record.get(BCCP.as("prev").PREV_BCCP_ID).toBigInteger()) : null,
                        (record.get(BCCP.as("prev").NEXT_BCCP_ID) != null) ?
                                new BccpId(record.get(BCCP.as("prev").NEXT_BCCP_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public BccpSummaryRecord getBccpSummary(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }

        var queryBuilder = new GetBccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.in(valueOf(bccpManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<BccpSummaryRecord> getBccpSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetBccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BCCP_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BccpSummaryRecord> getBccpSummaryList(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetBccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BCCP_MANIFEST.BDT_MANIFEST_ID.in(valueOf(dtManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BccpSummaryRecord> getBccpSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(BCCP.STATE.eq(state.name()));
        }
        var queryBuilder = new GetBccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private class GetBccpSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            BCCP_MANIFEST.BCCP_MANIFEST_ID,
                            BCCP.BCCP_ID,
                            BCCP_MANIFEST.BDT_MANIFEST_ID,
                            BCCP.GUID,

                            BCCP_MANIFEST.DEN,
                            BCCP.PROPERTY_TERM,
                            BCCP.REPRESENTATION_TERM,
                            BCCP.DEFINITION,
                            BCCP.DEFINITION_SOURCE,
                            BCCP.IS_DEPRECATED,
                            BCCP.IS_NILLABLE,
                            BCCP.STATE,
                            BCCP.DEFAULT_VALUE,
                            BCCP.FIXED_VALUE,
                            BCCP.CREATION_TIMESTAMP,
                            BCCP.LAST_UPDATE_TIMESTAMP,

                            BCCP.NAMESPACE_ID,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.REVISION_NUM,

                            BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID,
                            BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID
                    ), ownerFields()))
                    .from(BCCP_MANIFEST)
                    .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(BCCP.OWNER_USER_ID))
                    .leftJoin(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
        }

        private RecordMapper<Record, BccpSummaryRecord> mapper() {
            return record -> {
                BccpManifestId bccpManifestId = new BccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
                DtManifestId dtManifestId = (record.get(BCCP_MANIFEST.BDT_MANIFEST_ID) != null) ?
                        new DtManifestId(record.get(BCCP_MANIFEST.BDT_MANIFEST_ID).toBigInteger()) : null;
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
                return new BccpSummaryRecord(
                        library, release,
                        bccpManifestId,
                        new BccpId(record.get(BCCP.BCCP_ID).toBigInteger()),
                        new Guid(record.get(BCCP.GUID)),
                        dtManifestId,

                        record.get(BCCP_MANIFEST.DEN),
                        record.get(BCCP.PROPERTY_TERM),
                        record.get(BCCP.REPRESENTATION_TERM),
                        (byte) 1 == record.get(BCCP.IS_DEPRECATED),
                        (byte) 1 == record.get(BCCP.IS_NILLABLE),
                        CcState.valueOf(record.get(BCCP.STATE)),
                        (record.get(BCCP.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(BCCP.NAMESPACE_ID).toBigInteger()) : null,
                        new ValueConstraint(
                                record.get(BCCP.DEFAULT_VALUE),
                                record.get(BCCP.FIXED_VALUE)),
                        new Definition(
                                record.get(BCCP.DEFINITION),
                                record.get(BCCP.DEFINITION_SOURCE)),

                        (record.get(LOG.REVISION_NUM) != null) ? record.get(LOG.REVISION_NUM).intValue() : 1,

                        fetchOwnerSummary(record),

                        (record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID) != null) ?
                                new BccpManifestId(record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID) != null) ?
                                new BccpManifestId(record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID).toBigInteger()) : null
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
                .from(BCCP)
                .where(BCCP.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    private boolean hasChild(BccpManifestId bccpManifestId) {
        int dtScCount = dslContext().selectCount()
                .from(BCCP_MANIFEST)
                .join(DT_MANIFEST).on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(DT_SC_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID))
                .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .where(and(
                        BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)),
                        or(
                                DT_SC.CARDINALITY_MIN.ne(0),
                                DT_SC.CARDINALITY_MAX.ne(0)
                        ))).fetchOneInto(Integer.class);
        return (dtScCount > 0);
    }

}
