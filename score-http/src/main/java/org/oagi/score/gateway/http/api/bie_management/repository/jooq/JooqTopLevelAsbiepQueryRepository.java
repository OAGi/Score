package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqTopLevelAsbiepQueryRepository extends JooqBaseRepository implements TopLevelAsbiepQueryRepository {

    public JooqTopLevelAsbiepQueryRepository(DSLContext dslContext,
                                             ScoreUser requester,
                                             RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public TopLevelAsbiepSummaryRecord getTopLevelAsbiepSummary(TopLevelAsbiepId topLevelAsbiepId) {
        if (topLevelAsbiepId == null) {
            return null;
        }

        var queryBuilder = new GetTopLevelAsbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<TopLevelAsbiepSummaryRecord> getReusedTopLevelAsbiepSummaryList(TopLevelAsbiepId topLevelAsbiepId) {
        List<TopLevelAsbiepId> reusedTopLevelAsbiepIdList = getReusedTopLevelAsbiepIdList(topLevelAsbiepId);
        if (reusedTopLevelAsbiepIdList == null || reusedTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetTopLevelAsbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(reusedTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    private List<TopLevelAsbiepId> getReusedTopLevelAsbiepIdList(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().selectDistinct(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .from(ASBIE)
                .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(valueOf(topLevelAsbiepId)),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId))
                ))
                .fetch(record -> new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger()));
    }

    @Override
    public List<TopLevelAsbiepSummaryRecord> getReusingTopLevelAsbiepSummaryList(TopLevelAsbiepId topLevelAsbiepId) {
        List<TopLevelAsbiepId> reusingTopLevelAsbiepIdList = getReusingTopLevelAsbiepIdList(topLevelAsbiepId);
        if (reusingTopLevelAsbiepIdList == null || reusingTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetTopLevelAsbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(reusingTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    private List<TopLevelAsbiepId> getReusingTopLevelAsbiepIdList(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().selectDistinct(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .from(ASBIE)
                .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(TOP_LEVEL_ASBIEP).on(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(valueOf(topLevelAsbiepId))
                ))
                .fetch(record -> new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger()));
    }

    @Override
    public List<TopLevelAsbiepSummaryRecord> getRefTopLevelAsbiepSummaryList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {

        var queryBuilder = new GetTopLevelAsbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .join(ASBIE).on(and(
                        ASBIEP.ASBIEP_ID.eq(ASBIE.TO_ASBIEP_ID),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList)
                ))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<TopLevelAsbiepSummaryRecord> getDerivedTopLevelAsbiepSummaryList(TopLevelAsbiepId basedTopLevelAsbiepId) {
        if (basedTopLevelAsbiepId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetTopLevelAsbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.eq(valueOf(basedTopLevelAsbiepId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetTopLevelAsbiepSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.ASBIEP_ID,
                            ASBIEP.GUID,

                            ASCCP_MANIFEST.DEN,
                            ASCCP.PROPERTY_TERM,
                            ASBIEP.DISPLAY_NAME,
                            TOP_LEVEL_ASBIEP.VERSION,
                            TOP_LEVEL_ASBIEP.STATUS,
                            TOP_LEVEL_ASBIEP.STATE,

                            TOP_LEVEL_ASBIEP.IS_DEPRECATED,
                            TOP_LEVEL_ASBIEP.INVERSE_MODE,

                            ASBIEP.CREATION_TIMESTAMP,
                            TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state")
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(TOP_LEVEL_ASBIEP)
                    .join(RELEASE).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                    .join(ASBIEP).on(and(
                            TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID),
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                    ))
                    .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASBIEP.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, TopLevelAsbiepSummaryRecord> mapper() {
            return record -> {
                TopLevelAsbiepId topLevelAsbiepId = new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                TopLevelAsbiepId basedTopLevelAsbiepId =
                        (record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID) != null) ?
                                new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID).toBigInteger()) : null;
                AsbiepId asbiepId = (record.get(ASBIEP.ASBIEP_ID) != null) ?
                        new AsbiepId(record.get(ASBIEP.ASBIEP_ID).toBigInteger()) : null;
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

                return new TopLevelAsbiepSummaryRecord(library, release,
                        topLevelAsbiepId,
                        basedTopLevelAsbiepId,
                        asbiepId,
                        new Guid(record.get(ASBIEP.GUID)),

                        record.get(ASCCP_MANIFEST.DEN),
                        record.get(ASCCP.PROPERTY_TERM),
                        record.get(ASBIEP.DISPLAY_NAME),
                        record.get(TOP_LEVEL_ASBIEP.VERSION),
                        record.get(TOP_LEVEL_ASBIEP.STATUS),
                        BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)),

                        (byte) 1 == record.get(TOP_LEVEL_ASBIEP.IS_DEPRECATED),
                        (byte) 1 == record.get(TOP_LEVEL_ASBIEP.INVERSE_MODE),

                        fetchOwnerSummary(record),

                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASBIEP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public List<BusinessContextId> getAssignedBusinessContextList(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                .fetchStream().map(record ->
                        new BusinessContextId(record.get(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID).toBigInteger()))
                .collect(Collectors.toList());
    }

    @Override
    public int countReferences(AsccManifestId asccManifestId) {
        return dslContext().selectCount()
                .from(ASBIE)
                .where(ASBIE.BASED_ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
    }

    @Override
    public int countReferences(BccManifestId bccManifestId) {
        return dslContext().selectCount()
                .from(BBIE)
                .where(BBIE.BASED_BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
    }

    @Override
    public int countReferences(DtScManifestId dtScManifestId) {
        return dslContext().selectCount()
                .from(BBIE_SC)
                .where(BBIE_SC.BASED_DT_SC_MANIFEST_ID.eq(valueOf(dtScManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
    }

}
