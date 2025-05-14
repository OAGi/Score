package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.BbiepQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

public class JooqBbiepQueryRepository extends JooqBaseRepository implements BbiepQueryRepository {

    public JooqBbiepQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BbiepSummaryRecord getBbiepSummary(BbiepId bbiepId) {
        if (bbiepId == null) {
            return null;
        }
        var queryBuilder = new GetBbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BBIEP.BBIEP_ID.eq(valueOf(bbiepId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<BbiepSummaryRecord> getBbiepSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList) {
        if (ownerTopLevelAsbiepIdList == null || ownerTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(ownerTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    private class GetBbiepSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BBIEP.BBIEP_ID,
                            BBIEP.GUID,
                            BBIEP.BASED_BCCP_MANIFEST_ID,
                            BBIEP.PATH,
                            BBIEP.HASH_PATH,
                            BBIEP.DEFINITION,
                            BBIEP.REMARK,
                            BBIEP.BIZ_TERM,
                            BBIEP.DISPLAY_NAME,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            BBIEP.CREATION_TIMESTAMP,
                            BBIEP.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BBIEP)
                    .join(TOP_LEVEL_ASBIEP).on(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BBIEP.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BBIEP.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, BbiepSummaryRecord> mapper() {
            return record -> {
                BbiepId bbiepId = new BbiepId(record.get(BBIEP.BBIEP_ID).toBigInteger());
                BccpManifestId basedBccpManifestId = new BccpManifestId(record.get(BBIEP.BASED_BCCP_MANIFEST_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new BbiepSummaryRecord(bbiepId,
                        new Guid(record.get(BBIEP.GUID)),
                        basedBccpManifestId,
                        record.get(BBIEP.PATH),
                        record.get(BBIEP.HASH_PATH),
                        record.get(BBIEP.DEFINITION),
                        record.get(BBIEP.REMARK),
                        record.get(BBIEP.BIZ_TERM),
                        record.get(BBIEP.DISPLAY_NAME),
                        BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)),
                        ownerTopLevelAsbiepId,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BBIEP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BBIEP.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public BbiepDetailsRecord getBbiepDetails(BbiepId bbiepId) {
        if (bbiepId == null) {
            return null;
        }
        var queryBuilder = new GetBbiepDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BBIEP.BBIEP_ID.eq(valueOf(bbiepId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public BbiepDetailsRecord getBbiepDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        if (topLevelAsbiepId == null || !hasLength(hashPath)) {
            return null;
        }
        var queryBuilder = new GetBbiepDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())),
                        BBIEP.HASH_PATH.eq(hashPath)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetBbiepDetailsQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BBIEP.BBIEP_ID,
                            BBIEP.GUID,
                            BBIEP.BASED_BCCP_MANIFEST_ID,
                            BBIEP.PATH,
                            BBIEP.HASH_PATH,
                            BBIEP.DEFINITION,
                            BBIEP.REMARK,
                            BBIEP.BIZ_TERM,
                            BCCP.DEFAULT_VALUE,
                            BCCP.FIXED_VALUE,
                            BBIEP.DISPLAY_NAME,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            BBIEP.CREATION_TIMESTAMP,
                            BBIEP.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BBIEP)
                    .join(TOP_LEVEL_ASBIEP).on(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(BCCP_MANIFEST).on(BBIEP.BASED_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                    .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BBIEP.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BBIEP.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, BbiepDetailsRecord> mapper() {
            return record -> {
                var bccpQuery = repositoryFactory().bccpQueryRepository(requester());
                var topLevelAsbiepQuery = repositoryFactory().topLevelAsbiepQueryRepository(requester());

                BbiepId bbiepId = new BbiepId(record.get(BBIEP.BBIEP_ID).toBigInteger());
                BccpManifestId basedBccpManifestId = new BccpManifestId(record.get(BBIEP.BASED_BCCP_MANIFEST_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new BbiepDetailsRecord(bbiepId,
                        new Guid(record.get(BBIEP.GUID)),
                        bccpQuery.getBccpSummary(basedBccpManifestId),
                        record.get(BBIEP.PATH),
                        record.get(BBIEP.HASH_PATH),
                        record.get(BBIEP.DEFINITION),
                        record.get(BBIEP.REMARK),
                        record.get(BBIEP.BIZ_TERM),
                        (record.get(BCCP.DEFAULT_VALUE) != null || record.get(BCCP.FIXED_VALUE) != null) ?
                                new ValueConstraint(record.get(BCCP.DEFAULT_VALUE), record.get(BCCP.FIXED_VALUE)) : null,
                        record.get(BBIEP.DISPLAY_NAME),
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(ownerTopLevelAsbiepId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BBIEP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BBIEP.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

}
