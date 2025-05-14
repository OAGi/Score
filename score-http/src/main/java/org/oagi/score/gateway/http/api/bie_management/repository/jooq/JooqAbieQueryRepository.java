package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.AbieQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ABIE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.TOP_LEVEL_ASBIEP;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAbieQueryRepository extends JooqBaseRepository implements AbieQueryRepository {

    public JooqAbieQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AbieSummaryRecord getAbieSummary(AbieId abieId) {
        if (abieId == null) {
            return null;
        }
        var queryBuilder = new GetAbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ABIE.ABIE_ID.eq(valueOf(abieId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AbieSummaryRecord> getAbieSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList) {
        if (ownerTopLevelAsbiepIdList == null || ownerTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(ownerTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    private class GetAbieSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ABIE.ABIE_ID,
                            ABIE.GUID,
                            ABIE.BASED_ACC_MANIFEST_ID,
                            ABIE.PATH,
                            ABIE.HASH_PATH,
                            ABIE.DEFINITION,
                            ABIE.REMARK,
                            ABIE.BIZ_TERM,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            ABIE.CREATION_TIMESTAMP,
                            ABIE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ABIE)
                    .join(TOP_LEVEL_ASBIEP).on(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ABIE.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ABIE.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, AbieSummaryRecord> mapper() {
            return record -> {
                AbieId abieId = new AbieId(record.get(ABIE.ABIE_ID).toBigInteger());
                AccManifestId basedAccManifestId = new AccManifestId(record.get(ABIE.BASED_ACC_MANIFEST_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new AbieSummaryRecord(abieId,
                        new Guid(record.get(ABIE.GUID)),
                        basedAccManifestId,
                        record.get(ABIE.PATH),
                        record.get(ABIE.HASH_PATH),
                        record.get(ABIE.DEFINITION),
                        record.get(ABIE.REMARK),
                        record.get(ABIE.BIZ_TERM),
                        BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)),
                        ownerTopLevelAsbiepId,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ABIE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ABIE.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public AbieDetailsRecord getAbieDetails(AbieId abieId) {
        if (abieId == null) {
            return null;
        }
        var queryBuilder = new GetAbieDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ABIE.ABIE_ID.eq(valueOf(abieId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public AbieDetailsRecord getAbieDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        if (topLevelAsbiepId == null || !hasLength(hashPath)) {
            return null;
        }
        var queryBuilder = new GetAbieDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())),
                        ABIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAbieDetailsQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(concat(fields(
                            ABIE.ABIE_ID,
                            ABIE.GUID,
                            ABIE.BASED_ACC_MANIFEST_ID,
                            ABIE.PATH,
                            ABIE.HASH_PATH,
                            ABIE.DEFINITION,
                            ABIE.REMARK,
                            ABIE.BIZ_TERM,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            ABIE.CREATION_TIMESTAMP,
                            ABIE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ABIE)
                    .join(TOP_LEVEL_ASBIEP).on(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ABIE.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ABIE.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, AbieDetailsRecord> mapper() {
            return record -> {
                var accQuery = repositoryFactory().accQueryRepository(requester());
                var topLevelAsbiepQuery = repositoryFactory().topLevelAsbiepQueryRepository(requester());

                AbieId abieId = new AbieId(record.get(ABIE.ABIE_ID).toBigInteger());
                AccManifestId basedAccManifestId = new AccManifestId(record.get(ABIE.BASED_ACC_MANIFEST_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new AbieDetailsRecord(abieId,
                        new Guid(record.get(ABIE.GUID)),
                        accQuery.getAccSummary(basedAccManifestId),
                        record.get(ABIE.PATH),
                        record.get(ABIE.HASH_PATH),
                        record.get(ABIE.DEFINITION),
                        record.get(ABIE.REMARK),
                        record.get(ABIE.BIZ_TERM),
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(ownerTopLevelAsbiepId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ABIE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ABIE.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

}
