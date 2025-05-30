package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.*;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
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
import org.oagi.score.gateway.http.common.model.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.Revised;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.model.ScoreRole.END_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqAsccpQueryRepository extends JooqBaseRepository implements AsccpQueryRepository {

    private final ReleaseQueryRepository releaseQueryRepository;

    public JooqAsccpQueryRepository(DSLContext dslContext,
                                    ScoreUser requester,
                                    RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.releaseQueryRepository = repositoryFactory.releaseQueryRepository(requester);
    }

    @Override
    public AsccpDetailsRecord getAsccpDetails(AsccpManifestId asccpManifestId) {
        var queryBuilder = new GetAsccpDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAsccpDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                            ASCCP.ASCCP_ID,
                            ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                            ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID,
                            ASCCP.GUID,
                            ASCCP.TYPE,

                            ASCCP_MANIFEST.DEN,
                            ASCCP.PROPERTY_TERM,
                            ASCCP.DEFINITION,
                            ASCCP.DEFINITION_SOURCE,
                            ASCCP.REUSABLE_INDICATOR,
                            ASCCP.IS_DEPRECATED,
                            ASCCP.IS_NILLABLE,
                            ASCCP.STATE,
                            ASCCP.CREATION_TIMESTAMP,
                            ASCCP.LAST_UPDATE_TIMESTAMP,

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

                            ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                            ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID,
                            ASCCP.PREV_ASCCP_ID,
                            ASCCP.NEXT_ASCCP_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASCCP_MANIFEST)
                    .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(ASCCP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASCCP.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ASCCP.LAST_UPDATED_BY))
                    .leftJoin(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(NAMESPACE).on(ASCCP.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID));
        }

        private RecordMapper<Record, AsccpDetailsRecord> mapper() {
            return record -> {
                AsccpManifestId asccpManifestId = new AsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
                AccManifestId roleOfAccManifestId = (record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID).toBigInteger()) : null;
                AsccpManifestId replacementAsccpManifestid = (record.get(ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID) != null) ?
                        new AsccpManifestId(record.get(ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(ASCCP.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new AsccpDetailsRecord(
                        library, release,
                        asccpManifestId,
                        new AsccpId(record.get(ASCCP.ASCCP_ID).toBigInteger()),
                        new Guid(record.get(ASCCP.GUID)),
                        AsccpType.valueOf(record.get(ASCCP.TYPE)),
                        (roleOfAccManifestId != null) ?
                                repositoryFactory().accQueryRepository(requester())
                                        .getAccSummary(roleOfAccManifestId) : null,
                        (replacementAsccpManifestid != null) ? getAsccpSummary(replacementAsccpManifestid) : null,
                        getAsccpSummary(since(asccpManifestId)),
                        getAsccpSummary(lastChanged(asccpManifestId)),

                        record.get(ASCCP_MANIFEST.DEN),
                        record.get(ASCCP.PROPERTY_TERM),
                        (byte) 1 == record.get(ASCCP.REUSABLE_INDICATOR),
                        (byte) 1 == record.get(ASCCP.IS_DEPRECATED),
                        (byte) 1 == record.get(ASCCP.IS_NILLABLE),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        new Definition(
                                record.get(ASCCP.DEFINITION),
                                record.get(ASCCP.DEFINITION_SOURCE)),
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
                                toDate(record.get(ASCCP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ASCCP.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID) != null) ?
                                new AsccpManifestId(record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID) != null) ?
                                new AsccpManifestId(record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ASCCP.PREV_ASCCP_ID) != null) ?
                                new AsccpId(record.get(ASCCP.PREV_ASCCP_ID).toBigInteger()) : null,
                        (record.get(ASCCP.NEXT_ASCCP_ID) != null) ?
                                new AsccpId(record.get(ASCCP.NEXT_ASCCP_ID).toBigInteger()) : null
                );
            };
        }
    }

    private AsccpManifestId since(AsccpManifestId asccpManifestId) {
        if (asccpManifestId == null) {
            return null;
        }
        Record2<ULong, ULong> record = dslContext().select(
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ULong prevAsccpManifestId = record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID);
        if (prevAsccpManifestId != null) {
            return since(new AsccpManifestId(prevAsccpManifestId.toBigInteger()));
        } else {
            return new AsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
        }
    }

    private AsccpManifestId lastChanged(AsccpManifestId asccpManifestId) {
        if (asccpManifestId == null) {
            return null;
        }
        Record4<ULong, ULong, ULong, ULong> record = dslContext().select(
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_ID,
                        ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID, ASCCP_MANIFEST.as("prev").ASCCP_ID)
                .from(ASCCP_MANIFEST)
                .join(ASCCP_MANIFEST.as("prev")).on(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID))
                .join(RELEASE).on(ASCCP_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        if (record.get(ASCCP_MANIFEST.ASCCP_ID).equals(record.get(ASCCP_MANIFEST.as("prev").ASCCP_ID))) {
            return lastChanged(new AsccpManifestId(record.get(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID).toBigInteger()));
        } else {
            return new AsccpManifestId(record.get(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID).toBigInteger());
        }
    }

    @Override
    public AsccpDetailsRecord getPrevAsccpDetails(AsccpManifestId asccpManifestId) {
        if (asccpManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAsccpDetailsQueryBuilder();
        // For the record in the 'Release Draft' state,
        // since there are records with duplicate next manifest IDs from the existing previous release,
        // retrieve the first record after sorting by ID in descending order.
        AsccpDetailsRecord prevAsccpDetails = queryBuilder.select()
                .where(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .orderBy(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.desc())
                .fetchAny(queryBuilder.mapper());
        if (prevAsccpDetails == null) {
            // In the case of an end-user, the new revision is created within the same Manifest and does not have a previous Manifest.
            // Therefore, the previous record must be retrieved based on the log.
            var prevQueryBuilder = new GetPrevAsccpDetailsQueryBuilder();
            prevAsccpDetails = prevQueryBuilder.select()
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                    .fetchOne(prevQueryBuilder.mapper());
        }

        return prevAsccpDetails;
    }

    private class GetPrevAsccpDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                            ASCCP.as("prev").ASCCP_ID,
                            ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                            ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID,
                            ASCCP.as("prev").GUID,
                            ASCCP.as("prev").TYPE,

                            ASCCP_MANIFEST.DEN,
                            ASCCP.as("prev").PROPERTY_TERM,
                            ASCCP.as("prev").DEFINITION,
                            ASCCP.as("prev").DEFINITION_SOURCE,
                            ASCCP.as("prev").REUSABLE_INDICATOR,
                            ASCCP.as("prev").IS_DEPRECATED,
                            ASCCP.as("prev").IS_NILLABLE,
                            ASCCP.as("prev").STATE,
                            ASCCP.as("prev").CREATION_TIMESTAMP,
                            ASCCP.as("prev").LAST_UPDATE_TIMESTAMP,

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

                            ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                            ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID,
                            ASCCP.as("prev").PREV_ASCCP_ID,
                            ASCCP.as("prev").NEXT_ASCCP_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASCCP_MANIFEST)
                    .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(ASCCP.as("prev")).on(and(
                            ASCCP.PREV_ASCCP_ID.eq(ASCCP.as("prev").ASCCP_ID),
                            ASCCP.ASCCP_ID.eq(ASCCP.as("prev").NEXT_ASCCP_ID)
                    ))
                    .join(ownerTable()).on(ownerTablePk().eq(ASCCP.as("prev").OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASCCP.as("prev").CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ASCCP.as("prev").LAST_UPDATED_BY))
                    .leftJoin(NAMESPACE).on(ASCCP.as("prev").NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
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

        private RecordMapper<Record, AsccpDetailsRecord> mapper() {
            return record -> {
                AsccpManifestId asccpManifestId = new AsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
                AccManifestId roleOfAccManifestId = (record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID).toBigInteger()) : null;
                AsccpManifestId replacementAsccpManifestid = (record.get(ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID) != null) ?
                        new AsccpManifestId(record.get(ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(ASCCP.as("prev").STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new AsccpDetailsRecord(
                        library, release,
                        asccpManifestId,
                        new AsccpId(record.get(ASCCP.as("prev").ASCCP_ID).toBigInteger()),
                        new Guid(record.get(ASCCP.as("prev").GUID)),
                        AsccpType.valueOf(record.get(ASCCP.as("prev").TYPE)),
                        (roleOfAccManifestId != null) ?
                                repositoryFactory().accQueryRepository(requester())
                                        .getAccSummary(roleOfAccManifestId) : null,
                        (replacementAsccpManifestid != null) ? getAsccpSummary(replacementAsccpManifestid) : null,
                        getAsccpSummary(since(asccpManifestId)),
                        getAsccpSummary(lastChanged(asccpManifestId)),

                        record.get(ASCCP_MANIFEST.DEN),
                        record.get(ASCCP.as("prev").PROPERTY_TERM),
                        (byte) 1 == record.get(ASCCP.as("prev").REUSABLE_INDICATOR),
                        (byte) 1 == record.get(ASCCP.as("prev").IS_DEPRECATED),
                        (byte) 1 == record.get(ASCCP.as("prev").IS_NILLABLE),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        new Definition(
                                record.get(ASCCP.as("prev").DEFINITION),
                                record.get(ASCCP.as("prev").DEFINITION_SOURCE)),
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
                                toDate(record.get(ASCCP.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ASCCP.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID) != null) ?
                                new AsccpManifestId(record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID) != null) ?
                                new AsccpManifestId(record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ASCCP.as("prev").PREV_ASCCP_ID) != null) ?
                                new AsccpId(record.get(ASCCP.as("prev").PREV_ASCCP_ID).toBigInteger()) : null,
                        (record.get(ASCCP.as("prev").NEXT_ASCCP_ID) != null) ?
                                new AsccpId(record.get(ASCCP.as("prev").NEXT_ASCCP_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public AsccpSummaryRecord getAsccpSummary(AsccpManifestId asccpManifestId) {
        if (asccpManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAsccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public AsccpSummaryRecord getAsccpSummary(AsccpId asccpId, ReleaseId releaseId) {
        if (asccpId == null || releaseId == null) {
            return null;
        }
        var queryBuilder = new GetAsccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ASCCP_MANIFEST.ASCCP_ID.eq(valueOf(asccpId)),
                        ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))
                ))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpSummaryList(Collection<ReleaseId> releaseIdList) {
        if (releaseIdList == null || releaseIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASCCP_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpSummaryList(AccManifestId roleOfAccManifestId) {
        if (roleOfAccManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(valueOf(roleOfAccManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(ASCCP.STATE.eq(state.name()));
        }
        var queryBuilder = new GetAsccpSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private class GetAsccpSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                            ASCCP.ASCCP_ID,
                            ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                            ASCCP.GUID,
                            ASCCP.TYPE,

                            ASCCP_MANIFEST.DEN,
                            ASCCP.PROPERTY_TERM,
                            ASCCP.DEFINITION,
                            ASCCP.DEFINITION_SOURCE,
                            ASCCP.REUSABLE_INDICATOR,
                            ASCCP.IS_DEPRECATED,
                            ASCCP.IS_NILLABLE,
                            ASCCP.STATE,

                            ASCCP.NAMESPACE_ID,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.REVISION_NUM,

                            ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                            ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID
                    ), ownerFields()))
                    .from(ASCCP_MANIFEST)
                    .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(ASCCP.OWNER_USER_ID))
                    .leftJoin(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
        }

        private RecordMapper<Record, AsccpSummaryRecord> mapper() {
            return record -> {
                AsccpManifestId asccpManifestId = new AsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
                AccManifestId roleOfAccManifestId = (record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID).toBigInteger()) : null;
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
                return new AsccpSummaryRecord(
                        library, release,
                        asccpManifestId,
                        new AsccpId(record.get(ASCCP.ASCCP_ID).toBigInteger()),
                        new Guid(record.get(ASCCP.GUID)),
                        AsccpType.valueOf(record.get(ASCCP.TYPE)),
                        roleOfAccManifestId,

                        record.get(ASCCP_MANIFEST.DEN),
                        record.get(ASCCP.PROPERTY_TERM),
                        (byte) 1 == record.get(ASCCP.REUSABLE_INDICATOR),
                        (byte) 1 == record.get(ASCCP.IS_DEPRECATED),
                        (byte) 1 == record.get(ASCCP.IS_NILLABLE),
                        CcState.valueOf(record.get(ASCCP.STATE)),
                        (record.get(ASCCP.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(ASCCP.NAMESPACE_ID).toBigInteger()) : null,
                        new Definition(
                                record.get(ASCCP.DEFINITION),
                                record.get(ASCCP.DEFINITION_SOURCE)),

                        (record.get(LOG.REVISION_NUM) != null) ? record.get(LOG.REVISION_NUM).intValue() : 1,

                        fetchOwnerSummary(record),

                        (record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID) != null) ?
                                new AsccpManifestId(record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID) != null) ?
                                new AsccpManifestId(record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public boolean hasRecordsByNamespaceId(NamespaceId namespaceId) {
        return dslContext().selectCount()
                .from(ASCCP)
                .where(ASCCP.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public AsccpSummaryRecord findNextAsccpManifest(
            TopLevelAsbiepId topLevelAsbiepId, ReleaseId nextReleaseId) {

        BigInteger asccpManifestId = dslContext().select(ASBIEP.BASED_ASCCP_MANIFEST_ID)
                .from(ASBIEP)
                .join(TOP_LEVEL_ASBIEP).on(ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                .fetchOptionalInto(BigInteger.class).orElse(null);
        if (asccpManifestId == null) {
            return null;
        }

        return findNextAsccpManifest(new AsccpManifestId(asccpManifestId), nextReleaseId);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public AsccpSummaryRecord findNextAsccpManifest(
            AsccpManifestId asccpManifestId, ReleaseId nextReleaseId) {
        if (asccpManifestId == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        if (nextReleaseId == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        AsccpSummaryRecord nextAsccp = null;
        while (nextAsccp == null) {
            AsccpSummaryRecord asccp = getAsccpSummary(asccpManifestId);
            if (asccp == null) {
                break;
            }

            if (asccp.release().releaseId().equals(nextReleaseId)) {
                nextAsccp = asccp;
                break;
            }

            asccpManifestId = asccp.nextAsccpManifestId();
        }

        return nextAsccp;
    }

}
