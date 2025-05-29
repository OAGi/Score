package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcRefactorValidationResponse;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.acc.*;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.*;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
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
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AsccManifestRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BccManifestRecord;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.*;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.Revised;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAccQueryRepository extends JooqBaseRepository implements AccQueryRepository {

    private final ReleaseQueryRepository releaseQueryRepository;

    public JooqAccQueryRepository(DSLContext dslContext,
                                  ScoreUser requester,
                                  RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.releaseQueryRepository = repositoryFactory.releaseQueryRepository(requester);
    }

    @Override
    public AccDetailsRecord getAccDetails(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAccDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAccDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ACC_MANIFEST.ACC_MANIFEST_ID,
                            ACC.ACC_ID,
                            ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                            ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID,
                            ACC_MANIFEST.DEN,

                            ACC.GUID,
                            ACC.TYPE,
                            ACC.OBJECT_CLASS_TERM,
                            ACC.OBJECT_CLASS_QUALIFIER,
                            ACC.OAGIS_COMPONENT_TYPE,
                            ACC.DEFINITION,
                            ACC.DEFINITION_SOURCE,
                            ACC.IS_ABSTRACT,
                            ACC.IS_DEPRECATED,
                            ACC.STATE,
                            ACC.CREATION_TIMESTAMP,
                            ACC.LAST_UPDATE_TIMESTAMP,

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

                            ACC_MANIFEST.PREV_ACC_MANIFEST_ID,
                            ACC_MANIFEST.NEXT_ACC_MANIFEST_ID,
                            ACC.PREV_ACC_ID,
                            ACC.NEXT_ACC_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ACC_MANIFEST)
                    .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(ACC.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ACC.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ACC.LAST_UPDATED_BY))
                    .leftJoin(NAMESPACE).on(ACC.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
        }

        private RecordMapper<Record, AccDetailsRecord> mapper() {
            return record -> {
                AccManifestId accManifestId = new AccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                AccManifestId basedAccManifestId = (record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID).toBigInteger()) : null;
                AccManifestId replacementAccManifestId = (record.get(ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID).toBigInteger()) : null;
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
                OagisComponentType componentType = OagisComponentType.valueOf(record.get(ACC.OAGIS_COMPONENT_TYPE));
                CcState state = CcState.valueOf(record.get(ACC.STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new AccDetailsRecord(
                        library, release,
                        accManifestId,
                        new AccId(record.get(ACC.ACC_ID).toBigInteger()),
                        new Guid(record.get(ACC.GUID)),
                        record.get(ACC.TYPE),

                        (basedAccManifestId != null) ? getAccSummary(basedAccManifestId) : null,
                        (replacementAccManifestId != null) ? getAccSummary(replacementAccManifestId) : null,
                        getAccSummary(since(accManifestId)),
                        getAccSummary(lastChanged(accManifestId)),

                        record.get(ACC_MANIFEST.DEN),
                        record.get(ACC.OBJECT_CLASS_TERM),
                        record.get(ACC.OBJECT_CLASS_QUALIFIER),
                        componentType,
                        (byte) 1 == record.get(ACC.IS_ABSTRACT),
                        componentType.isGroup(),
                        hasExtension(accManifestId),
                        hasChild(accManifestId),
                        (byte) 1 == record.get(ACC.IS_DEPRECATED),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        new Definition(
                                record.get(ACC.DEFINITION),
                                record.get(ACC.DEFINITION_SOURCE)),
                        AccessPrivilege.toAccessPrivilege(
                                requester(), owner.userId(),
                                state, release.isWorkingRelease()),
                        getAssociationSummaryList(accManifestId),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ACC.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ACC.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID) != null) ?
                                new AccManifestId(record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID) != null) ?
                                new AccManifestId(record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ACC.PREV_ACC_ID) != null) ?
                                new AccId(record.get(ACC.PREV_ACC_ID).toBigInteger()) : null,
                        (record.get(ACC.NEXT_ACC_ID) != null) ?
                                new AccId(record.get(ACC.NEXT_ACC_ID).toBigInteger()) : null
                );
            };
        }
    }

    private AccManifestId since(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        Record2<ULong, ULong> record = dslContext().select(
                        ACC_MANIFEST.ACC_MANIFEST_ID,
                        ACC_MANIFEST.PREV_ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ULong prevAccManifestId = record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID);
        if (prevAccManifestId != null) {
            return since(new AccManifestId(prevAccManifestId.toBigInteger()));
        } else {
            return new AccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
        }
    }

    private AccManifestId lastChanged(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        Record4<ULong, ULong, ULong, ULong> record = dslContext().select(
                        ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.ACC_ID,
                        ACC_MANIFEST.as("prev").ACC_MANIFEST_ID, ACC_MANIFEST.as("prev").ACC_ID)
                .from(ACC_MANIFEST)
                .join(ACC_MANIFEST.as("prev")).on(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID))
                .join(RELEASE).on(ACC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        if (record.get(ACC_MANIFEST.ACC_ID).equals(record.get(ACC_MANIFEST.as("prev").ACC_ID))) {
            return lastChanged(new AccManifestId(record.get(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID).toBigInteger()));
        } else {
            return new AccManifestId(record.get(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID).toBigInteger());
        }
    }

    @Override
    public AccSummaryRecord getAccSummary(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AccSummaryRecord> getAccSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetAccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ACC_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AccSummaryRecord> getAccSummaryList(Collection<ReleaseId> releaseIdList, String objectClassTerm) {
        if (releaseIdList == null || releaseIdList.isEmpty() || objectClassTerm == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ((releaseIdList.size() == 1) ?
                                ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseIdList.iterator().next())) :
                                ACC_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList))),
                        ACC.OBJECT_CLASS_TERM.eq(objectClassTerm)
                ))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AccSummaryRecord> getAccSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }
        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(ACC.STATE.eq(state.name()));
        }
        var queryBuilder = new GetAccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AccSummaryRecord> getInheritedAccSummaryList(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetAccSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ACC_MANIFEST.ACC_MANIFEST_ID,
                            ACC.ACC_ID,
                            ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                            ACC_MANIFEST.DEN,

                            ACC.GUID,
                            ACC.TYPE,
                            ACC.OBJECT_CLASS_TERM,
                            ACC.OBJECT_CLASS_QUALIFIER,
                            ACC.OAGIS_COMPONENT_TYPE,
                            ACC.DEFINITION,
                            ACC.DEFINITION_SOURCE,
                            ACC.IS_ABSTRACT,
                            ACC.IS_DEPRECATED,
                            ACC.STATE,

                            ACC.NAMESPACE_ID,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.REVISION_NUM,

                            ACC_MANIFEST.PREV_ACC_MANIFEST_ID,
                            ACC_MANIFEST.NEXT_ACC_MANIFEST_ID
                    ), ownerFields()))
                    .from(ACC_MANIFEST)
                    .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(ACC.OWNER_USER_ID))
                    .leftJoin(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
        }

        private RecordMapper<Record, AccSummaryRecord> mapper() {
            return record -> {
                AccManifestId accManifestId = new AccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                AccManifestId basedAccManifestId = (record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID).toBigInteger()) : null;
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
                return new AccSummaryRecord(
                        library, release,
                        accManifestId,
                        new AccId(record.get(ACC.ACC_ID).toBigInteger()),
                        new Guid(record.get(ACC.GUID)),
                        record.get(ACC.TYPE),

                        basedAccManifestId,

                        record.get(ACC_MANIFEST.DEN),
                        record.get(ACC.OBJECT_CLASS_TERM),
                        record.get(ACC.OBJECT_CLASS_QUALIFIER),
                        OagisComponentType.valueOf(record.get(ACC.OAGIS_COMPONENT_TYPE)),
                        (byte) 1 == record.get(ACC.IS_ABSTRACT),
                        (byte) 1 == record.get(ACC.IS_DEPRECATED),
                        CcState.valueOf(record.get(ACC.STATE)),
                        (record.get(ACC.NAMESPACE_ID) != null) ?
                                new NamespaceId(record.get(ACC.NAMESPACE_ID).toBigInteger()) : null,
                        new Definition(
                                record.get(ACC.DEFINITION),
                                record.get(ACC.DEFINITION_SOURCE)),

                        (record.get(LOG.REVISION_NUM) != null) ? record.get(LOG.REVISION_NUM).intValue() : 1,

                        fetchOwnerSummary(record),

                        (record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID) != null) ?
                                new AccManifestId(record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID) != null) ?
                                new AccManifestId(record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public List<CcAssociation> getAssociationSummaryList(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return Collections.emptyList();
        }

        List<SeqKeySummaryRecord> seqKeyList = repositoryFactory()
                .seqKeyQueryRepository(requester())
                .getSeqKeySummaryList(accManifestId);
        if (seqKeyList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<SeqKeyId, SeqKeySummaryRecord> seqKeyMap = seqKeyList.stream().collect(
                Collectors.toMap(SeqKeySummaryRecord::seqKeyId, Function.identity()));

        List<AsccSummaryRecord> asccSummaryList = getAsccSummaryList(accManifestId);
        Map<AsccManifestId, AsccSummaryRecord> asccMap = asccSummaryList.stream().collect(
                Collectors.toMap(AsccSummaryRecord::asccManifestId, Function.identity()));

        List<BccSummaryRecord> bccSummaryList = getBccSummaryList(accManifestId);
        Map<BccManifestId, BccSummaryRecord> bccMap = bccSummaryList.stream().collect(
                Collectors.toMap(BccSummaryRecord::bccManifestId, Function.identity()));

        List<CcAssociation> results = new ArrayList<>();
        SeqKeySummaryRecord seqKey = seqKeyList.stream().filter(e -> e.prevSeqKeyId() == null).findFirst().get();
        while (seqKey != null) {
            if (seqKey.asccManifestId() != null) {
                AsccSummaryRecord ascc = asccMap.get(seqKey.asccManifestId());
                results.add(ascc);
            } else {
                BccSummaryRecord bcc = bccMap.get(seqKey.bccManifestId());
                results.add(bcc);
            }

            seqKey = seqKeyMap.get(seqKey.nextSeqKeyId());
        }

        return results;
    }

    @Override
    public AccDetailsRecord getPrevAccDetails(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAccDetailsQueryBuilder();
        // For the record in the 'Release Draft' state,
        // since there are records with duplicate next manifest IDs from the existing previous release,
        // retrieve the first record after sorting by ID in descending order.
        AccDetailsRecord prevAccDetails = queryBuilder.select()
                .where(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .orderBy(ACC_MANIFEST.ACC_MANIFEST_ID.desc())
                .fetchAny(queryBuilder.mapper());
        if (prevAccDetails == null) {
            // In the case of an end-user, the new revision is created within the same Manifest and does not have a previous Manifest.
            // Therefore, the previous record must be retrieved based on the log.
            var prevQueryBuilder = new GetPrevAccDetailsQueryBuilder();
            prevAccDetails = prevQueryBuilder.select()
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                    .fetchOne(prevQueryBuilder.mapper());
        }

        return prevAccDetails;
    }

    private class GetPrevAccDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ACC_MANIFEST.ACC_MANIFEST_ID,
                            ACC.as("prev").ACC_ID,
                            ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                            ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID,
                            ACC_MANIFEST.DEN,

                            ACC.as("prev").GUID,
                            ACC.as("prev").TYPE,
                            ACC.as("prev").OBJECT_CLASS_TERM,
                            ACC.as("prev").OBJECT_CLASS_QUALIFIER,
                            ACC.as("prev").OAGIS_COMPONENT_TYPE,
                            ACC.as("prev").DEFINITION,
                            ACC.as("prev").DEFINITION_SOURCE,
                            ACC.as("prev").IS_ABSTRACT,
                            ACC.as("prev").IS_DEPRECATED,
                            ACC.as("prev").STATE,
                            ACC.as("prev").CREATION_TIMESTAMP,
                            ACC.as("prev").LAST_UPDATE_TIMESTAMP,

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

                            ACC_MANIFEST.PREV_ACC_MANIFEST_ID,
                            ACC_MANIFEST.NEXT_ACC_MANIFEST_ID,
                            ACC.as("prev").PREV_ACC_ID,
                            ACC.as("prev").NEXT_ACC_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ACC_MANIFEST)
                    .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(ACC.as("prev")).on(and(
                            ACC.PREV_ACC_ID.eq(ACC.as("prev").ACC_ID),
                            ACC.ACC_ID.eq(ACC.as("prev").NEXT_ACC_ID)
                    ))
                    .join(ownerTable()).on(ownerTablePk().eq(ACC.as("prev").OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ACC.as("prev").CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ACC.as("prev").LAST_UPDATED_BY))
                    .leftJoin(NAMESPACE).on(ACC.as("prev").NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                    .leftJoin(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
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

        private RecordMapper<Record, AccDetailsRecord> mapper() {
            return record -> {
                AccManifestId accManifestId = new AccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                AccManifestId basedAccManifestId = (record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID).toBigInteger()) : null;
                AccManifestId replacementAccManifestId = (record.get(ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID) != null) ?
                        new AccManifestId(record.get(ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID).toBigInteger()) : null;
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
                OagisComponentType componentType = OagisComponentType.valueOf(record.get(ACC.as("prev").OAGIS_COMPONENT_TYPE));
                CcState state = CcState.valueOf(record.get(ACC.as("prev").STATE));
                UserSummaryRecord owner = fetchOwnerSummary(record);
                return new AccDetailsRecord(
                        library, release,
                        accManifestId,
                        new AccId(record.get(ACC.as("prev").ACC_ID).toBigInteger()),
                        new Guid(record.get(ACC.as("prev").GUID)),
                        record.get(ACC.as("prev").TYPE),

                        (basedAccManifestId != null) ? getAccSummary(basedAccManifestId) : null,
                        (replacementAccManifestId != null) ? getAccSummary(replacementAccManifestId) : null,
                        getAccSummary(since(accManifestId)),
                        getAccSummary(lastChanged(accManifestId)),

                        record.get(ACC_MANIFEST.DEN),
                        record.get(ACC.as("prev").OBJECT_CLASS_TERM),
                        record.get(ACC.as("prev").OBJECT_CLASS_QUALIFIER),
                        componentType,
                        (byte) 1 == record.get(ACC.as("prev").IS_ABSTRACT),
                        componentType.isGroup(),
                        (byte) 1 == record.get(ACC.as("prev").IS_DEPRECATED),
                        hasExtension(accManifestId),
                        hasChild(accManifestId),
                        state,
                        (record.get(NAMESPACE.NAMESPACE_ID) != null) ?
                                new NamespaceSummaryRecord(
                                        new NamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger()),
                                        record.getValue(NAMESPACE.URI),
                                        record.getValue(NAMESPACE.PREFIX),
                                        record.getValue(NAMESPACE.IS_STD_NMSP) == (byte) 1
                                ) : null,
                        new Definition(
                                record.get(ACC.as("prev").DEFINITION),
                                record.get(ACC.as("prev").DEFINITION_SOURCE)),
                        AccessPrivilege.toAccessPrivilege(
                                requester(), owner.userId(),
                                state, release.isWorkingRelease()),
                        getAssociationSummaryList(accManifestId),

                        (record.get(LOG.as("prev_log").LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.as("prev_log").LOG_ID).toBigInteger()),
                                record.get(LOG.as("prev_log").REVISION_NUM).intValue(),
                                record.get(LOG.as("prev_log").REVISION_TRACKING_NUM).intValue()) : null,

                        owner,
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ACC.as("prev").CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ACC.as("prev").LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID) != null) ?
                                new AccManifestId(record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID) != null) ?
                                new AccManifestId(record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ACC.as("prev").PREV_ACC_ID) != null) ?
                                new AccId(record.get(ACC.as("prev").PREV_ACC_ID).toBigInteger()) : null,
                        (record.get(ACC.as("prev").NEXT_ACC_ID) != null) ?
                                new AccId(record.get(ACC.as("prev").NEXT_ACC_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public AsccDetailsRecord getAsccDetails(AsccManifestId asccManifestId) {
        if (asccManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAsccDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AsccDetailsRecord> getAsccDetailsList() {
        var queryBuilder = new GetAsccDetailsQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsccDetailsRecord> getAsccDetailsList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }
        Set<ReleaseId> libraryIdSet = releaseQueryRepository.getIncludedReleaseSummaryList(releaseId)
                .stream().map(e -> e.releaseId()).collect(Collectors.toSet());

        var queryBuilder = new GetAsccDetailsQueryBuilder();
        return queryBuilder.select()
                .where(RELEASE.RELEASE_ID.in(valueOf(libraryIdSet)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsccDetailsRecord> getAsccDetailsList(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsccDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetAsccDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ASCC_MANIFEST.ASCC_MANIFEST_ID,
                            ASCC.ASCC_ID,
                            ASCC.GUID,

                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                            ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                            SEQ_KEY.SEQ_KEY_ID,
                            SEQ_KEY.PREV_SEQ_KEY_ID,
                            SEQ_KEY.NEXT_SEQ_KEY_ID,
                            ASCC_MANIFEST.REPLACEMENT_ASCC_MANIFEST_ID,

                            ASCC_MANIFEST.DEN,
                            ASCC.DEFINITION,
                            ASCC.DEFINITION_SOURCE,
                            ASCC.CARDINALITY_MIN,
                            ASCC.CARDINALITY_MAX,
                            ASCC.IS_DEPRECATED,
                            ACC.STATE,
                            ASCC.CREATION_TIMESTAMP,
                            ASCC.LAST_UPDATE_TIMESTAMP,

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

                            ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID,
                            ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASCC_MANIFEST)
                    .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                    .join(ACC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(ASCC.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASCC.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ASCC.LAST_UPDATED_BY))
                    .leftJoin(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(SEQ_KEY).on(ASCC_MANIFEST.SEQ_KEY_ID.eq(SEQ_KEY.SEQ_KEY_ID));
        }

        private RecordMapper<Record, AsccDetailsRecord> mapper() {
            return record -> {
                AsccManifestId asccManifestId = new AsccManifestId(record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID).toBigInteger());
                AccManifestId fromAccManifestId = new AccManifestId(record.get(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID).toBigInteger());
                AsccpManifestId toAsccpManifestId = new AsccpManifestId(record.get(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID).toBigInteger());
                AsccManifestId replacementAsccManifestId = (record.get(ASCC_MANIFEST.REPLACEMENT_ASCC_MANIFEST_ID) != null) ?
                        new AsccManifestId(record.get(ASCC_MANIFEST.REPLACEMENT_ASCC_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(ACC.STATE));
                return new AsccDetailsRecord(
                        library, release,
                        asccManifestId,
                        new AsccId(record.get(ASCC.ASCC_ID).toBigInteger()),
                        new Guid(record.get(ASCC.GUID)),

                        getAccSummary(fromAccManifestId),
                        (toAsccpManifestId != null) ?
                                repositoryFactory().asccpQueryRepository(requester())
                                        .getAsccpSummary(toAsccpManifestId) : null,
                        (record.get(SEQ_KEY.SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.PREV_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.PREV_SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.NEXT_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.NEXT_SEQ_KEY_ID).toBigInteger()) : null,
                        getAsccSummary(replacementAsccManifestId),
                        getAsccSummary(since(asccManifestId)),
                        getAsccSummary(lastChanged(asccManifestId)),

                        record.get(ASCC_MANIFEST.DEN),
                        new Cardinality(
                                record.get(ASCC.CARDINALITY_MIN),
                                record.get(ASCC.CARDINALITY_MAX)),
                        (byte) 1 == record.get(ASCC.IS_DEPRECATED),
                        state,
                        new Definition(
                                record.get(ASCC.DEFINITION),
                                record.get(ASCC.DEFINITION_SOURCE)),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASCC.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ASCC.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID) != null) ?
                                new AsccManifestId(record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID) != null) ?
                                new AsccManifestId(record.get(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    private AsccManifestId since(AsccManifestId asccManifestId) {
        if (asccManifestId == null) {
            return null;
        }
        Record2<ULong, ULong> record = dslContext().select(
                        ASCC_MANIFEST.ASCC_MANIFEST_ID,
                        ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ULong prevAsccManifestId = record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID);
        if (prevAsccManifestId != null) {
            return since(new AsccManifestId(prevAsccManifestId.toBigInteger()));
        } else {
            return new AsccManifestId(record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID).toBigInteger());
        }
    }

    private AsccManifestId lastChanged(AsccManifestId asccManifestId) {
        if (asccManifestId == null) {
            return null;
        }
        Record4<ULong, ULong, ULong, ULong> record = dslContext().select(
                        ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.ASCC_ID,
                        ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID, ASCC_MANIFEST.as("prev").ASCC_ID)
                .from(ASCC_MANIFEST)
                .join(ASCC_MANIFEST.as("prev")).on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID))
                .join(RELEASE).on(ASCC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        if (record.get(ASCC_MANIFEST.ASCC_ID).equals(record.get(ASCC_MANIFEST.as("prev").ASCC_ID))) {
            return lastChanged(new AsccManifestId(record.get(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID).toBigInteger()));
        } else {
            return new AsccManifestId(record.get(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID).toBigInteger());
        }
    }

    @Override
    public AsccSummaryRecord getAsccSummary(AsccManifestId asccManifestId) {
        if (asccManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAsccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public AsccSummaryRecord getAsccSummary(AccManifestId fromAccManifestId, AsccpManifestId toAsccpManifestId) {
        if (fromAccManifestId == null || toAsccpManifestId == null) {
            return null;
        }
        var queryBuilder = new GetAsccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId)),
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(valueOf(toAsccpManifestId))
                ))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AsccSummaryRecord> getAsccSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetAsccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASCC_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsccSummaryRecord> getAsccSummaryList(AccManifestId fromAccManifestId) {
        if (fromAccManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsccSummaryRecord> getAsccSummaryList(AsccpManifestId toAsccpManifestId) {
        if (toAsccpManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(valueOf(toAsccpManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsccSummaryRecord> getAsccSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(ACC.STATE.eq(state.name()));
        }
        var queryBuilder = new GetAsccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private class GetAsccSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ASCC_MANIFEST.ASCC_MANIFEST_ID,
                            ASCC.ASCC_ID,
                            ASCC.GUID,

                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                            ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                            SEQ_KEY.SEQ_KEY_ID,
                            SEQ_KEY.PREV_SEQ_KEY_ID,
                            SEQ_KEY.NEXT_SEQ_KEY_ID,

                            ASCC_MANIFEST.DEN,
                            ASCC.DEFINITION,
                            ASCC.DEFINITION_SOURCE,
                            ASCC.CARDINALITY_MIN,
                            ASCC.CARDINALITY_MAX,
                            ASCC.IS_DEPRECATED,
                            ACC.STATE,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.REVISION_NUM,

                            ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID,
                            ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID
                    ), ownerFields()))
                    .from(ASCC_MANIFEST)
                    .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                    .join(ACC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(ASCC.OWNER_USER_ID))
                    .leftJoin(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(SEQ_KEY).on(ASCC_MANIFEST.SEQ_KEY_ID.eq(SEQ_KEY.SEQ_KEY_ID));
        }

        private RecordMapper<Record, AsccSummaryRecord> mapper() {
            return record -> {
                AsccManifestId asccManifestId = new AsccManifestId(record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID).toBigInteger());
                AccManifestId fromAccManifestId = new AccManifestId(record.get(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID).toBigInteger());
                AsccpManifestId toAsccpManifestId = new AsccpManifestId(record.get(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID).toBigInteger());
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
                CcState state = CcState.valueOf(record.get(ACC.STATE));
                return new AsccSummaryRecord(
                        library, release,
                        asccManifestId,
                        new AsccId(record.get(ASCC.ASCC_ID).toBigInteger()),
                        new Guid(record.get(ASCC.GUID)),

                        fromAccManifestId,
                        toAsccpManifestId,
                        (record.get(SEQ_KEY.SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.PREV_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.PREV_SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.NEXT_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.NEXT_SEQ_KEY_ID).toBigInteger()) : null,

                        record.get(ASCC_MANIFEST.DEN),
                        new Cardinality(
                                record.get(ASCC.CARDINALITY_MIN),
                                record.get(ASCC.CARDINALITY_MAX)),
                        (byte) 1 == record.get(ASCC.IS_DEPRECATED),
                        state,
                        new Definition(
                                record.get(ASCC.DEFINITION),
                                record.get(ASCC.DEFINITION_SOURCE)),

                        (record.get(LOG.REVISION_NUM) != null) ? record.get(LOG.REVISION_NUM).intValue() : 1,

                        fetchOwnerSummary(record),

                        (record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID) != null) ?
                                new AsccManifestId(record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID) != null) ?
                                new AsccManifestId(record.get(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    @Override
    public BccDetailsRecord getBccDetails(BccManifestId bccManifestId) {
        if (bccManifestId == null) {
            return null;
        }
        var queryBuilder = new GetBccDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<BccDetailsRecord> getBccDetailsList() {
        var queryBuilder = new GetBccDetailsQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BccDetailsRecord> getBccDetailsList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }
        Set<ReleaseId> libraryIdSet = releaseQueryRepository.getIncludedReleaseSummaryList(releaseId)
                .stream().map(e -> e.releaseId()).collect(Collectors.toSet());

        var queryBuilder = new GetBccDetailsQueryBuilder();
        return queryBuilder.select()
                .where(RELEASE.RELEASE_ID.in(valueOf(libraryIdSet)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BccDetailsRecord> getBccDetailsList(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBccDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetBccDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            BCC_MANIFEST.BCC_MANIFEST_ID,
                            BCC.BCC_ID,
                            BCC.GUID,

                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                            BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                            SEQ_KEY.SEQ_KEY_ID,
                            SEQ_KEY.PREV_SEQ_KEY_ID,
                            SEQ_KEY.NEXT_SEQ_KEY_ID,
                            BCC_MANIFEST.REPLACEMENT_BCC_MANIFEST_ID,

                            BCC_MANIFEST.DEN,
                            BCC.ENTITY_TYPE,
                            BCC.DEFINITION,
                            BCC.DEFINITION_SOURCE,
                            BCC.CARDINALITY_MIN,
                            BCC.CARDINALITY_MAX,
                            BCC.IS_DEPRECATED,
                            BCC.IS_NILLABLE,
                            BCC.DEFAULT_VALUE,
                            BCC.FIXED_VALUE,
                            ACC.STATE,
                            BCC.CREATION_TIMESTAMP,
                            BCC.LAST_UPDATE_TIMESTAMP,

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

                            BCC_MANIFEST.PREV_BCC_MANIFEST_ID,
                            BCC_MANIFEST.NEXT_BCC_MANIFEST_ID
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BCC_MANIFEST)
                    .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                    .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(BCC.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BCC.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BCC.LAST_UPDATED_BY))
                    .leftJoin(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(SEQ_KEY).on(BCC_MANIFEST.SEQ_KEY_ID.eq(SEQ_KEY.SEQ_KEY_ID));
        }

        private RecordMapper<Record, BccDetailsRecord> mapper() {
            return record -> {
                BccManifestId bccManifestId = new BccManifestId(record.get(BCC_MANIFEST.BCC_MANIFEST_ID).toBigInteger());
                AccManifestId fromAccManifestId = new AccManifestId(record.get(BCC_MANIFEST.FROM_ACC_MANIFEST_ID).toBigInteger());
                BccpManifestId toBccpManifestId = new BccpManifestId(record.get(BCC_MANIFEST.TO_BCCP_MANIFEST_ID).toBigInteger());
                BccManifestId replacementBccManifestId = (record.get(BCC_MANIFEST.REPLACEMENT_BCC_MANIFEST_ID) != null) ?
                        new BccManifestId(record.get(BCC_MANIFEST.REPLACEMENT_BCC_MANIFEST_ID).toBigInteger()) : null;
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
                CcState state = CcState.valueOf(record.get(ACC.STATE));
                return new BccDetailsRecord(
                        library, release,
                        bccManifestId,
                        new BccId(record.get(BCC.BCC_ID).toBigInteger()),
                        new Guid(record.get(BCC.GUID)),

                        getAccSummary(fromAccManifestId),
                        (toBccpManifestId != null) ?
                                repositoryFactory().bccpQueryRepository(requester())
                                        .getBccpSummary(toBccpManifestId) : null,
                        (record.get(SEQ_KEY.SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.PREV_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.PREV_SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.NEXT_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.NEXT_SEQ_KEY_ID).toBigInteger()) : null,
                        getBccSummary(replacementBccManifestId),
                        getBccSummary(since(bccManifestId)),
                        getBccSummary(lastChanged(bccManifestId)),

                        EntityType.valueOf(record.get(BCC.ENTITY_TYPE)),
                        record.get(BCC_MANIFEST.DEN),
                        new Cardinality(
                                record.get(BCC.CARDINALITY_MIN),
                                record.get(BCC.CARDINALITY_MAX)),
                        (byte) 1 == record.get(BCC.IS_DEPRECATED),
                        (byte) 1 == record.get(BCC.IS_NILLABLE),
                        state,
                        new ValueConstraint(
                                record.get(BCC.DEFAULT_VALUE),
                                record.get(BCC.FIXED_VALUE)),
                        new Definition(
                                record.get(BCC.DEFINITION),
                                record.get(BCC.DEFINITION_SOURCE)),

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BCC.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BCC.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID) != null) ?
                                new BccManifestId(record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID) != null) ?
                                new BccManifestId(record.get(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID).toBigInteger()) : null
                );
            };
        }
    }

    private BccManifestId since(BccManifestId bccManifestId) {
        if (bccManifestId == null) {
            return null;
        }
        Record2<ULong, ULong> record = dslContext().select(
                        BCC_MANIFEST.BCC_MANIFEST_ID,
                        BCC_MANIFEST.PREV_BCC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ULong prevBccManifestId = record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID);
        if (prevBccManifestId != null) {
            return since(new BccManifestId(prevBccManifestId.toBigInteger()));
        } else {
            return new BccManifestId(record.get(BCC_MANIFEST.BCC_MANIFEST_ID).toBigInteger());
        }
    }

    private BccManifestId lastChanged(BccManifestId bccManifestId) {
        if (bccManifestId == null) {
            return null;
        }
        Record4<ULong, ULong, ULong, ULong> record = dslContext().select(
                        BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.BCC_ID,
                        BCC_MANIFEST.as("prev").BCC_MANIFEST_ID, BCC_MANIFEST.as("prev").BCC_ID)
                .from(BCC_MANIFEST)
                .join(BCC_MANIFEST.as("prev")).on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID))
                .join(RELEASE).on(BCC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        if (record.get(BCC_MANIFEST.BCC_ID).equals(record.get(BCC_MANIFEST.as("prev").BCC_ID))) {
            return lastChanged(new BccManifestId(record.get(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID).toBigInteger()));
        } else {
            return new BccManifestId(record.get(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID).toBigInteger());
        }
    }

    @Override
    public BccSummaryRecord getBccSummary(BccManifestId bccManifestId) {
        if (bccManifestId == null) {
            return null;
        }
        var queryBuilder = new GetBccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public BccSummaryRecord getBccSummary(AccManifestId fromAccManifestId, BccpManifestId toBccpManifestId) {
        if (fromAccManifestId == null || toBccpManifestId == null) {
            return null;
        }
        var queryBuilder = new GetBccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId)),
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(valueOf(toBccpManifestId))
                ))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<BccSummaryRecord> getBccSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetBccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BCC_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BccSummaryRecord> getBccSummaryList(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BccSummaryRecord> getBccSummaryList(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BccSummaryRecord> getBccSummaryList(LibraryId libraryId, String releaseNum, CcState state) {
        if (libraryId == null || releaseNum == null) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        conditions.add(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)));
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (state != null) {
            conditions.add(ACC.STATE.eq(state.name()));
        }
        var queryBuilder = new GetBccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private class GetBccSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            BCC_MANIFEST.BCC_MANIFEST_ID,
                            BCC.BCC_ID,
                            BCC.GUID,

                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                            BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                            SEQ_KEY.SEQ_KEY_ID,
                            SEQ_KEY.PREV_SEQ_KEY_ID,
                            SEQ_KEY.NEXT_SEQ_KEY_ID,

                            BCC_MANIFEST.DEN,
                            BCC.ENTITY_TYPE,
                            BCC.DEFINITION,
                            BCC.DEFINITION_SOURCE,
                            BCC.CARDINALITY_MIN,
                            BCC.CARDINALITY_MAX,
                            BCC.IS_DEPRECATED,
                            BCC.IS_NILLABLE,
                            BCC.DEFAULT_VALUE,
                            BCC.FIXED_VALUE,
                            ACC.STATE,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"),

                            LOG.REVISION_NUM,

                            BCC_MANIFEST.PREV_BCC_MANIFEST_ID,
                            BCC_MANIFEST.NEXT_BCC_MANIFEST_ID
                    ), ownerFields()))
                    .from(BCC_MANIFEST)
                    .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                    .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(BCC.OWNER_USER_ID))
                    .leftJoin(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .leftJoin(SEQ_KEY).on(BCC_MANIFEST.SEQ_KEY_ID.eq(SEQ_KEY.SEQ_KEY_ID));
        }

        private RecordMapper<Record, BccSummaryRecord> mapper() {
            return record -> {
                BccManifestId bccManifestId = new BccManifestId(record.get(BCC_MANIFEST.BCC_MANIFEST_ID).toBigInteger());
                AccManifestId fromAccManifestId = new AccManifestId(record.get(BCC_MANIFEST.FROM_ACC_MANIFEST_ID).toBigInteger());
                BccpManifestId toBccpManifestId = new BccpManifestId(record.get(BCC_MANIFEST.TO_BCCP_MANIFEST_ID).toBigInteger());
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
                CcState state = CcState.valueOf(record.get(ACC.STATE));
                return new BccSummaryRecord(
                        library, release,
                        bccManifestId,
                        new BccId(record.get(BCC.BCC_ID).toBigInteger()),
                        new Guid(record.get(BCC.GUID)),

                        fromAccManifestId,
                        toBccpManifestId,
                        (record.get(SEQ_KEY.SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.PREV_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.PREV_SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.NEXT_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.NEXT_SEQ_KEY_ID).toBigInteger()) : null,

                        EntityType.valueOf(record.get(BCC.ENTITY_TYPE)),
                        record.get(BCC_MANIFEST.DEN),
                        new Cardinality(
                                record.get(BCC.CARDINALITY_MIN),
                                record.get(BCC.CARDINALITY_MAX)),
                        (byte) 1 == record.get(BCC.IS_DEPRECATED),
                        (byte) 1 == record.get(BCC.IS_NILLABLE),
                        state,
                        new ValueConstraint(
                                record.get(BCC.DEFAULT_VALUE),
                                record.get(BCC.FIXED_VALUE)),
                        new Definition(
                                record.get(BCC.DEFINITION),
                                record.get(BCC.DEFINITION_SOURCE)),

                        (record.get(LOG.REVISION_NUM) != null) ? record.get(LOG.REVISION_NUM).intValue() : 1,

                        fetchOwnerSummary(record),

                        (record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID) != null) ?
                                new BccManifestId(record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID) != null) ?
                                new BccManifestId(record.get(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID).toBigInteger()) : null
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
                .from(ACC)
                .where(ACC.NAMESPACE_ID.eq(valueOf(namespaceId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    private boolean hasChild(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return false;
        }
        AccManifestId basedAccManifestId = getBasedAccManifestId(accManifestId);
        if (basedAccManifestId != null) {
            return true;
        }
        long asccCount = dslContext().selectCount()
                .from(ASCC_MANIFEST)
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(and(
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)),
                        ASCC.STATE.notEqual(CcState.Deleted.name())
                ))
                .fetchOneInto(long.class);
        if (asccCount > 0) {
            return true;
        }

        long bccCount = dslContext().selectCount()
                .from(BCC_MANIFEST)
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(and(
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)),
                        BCC.STATE.notEqual(CcState.Deleted.name())
                ))
                .fetchOneInto(long.class);
        return bccCount > 0;
    }

    private boolean hasExtension(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return false;
        }
        if (dslContext().selectCount()
                .from(ASCC_MANIFEST)
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(and(
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(valueOf(accManifestId)),
                        ASCCP.TYPE.eq(AsccpType.Extension.name())
                ))
                .fetchOneInto(long.class) > 0) {
            return true;
        } else {
            AccManifestId basedAccManifestId = getBasedAccManifestId(accManifestId);
            if (basedAccManifestId != null) {
                return hasExtension(basedAccManifestId);
            } else {
                return false;
            }
        }
    }

    private AccManifestId getBasedAccManifestId(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        BigInteger val = dslContext().select(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetchOptionalInto(BigInteger.class).orElse(null);
        return (val != null) ? new AccManifestId(val) : null;
    }

    @Override
    public boolean hasSamePropertyTerm(AccManifestId accManifestId, String propertyTerm) {

        if (accManifestId == null) {
            throw new IllegalArgumentException("`accManifestId` must not be null.");
        }

        if (!hasLength(propertyTerm)) {
            throw new IllegalArgumentException("`propertyTerm` must not be empty.");
        }

        AccSummaryRecord acc = getAccSummary(accManifestId);
        if (acc == null) {
            throw new IllegalArgumentException("The ACC manifest record with ID " + accManifestId + " could not be found.");
        }

        var asccpQuery = repositoryFactory().asccpQueryRepository(requester());
        for (AsccSummaryRecord ascc : getAsccSummaryList(accManifestId)) {
            AsccpDetailsRecord asccp = asccpQuery.getAsccpDetails(ascc.toAsccpManifestId());

            if (asccp.roleOfAcc().isGroup()) {
                if (hasSamePropertyTerm(asccp.roleOfAcc().accManifestId(), propertyTerm)) {
                    return true;
                }
            } else {
                if (StringUtils.equals(propertyTerm, asccp.propertyTerm())) {
                    return true;
                }
            }
        }

        var bccpQuery = repositoryFactory().bccpQueryRepository(requester());
        for (BccSummaryRecord bcc : getBccSummaryList(accManifestId)) {
            BccpDetailsRecord bccp = bccpQuery.getBccpDetails(bcc.toBccpManifestId());

            if (StringUtils.equals(propertyTerm, bccp.propertyTerm())) {
                return true;
            }
        }

        if (acc.basedAccManifestId() != null) {
            return hasSamePropertyTerm(acc.basedAccManifestId(), propertyTerm);
        }

        return false;
    }

    @Override
    public AccSummaryRecord getAllExtensionAccManifest(ReleaseId releaseId) {
        if (releaseId == null) {
            return null;
        }
        var queryBuilder = new GetAccSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.TYPE.eq(AccType.AllExtension.name())
                ))
                .fetchOptional(queryBuilder.mapper()).orElse(null);
    }

    @Override
    public AccSummaryRecord getExistsUserExtension(AccManifestId accManifestId) {
        BigInteger value = dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC.as("eAcc"))
                .join(ACC_MANIFEST.as("eACCRM")).on(ACC.as("eAcc").ACC_ID.eq(ACC_MANIFEST.as("eACCRM").ACC_ID))
                .join(ASCC_MANIFEST).on(ACC_MANIFEST.as("eACCRM").ACC_MANIFEST_ID.eq(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID))
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(
                        ACC_MANIFEST.as("eACCRM").ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId.value())),
                        ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue())
                )).fetchOneInto(BigInteger.class);
        AccManifestId ueAccManifestId = (value != null) ? new AccManifestId(value) : null;
        return getAccSummary(ueAccManifestId);
    }

    @Override
    public CcRefactorValidationResponse validateAsccRefactoring(AsccManifestId asccManifestId, AccManifestId accManifestId) {

        AsccManifestRecord asccManifestRecord = dslContext().selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(
                        valueOf(asccManifestId)
                ))
                .fetchOne();

        int usedBieCount = dslContext().selectCount().from(ASBIE)
                .where(ASBIE.BASED_ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId())).fetchOne(0, int.class);

        if (usedBieCount > 0) {
            throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be refactored.");
        }

        CcRefactorValidationResponse response = new CcRefactorValidationResponse();
        response.setType(CcType.ASCC);
        response.setManifestId(asccManifestId);

        Map<AccManifestId, List<String>> issueMap = getBlockerReasonMap(asccManifestId, accManifestId);

        List<CcRefactorValidationResponse.IssuedCc> issuedCcList = dslContext().select(
                        inline("ACC").as("type"),
                        ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
                        ACC.ACC_ID.as("id"),
                        ACC.GUID,
                        ACC_MANIFEST.DEN,
                        ACC.OBJECT_CLASS_TERM,
                        ACC.OAGIS_COMPONENT_TYPE.as("oagis_component_type"),
                        ACC.STATE,
                        ACC.IS_DEPRECATED,
                        ACC.LAST_UPDATE_TIMESTAMP,
                        APP_USER.as("appUserOwner").LOGIN_ID.as("owner"),
                        APP_USER.as("appUserOwner").IS_DEVELOPER.as("owned_by_developer"),
                        APP_USER.as("appUserUpdater").LOGIN_ID.as("last_update_user"),
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        RELEASE.RELEASE_NUM)
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID).and(ACC_MANIFEST.RELEASE_ID.eq(asccManifestRecord.getReleaseId())))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(APP_USER.as("appUserOwner"))
                .on(ACC.OWNER_USER_ID.eq(APP_USER.as("appUserOwner").APP_USER_ID))
                .join(APP_USER.as("appUserUpdater"))
                .on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("appUserUpdater").APP_USER_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(valueOf(issueMap.keySet())))
                .fetchStream().map(row -> {
                    CcRefactorValidationResponse.IssuedCc issuedCc = new CcRefactorValidationResponse.IssuedCc();
                    issuedCc.setManifestId(new AccManifestId(row.getValue("manifest_id", ULong.class).toBigInteger()));
                    issuedCc.setId(new AccId(row.getValue("id", ULong.class).toBigInteger()));
                    issuedCc.setGuid(row.getValue("guid", String.class));
                    issuedCc.setDen(row.getValue("den", String.class));
                    issuedCc.setName(row.getValue("object_class_term", String.class));
                    Integer componentType = row.getValue("oagis_component_type", Integer.class);
                    if (componentType != null) {
                        issuedCc.setOagisComponentType(OagisComponentType.valueOf(componentType));
                    }
                    issuedCc.setState(CcState.valueOf(row.getValue("state", String.class)));
                    issuedCc.setDeprecated(row.getValue("is_deprecated", Byte.class) == 1);
                    issuedCc.setLastUpdateTimestamp(Date.from(row.getValue("last_update_timestamp", LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    issuedCc.setOwner((String) row.getValue("owner"));
                    issuedCc.setLastUpdateUser((String) row.getValue("last_update_user"));
                    issuedCc.setRevision(row.getValue(LOG.REVISION_NUM).toString());
                    issuedCc.setReleaseNum(row.getValue(RELEASE.RELEASE_NUM));
                    issuedCc.setReasons(issueMap.getOrDefault(issuedCc.getManifestId(), Collections.emptyList()));
                    return issuedCc;
                }).collect(Collectors.toList());

        response.setIssueList(issuedCcList);

        return response;
    }

    private Map<AccManifestId, List<String>> getBlockerReasonMap(AsccManifestId asccManifestId, AccManifestId targetAccManifestId) {

        AsccSummaryRecord ascc = getAsccSummary(asccManifestId);
        ReleaseId releaseId = ascc.release().releaseId();

        var accQuery = repositoryFactory().accQueryRepository(requester());
        List<AccSummaryRecord> accManifestList = accQuery.getAccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, AccSummaryRecord> accManifestMap = accManifestList.stream()
                .collect(Collectors.toMap(AccSummaryRecord::accManifestId, Function.identity()));
        Map<AccManifestId, List<AccSummaryRecord>> baseAccMap = accManifestList.stream()
                .filter(e -> e.basedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccSummaryRecord::basedAccManifestId));

        List<AsccSummaryRecord> asccList = accQuery.getAsccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, List<AsccSummaryRecord>> fromAccAsccMap = asccList.stream()
                .collect(Collectors.groupingBy(AsccSummaryRecord::fromAccManifestId));

        List<AccManifestId> accManifestIdList = new ArrayList<>();

        accManifestIdList.add(targetAccManifestId);

        Set<AccManifestId> accCandidates = new HashSet<>();

        for (AccManifestId cur : accManifestIdList) {
            accCandidates.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Map<AccManifestId, AccManifestId> groupMap = new HashMap<>();

        dslContext().select(ACC_MANIFEST.as("group").ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ASCC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                .join(ACC_MANIFEST.as("group")).on(ACC_MANIFEST.as("group").ACC_MANIFEST_ID.eq(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.as("group").ACC_ID.eq(ACC.ACC_ID))
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.OAGIS_COMPONENT_TYPE.in(OagisComponentType.SemanticGroup.getValue(),
                                OagisComponentType.UserExtensionGroup.getValue()),
                        ACC_MANIFEST.ACC_MANIFEST_ID.in(accCandidates)))
                .fetchStream().forEach(r -> {
                    groupMap.put(
                            new AccManifestId(r.get(ACC_MANIFEST.as("group").ACC_MANIFEST_ID).toBigInteger()),
                            new AccManifestId(r.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()));
                });

        accCandidates.addAll(groupMap.keySet());

        Set<AsccSummaryRecord> asccResult = new HashSet<>();

        for (AccManifestId acc : accCandidates) {
            asccResult.addAll(
                    fromAccAsccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(e -> e.toAsccpManifestId().equals(ascc.toAsccpManifestId())
                                    && !e.asccManifestId().equals(ascc.asccManifestId()))
                            .collect(Collectors.toList()));
        }

        Map<AccManifestId, List<String>> map = new HashMap<>();

        for (AsccSummaryRecord asccR : asccResult) {
            AccSummaryRecord acc = accManifestMap.get(asccR.fromAccManifestId());
            map.computeIfAbsent(acc.accManifestId(), k -> new ArrayList<>());
            if (acc.state() != WIP) {
                map.get(acc.accManifestId()).add("Direct association: 'WIP' state required.");
            }

            if (!acc.owner().userId().equals(requester().userId())
                    && acc.state() != Production
                    && acc.state() != Published) {
                map.get(acc.accManifestId()).add("Direct association: Ownership required.");
            }

            if (acc.isGroup()) {
                AccSummaryRecord parentAccManifest = accManifestMap.get(groupMap.get(acc.accManifestId()));
                map.put(parentAccManifest.accManifestId(), map.get(acc.accManifestId()));
                map.remove(acc.accManifestId());
                map.get(parentAccManifest.accManifestId()).add("Ungrouping '" + acc.objectClassTerm() + "' required.");
            }
        }

        if (map.get(targetAccManifestId) == null) {
            AccSummaryRecord acc = accManifestMap.get(targetAccManifestId);
            map.computeIfAbsent(acc.accManifestId(), k -> new ArrayList<>());
            if (acc.state() != WIP) {
                map.get(acc.accManifestId()).add("Direct association: 'WIP' state required.");
            }

            if (!acc.owner().userId().equals(requester().userId())
                    && acc.state() != Production
                    && acc.state() != Published) {
                map.get(acc.accManifestId()).add("Direct association: Ownership required.");
            }
        }

        return map;
    }

    @Override
    public CcRefactorValidationResponse validateBccRefactoring(BccManifestId bccManifestId, AccManifestId accManifestId) {
        BccManifestRecord bccManifestRecord = dslContext().selectFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .fetchOne();

        int usedBieCount = dslContext().selectCount().from(BBIE)
                .where(BBIE.BASED_BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId())).fetchOne(0, int.class);

        if (usedBieCount > 0) {
            throw new IllegalArgumentException("This association used in " + usedBieCount + " BIE(s). Can not be refactored.");
        }

        CcRefactorValidationResponse response = new CcRefactorValidationResponse();
        response.setType(CcType.BCC);
        response.setManifestId(bccManifestId);

        Map<AccManifestId, List<String>> issueMap = getBlockerReasonMap(bccManifestId, accManifestId);

        List<CcRefactorValidationResponse.IssuedCc> issuedCcList = dslContext().select(
                        inline("ACC").as("type"),
                        ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
                        ACC.ACC_ID.as("id"),
                        ACC.GUID,
                        ACC_MANIFEST.DEN,
                        ACC.OBJECT_CLASS_TERM,
                        ACC.OAGIS_COMPONENT_TYPE.as("oagis_component_type"),
                        ACC.STATE,
                        ACC.IS_DEPRECATED,
                        ACC.LAST_UPDATE_TIMESTAMP,
                        APP_USER.as("appUserOwner").LOGIN_ID.as("owner"),
                        APP_USER.as("appUserOwner").IS_DEVELOPER.as("owned_by_developer"),
                        APP_USER.as("appUserUpdater").LOGIN_ID.as("last_update_user"),
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        RELEASE.RELEASE_NUM)
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID).and(ACC_MANIFEST.RELEASE_ID.eq(bccManifestRecord.getReleaseId())))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(APP_USER.as("appUserOwner"))
                .on(ACC.OWNER_USER_ID.eq(APP_USER.as("appUserOwner").APP_USER_ID))
                .join(APP_USER.as("appUserUpdater"))
                .on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("appUserUpdater").APP_USER_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(valueOf(issueMap.keySet())))
                .fetchStream().map(row -> {
                    CcRefactorValidationResponse.IssuedCc issuedCc = new CcRefactorValidationResponse.IssuedCc();
                    issuedCc.setManifestId(new AccManifestId(row.getValue("manifest_id", ULong.class).toBigInteger()));
                    issuedCc.setId(new AccId(row.getValue("id", ULong.class).toBigInteger()));
                    issuedCc.setGuid(row.getValue("guid", String.class));
                    issuedCc.setDen(row.getValue("den", String.class));
                    issuedCc.setName(row.getValue("object_class_term", String.class));
                    Integer componentType = row.getValue("oagis_component_type", Integer.class);
                    if (componentType != null) {
                        issuedCc.setOagisComponentType(OagisComponentType.valueOf(componentType));
                    }
                    issuedCc.setState(CcState.valueOf(row.getValue("state", String.class)));
                    issuedCc.setDeprecated(row.getValue("is_deprecated", Byte.class) == 1);
                    issuedCc.setLastUpdateTimestamp(Date.from(row.getValue("last_update_timestamp", LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    issuedCc.setOwner((String) row.getValue("owner"));
                    issuedCc.setLastUpdateUser((String) row.getValue("last_update_user"));
                    issuedCc.setRevision(row.getValue(LOG.REVISION_NUM).toString());
                    issuedCc.setReleaseNum(row.getValue(RELEASE.RELEASE_NUM));
                    issuedCc.setReasons(issueMap.getOrDefault(issuedCc.getManifestId(), Collections.emptyList()));
                    return issuedCc;
                }).collect(Collectors.toList());

        response.setIssueList(issuedCcList);

        return response;
    }

    private Map<AccManifestId, List<String>> getBlockerReasonMap(BccManifestId bccManifestId, AccManifestId targetAccManifestId) {

        BccSummaryRecord bcc = getBccSummary(bccManifestId);
        ReleaseId releaseId = bcc.release().releaseId();

        var accQuery = repositoryFactory().accQueryRepository(requester());
        List<AccSummaryRecord> accManifestList = accQuery.getAccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, AccSummaryRecord> accManifestMap = accManifestList.stream()
                .collect(Collectors.toMap(AccSummaryRecord::accManifestId, Function.identity()));
        Map<AccManifestId, List<AccSummaryRecord>> baseAccMap = accManifestList.stream()
                .filter(e -> e.basedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccSummaryRecord::basedAccManifestId));

        List<BccSummaryRecord> asccList = accQuery.getBccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, List<BccSummaryRecord>> fromAccBccMap = asccList.stream()
                .collect(Collectors.groupingBy(BccSummaryRecord::fromAccManifestId));

        List<AccManifestId> accManifestIdList = new ArrayList<>();

        accManifestIdList.add(targetAccManifestId);

        Set<AccManifestId> accCandidates = new HashSet<>();

        for (AccManifestId cur : accManifestIdList) {
            accCandidates.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Map<AccManifestId, AccManifestId> groupMap = new HashMap<>();

        dslContext().select(ACC_MANIFEST.as("group").ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ASCC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                .join(ACC_MANIFEST.as("group")).on(ACC_MANIFEST.as("group").ACC_MANIFEST_ID.eq(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.as("group").ACC_ID.eq(ACC.ACC_ID))
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.OAGIS_COMPONENT_TYPE.in(OagisComponentType.SemanticGroup.getValue(),
                                OagisComponentType.UserExtensionGroup.getValue()),
                        ACC_MANIFEST.ACC_MANIFEST_ID.in(accCandidates)))
                .fetchStream().forEach(r -> {
                    groupMap.put(
                            new AccManifestId(r.get(ACC_MANIFEST.as("group").ACC_MANIFEST_ID).toBigInteger()),
                            new AccManifestId(r.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()));
                });

        accCandidates.addAll(groupMap.keySet());

        Set<BccSummaryRecord> bccResult = new HashSet<>();

        for (AccManifestId acc : accCandidates) {
            bccResult.addAll(
                    fromAccBccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(e -> e.toBccpManifestId().equals(bcc.toBccpManifestId())
                                    && !e.bccManifestId().equals(bcc.bccManifestId()))
                            .collect(Collectors.toList()));
        }

        Map<AccManifestId, List<String>> map = new HashMap<>();

        for (BccSummaryRecord bccR : bccResult) {
            AccSummaryRecord acc = accManifestMap.get(bccR.fromAccManifestId());
            map.computeIfAbsent(acc.accManifestId(), k -> new ArrayList<>());
            if (acc.state() != WIP) {
                map.get(acc.accManifestId()).add("Direct association: 'WIP' state required.");
            }

            if (!acc.owner().userId().equals(requester().userId())
                    && acc.state() != Production
                    && acc.state() != Published) {
                map.get(acc.accManifestId()).add("Direct association: Ownership required.");
            }

            if (acc.isGroup()) {
                AccSummaryRecord parentAccManifest = accManifestMap.get(groupMap.get(acc.accManifestId()));
                map.put(parentAccManifest.accManifestId(), map.get(acc.accManifestId()));
                map.remove(acc.accManifestId());
                map.get(parentAccManifest.accManifestId()).add("Ungrouping '" + acc.objectClassTerm() + "' required.");
            }
        }

        if (map.get(targetAccManifestId) == null) {
            AccSummaryRecord acc = accManifestMap.get(targetAccManifestId);
            map.computeIfAbsent(acc.accManifestId(), k -> new ArrayList<>());
            if (acc.state() != WIP) {
                map.get(acc.accManifestId()).add("Direct association: 'WIP' state required.");
            }

            if (!acc.owner().userId().equals(requester().userId())
                    && acc.state() != Production
                    && acc.state() != Published) {
                map.get(acc.accManifestId()).add("Direct association: Ownership required.");
            }
        }

        return map;
    }

    private List<AccManifestId> getBaseAccManifestId(AccManifestId accManifestId, Map<AccManifestId, List<AccSummaryRecord>> baseAccMap) {
        List<AccManifestId> result = new ArrayList<>();
        result.add(accManifestId);
        if (baseAccMap.containsKey(accManifestId)) {
            baseAccMap.get(accManifestId).forEach(e -> {
                result.addAll(getBaseAccManifestId(e.accManifestId(), baseAccMap));
            });
        }
        return result;
    }

    @Override
    public List<AsccSummaryRecord> getRefactorTargetAsccManifestList(AsccManifestId asccManifestId, AccManifestId accManifestId) {

        AsccSummaryRecord ascc = getAsccSummary(asccManifestId);
        ReleaseId releaseId = ascc.release().releaseId();

        var accQuery = repositoryFactory().accQueryRepository(requester());
        List<AccSummaryRecord> accManifestList = accQuery.getAccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, List<AccSummaryRecord>> baseAccMap = accManifestList.stream()
                .filter(e -> e.basedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccSummaryRecord::basedAccManifestId));

        List<AsccSummaryRecord> asccList = accQuery.getAsccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, List<AsccSummaryRecord>> fromAccAsccMap = asccList.stream()
                .collect(Collectors.groupingBy(AsccSummaryRecord::fromAccManifestId));

        List<AccManifestId> accManifestIdList = new ArrayList<>();

        accManifestIdList.add(accManifestId);

        Set<AccManifestId> accCandidates = new HashSet<>();

        for (AccManifestId cur : accManifestIdList) {
            accCandidates.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Set<AsccSummaryRecord> asccResult = new HashSet<>();

        for (AccManifestId acc : accCandidates) {
            asccResult.addAll(
                    fromAccAsccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(e -> e.toAsccpManifestId().equals(ascc.toAsccpManifestId()))
                            .collect(Collectors.toList()));
        }
        return new ArrayList<>(asccResult);
    }

    @Override
    public List<BccSummaryRecord> getRefactorTargetBccManifestList(BccManifestId bccManifestId, AccManifestId accManifestId) {

        BccSummaryRecord bcc = getBccSummary(bccManifestId);
        ReleaseId releaseId = bcc.release().releaseId();

        var accQuery = repositoryFactory().accQueryRepository(requester());
        List<AccSummaryRecord> accManifestList = accQuery.getAccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, List<AccSummaryRecord>> baseAccMap = accManifestList.stream()
                .filter(e -> e.basedAccManifestId() != null)
                .collect(Collectors.groupingBy(AccSummaryRecord::basedAccManifestId));

        List<BccSummaryRecord> bccList = accQuery.getBccSummaryList(Arrays.asList(releaseId));
        Map<AccManifestId, List<BccSummaryRecord>> fromAccBccMap = bccList.stream()
                .collect(Collectors.groupingBy(BccSummaryRecord::fromAccManifestId));

        List<AccManifestId> accManifestIdList = new ArrayList<>();

        accManifestIdList.add(accManifestId);

        Set<AccManifestId> accCandidates = new HashSet<>();

        for (AccManifestId cur : accManifestIdList) {
            accCandidates.addAll(getBaseAccManifestId(cur, baseAccMap));
        }

        Set<BccSummaryRecord> bccResult = new HashSet<>();

        for (AccManifestId acc : accCandidates) {
            bccResult.addAll(
                    fromAccBccMap.getOrDefault(acc, Collections.emptyList())
                            .stream()
                            .filter(e -> e.toBccpManifestId().equals(bcc.toBccpManifestId()))
                            .collect(Collectors.toList()));
        }
        return new ArrayList<>(bccResult);
    }

}
