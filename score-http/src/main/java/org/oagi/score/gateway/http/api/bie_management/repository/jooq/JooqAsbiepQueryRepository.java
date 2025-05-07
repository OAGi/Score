package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.AsbiepQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASBIEP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.TOP_LEVEL_ASBIEP;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAsbiepQueryRepository extends JooqBaseRepository implements AsbiepQueryRepository {

    public JooqAsbiepQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AsbiepSummaryRecord getAsbiepSummary(AsbiepId asbiepId) {
        if (asbiepId == null) {
            return null;
        }
        var queryBuilder = new GetAsbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASBIEP.ASBIEP_ID.eq(valueOf(asbiepId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AsbiepSummaryRecord> getAsbiepSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList) {
        if (ownerTopLevelAsbiepIdList == null || ownerTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsbiepSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(ownerTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    private class GetAsbiepSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            ASBIEP.ASBIEP_ID,
                            ASBIEP.GUID,
                            ASBIEP.BASED_ASCCP_MANIFEST_ID,
                            ASBIEP.ROLE_OF_ABIE_ID,
                            ASBIEP.PATH,
                            ASBIEP.HASH_PATH,
                            ASBIEP.DEFINITION,
                            ASBIEP.REMARK,
                            ASBIEP.BIZ_TERM,
                            ASBIEP.DISPLAY_NAME,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            ASBIEP.CREATION_TIMESTAMP,
                            ASBIEP.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASBIEP)
                    .join(TOP_LEVEL_ASBIEP).on(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASBIEP.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ASBIEP.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, AsbiepSummaryRecord> mapper() {
            return record -> {
                AsbiepId asbiepId = new AsbiepId(record.get(ASBIEP.ASBIEP_ID).toBigInteger());
                AsccpManifestId basedAsccpManifestId = new AsccpManifestId(record.get(ASBIEP.BASED_ASCCP_MANIFEST_ID).toBigInteger());
                AbieId roleOfAbieId = new AbieId(record.get(ASBIEP.ROLE_OF_ABIE_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new AsbiepSummaryRecord(asbiepId,
                        new Guid(record.get(ASBIEP.GUID)),
                        basedAsccpManifestId,
                        roleOfAbieId,
                        record.get(ASBIEP.PATH),
                        record.get(ASBIEP.HASH_PATH),
                        record.get(ASBIEP.DEFINITION),
                        record.get(ASBIEP.REMARK),
                        record.get(ASBIEP.BIZ_TERM),
                        record.get(ASBIEP.DISPLAY_NAME),
                        BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)),
                        ownerTopLevelAsbiepId,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASBIEP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ASBIEP.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public AsbiepDetailsRecord getAsbiepDetails(AsbiepId asbiepId) {
        if (asbiepId == null) {
            return null;
        }
        var queryBuilder = new GetAsbiepDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ASBIEP.ASBIEP_ID.eq(valueOf(asbiepId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public AsbiepDetailsRecord getAsbiepDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        if (topLevelAsbiepId == null || !hasLength(hashPath)) {
            return null;
        }
        var queryBuilder = new GetAsbiepDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())),
                        ASBIEP.HASH_PATH.eq(hashPath)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAsbiepDetailsQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            ASBIEP.ASBIEP_ID,
                            ASBIEP.GUID,
                            ASBIEP.BASED_ASCCP_MANIFEST_ID,
                            ASBIEP.ROLE_OF_ABIE_ID,
                            ASBIEP.PATH,
                            ASBIEP.HASH_PATH,
                            ASBIEP.DEFINITION,
                            ASBIEP.REMARK,
                            ASBIEP.BIZ_TERM,
                            ASBIEP.DISPLAY_NAME,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            ASBIEP.CREATION_TIMESTAMP,
                            ASBIEP.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASBIEP)
                    .join(TOP_LEVEL_ASBIEP).on(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASBIEP.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ASBIEP.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, AsbiepDetailsRecord> mapper() {
            return record -> {
                var asccpQuery = repositoryFactory().asccpQueryRepository(requester());
                var topLevelAsbiepQuery = repositoryFactory().topLevelAsbiepQueryRepository(requester());

                AsbiepId asbiepId = new AsbiepId(record.get(ASBIEP.ASBIEP_ID).toBigInteger());
                AsccpManifestId basedAsccpManifestId = new AsccpManifestId(record.get(ASBIEP.BASED_ASCCP_MANIFEST_ID).toBigInteger());
                AbieId roleOfAbieId = new AbieId(record.get(ASBIEP.ROLE_OF_ABIE_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new AsbiepDetailsRecord(asbiepId,
                        new Guid(record.get(ASBIEP.GUID)),
                        asccpQuery.getAsccpSummary(basedAsccpManifestId),
                        roleOfAbieId,
                        record.get(ASBIEP.PATH),
                        record.get(ASBIEP.HASH_PATH),
                        record.get(ASBIEP.DEFINITION),
                        record.get(ASBIEP.REMARK),
                        record.get(ASBIEP.BIZ_TERM),
                        record.get(ASBIEP.DISPLAY_NAME),
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(ownerTopLevelAsbiepId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASBIEP.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ASBIEP.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

}
