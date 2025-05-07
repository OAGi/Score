package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditUsed;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditRef;
import org.oagi.score.gateway.http.api.bie_management.repository.AsbieQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAsbieQueryRepository extends JooqBaseRepository implements AsbieQueryRepository {

    public JooqAsbieQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AsbieSummaryRecord getAsbieSummary(AsbieId asbieId) {
        if (asbieId == null) {
            return null;
        }
        var queryBuilder = new GetAsbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(ASBIE.ASBIE_ID.eq(valueOf(asbieId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<AsbieSummaryRecord> getAsbieSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList) {
        if (ownerTopLevelAsbiepIdList == null || ownerTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(ownerTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<AsbieSummaryRecord> getAsbieSummaryList(AbieId fromAbieId, TopLevelAsbiepId topLevelAsbiepId) {
        if (fromAbieId == null || topLevelAsbiepId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetAsbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ASBIE.FROM_ABIE_ID.eq(valueOf(fromAbieId)),
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId))
                ))
                .fetch(queryBuilder.mapper());
    }

    private class GetAsbieSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            ASBIE.ASBIE_ID,
                            ASBIE.GUID,
                            ASBIE.BASED_ASCC_MANIFEST_ID,
                            ASBIE.FROM_ABIE_ID,
                            ASBIE.TO_ASBIEP_ID,
                            ASBIE.PATH,
                            ASBIE.HASH_PATH,
                            ASBIE.CARDINALITY_MIN,
                            ASBIE.CARDINALITY_MAX,
                            ASBIE.DEFINITION,
                            ASBIE.REMARK,
                            ASBIE.IS_NILLABLE,
                            ASBIE.IS_DEPRECATED,
                            ASBIE.IS_USED,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            ASBIE.CREATION_TIMESTAMP,
                            ASBIE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASBIE)
                    .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASBIE.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ASBIE.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, AsbieSummaryRecord> mapper() {
            return record -> {
                AsbieId asbieId = new AsbieId(record.get(ASBIE.ASBIE_ID).toBigInteger());
                AsccManifestId basedAsccManifestId = new AsccManifestId(record.get(ASBIE.BASED_ASCC_MANIFEST_ID).toBigInteger());
                AbieId fromAbieId = new AbieId(record.get(ASBIE.FROM_ABIE_ID).toBigInteger());
                AsbiepId toAsbiepId = new AsbiepId(record.get(ASBIE.TO_ASBIEP_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new AsbieSummaryRecord(asbieId,
                        new Guid(record.get(ASBIE.GUID)),
                        basedAsccManifestId,
                        record.get(ASBIE.PATH),
                        record.get(ASBIE.HASH_PATH),
                        fromAbieId,
                        toAsbiepId,
                        new Cardinality(
                                record.get(ASBIE.CARDINALITY_MIN),
                                record.get(ASBIE.CARDINALITY_MAX)),
                        record.get(ASBIE.DEFINITION),
                        record.get(ASBIE.REMARK),
                        (byte) 1 == record.get(ASBIE.IS_NILLABLE),
                        (byte) 1 == record.get(ASBIE.IS_DEPRECATED),
                        (byte) 1 == record.get(ASBIE.IS_USED),
                        BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)),
                        ownerTopLevelAsbiepId,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASBIE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ASBIE.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public AsbieDetailsRecord getAsbieDetails(AsbieId asbieId) {
        if (asbieId == null) {
            return null;
        }
        var queryBuilder = new GetAsbieDetailsQueryBuilder();
        return queryBuilder.select()
                .where(ASBIE.ASBIE_ID.eq(valueOf(asbieId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public AsbieDetailsRecord getAsbieDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        if (topLevelAsbiepId == null || !hasLength(hashPath)) {
            return null;
        }
        var queryBuilder = new GetAsbieDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())),
                        ASBIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetAsbieDetailsQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            ASBIE.ASBIE_ID,
                            ASBIE.GUID,
                            ASBIE.BASED_ASCC_MANIFEST_ID,
                            ASBIE.FROM_ABIE_ID,
                            ASBIE.TO_ASBIEP_ID,
                            ASBIE.PATH,
                            ASBIE.HASH_PATH,
                            ASBIE.CARDINALITY_MIN,
                            ASBIE.CARDINALITY_MAX,
                            ASBIE.DEFINITION,
                            ASBIE.REMARK,
                            ASBIE.IS_NILLABLE,
                            ASBIE.IS_DEPRECATED,
                            ASBIE.IS_USED,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            ASBIE.CREATION_TIMESTAMP,
                            ASBIE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(ASBIE)
                    .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(ASBIE.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(ASBIE.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, AsbieDetailsRecord> mapper() {
            return record -> {
                var accQuery = repositoryFactory().accQueryRepository(requester());
                var topLevelAsbiepQuery = repositoryFactory().topLevelAsbiepQueryRepository(requester());

                AsbieId asbieId = new AsbieId(record.get(ASBIE.ASBIE_ID).toBigInteger());
                AsccManifestId basedAsccManifestId = new AsccManifestId(record.get(ASBIE.BASED_ASCC_MANIFEST_ID).toBigInteger());
                AbieId fromAbieId = new AbieId(record.get(ASBIE.FROM_ABIE_ID).toBigInteger());
                AsbiepId toAsbiepId = new AsbiepId(record.get(ASBIE.TO_ASBIEP_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new AsbieDetailsRecord(asbieId,
                        new Guid(record.get(ASBIE.GUID)),
                        accQuery.getAsccSummary(basedAsccManifestId),
                        record.get(ASBIE.PATH),
                        record.get(ASBIE.HASH_PATH),
                        fromAbieId,
                        toAsbiepId,
                        new Cardinality(
                                record.get(ASBIE.CARDINALITY_MIN),
                                record.get(ASBIE.CARDINALITY_MAX)),
                        record.get(ASBIE.DEFINITION),
                        record.get(ASBIE.REMARK),
                        (byte) 1 == record.get(ASBIE.IS_NILLABLE),
                        (byte) 1 == record.get(ASBIE.IS_DEPRECATED),
                        (byte) 1 == record.get(ASBIE.IS_USED),
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(ownerTopLevelAsbiepId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(ASBIE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(ASBIE.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public List<BieEditUsed> getUsedAsbieList(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().select(ASBIE.IS_USED, ASBIE.ASBIE_ID, ASBIE.BASED_ASCC_MANIFEST_ID,
                        ASBIE.HASH_PATH, ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID,
                        ASBIEP.DISPLAY_NAME,
                        ASBIE.CARDINALITY_MIN, ASBIE.CARDINALITY_MAX,
                        ASBIE.IS_DEPRECATED)
                .from(ASBIE)
                .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetchStream().map(record -> {
                    BieEditUsed bieEditUsed = new BieEditUsed();
                    bieEditUsed.setUsed(record.get(ASBIE.IS_USED) == 1);
                    bieEditUsed.setType("ASBIE");
                    bieEditUsed.setBieId(new AsbieId(record.get(ASBIE.ASBIE_ID).toBigInteger()));
                    bieEditUsed.setManifestId(new AsccManifestId(record.get(ASBIE.BASED_ASCC_MANIFEST_ID).toBigInteger()));
                    bieEditUsed.setHashPath(record.get(ASBIE.HASH_PATH));
                    bieEditUsed.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
                    bieEditUsed.setDisplayName(record.get(ASBIEP.DISPLAY_NAME));
                    bieEditUsed.setCardinalityMin(record.get(ASBIE.CARDINALITY_MIN));
                    bieEditUsed.setCardinalityMax(record.get(ASBIE.CARDINALITY_MAX));
                    bieEditUsed.setDeprecated(record.get(ASBIE.IS_DEPRECATED) == 1);
                    return bieEditUsed;
                }).collect(Collectors.toList());
    }

    public List<BieEditRef> getBieRefList(TopLevelAsbiepId topLevelAsbiepId) {
        if (topLevelAsbiepId == null) {
            return Collections.emptyList();
        }

        List<BieEditRef> bieEditRefList = new ArrayList();
        List<BieEditRef> refTopLevelAsbiepIdList = getRefTopLevelAsbiepIdList(topLevelAsbiepId);
        bieEditRefList.addAll(refTopLevelAsbiepIdList);

        if (!bieEditRefList.isEmpty()) {
            refTopLevelAsbiepIdList.stream().map(e -> e.getRefTopLevelAsbiepId()).distinct().forEach(refTopLevelAsbiepId -> {
                bieEditRefList.addAll(getBieRefList(refTopLevelAsbiepId));
            });
        }
        return bieEditRefList;
    }

    private List<BieEditRef> getRefTopLevelAsbiepIdList(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().select(
                        ASBIE.ASBIE_ID,
                        ASBIE.HASH_PATH,
                        ASBIE.BASED_ASCC_MANIFEST_ID,
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.as("top_level_asbiep_id"),
                        TOP_LEVEL_ASBIEP.as("asbie_top_level_asbiep").BASED_TOP_LEVEL_ASBIEP_ID.as("based_top_level_asbiep_id"),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.as("ref_top_level_asbiep_id"),
                        TOP_LEVEL_ASBIEP.as("asbiep_top_level_asbiep").BASED_TOP_LEVEL_ASBIEP_ID.as("ref_based_top_level_asbiep_id"),
                        TOP_LEVEL_ASBIEP.as("asbiep_top_level_asbiep").INVERSE_MODE)
                .from(ASBIE)
                .join(ASBIEP).on(
                        and(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID),
                                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID)))
                .join(TOP_LEVEL_ASBIEP.as("asbie_top_level_asbiep"))
                .on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.as("asbie_top_level_asbiep").TOP_LEVEL_ASBIEP_ID))
                .join(TOP_LEVEL_ASBIEP.as("asbiep_top_level_asbiep"))
                .on(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.as("asbiep_top_level_asbiep").TOP_LEVEL_ASBIEP_ID))
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetch(record -> {
                    BieEditRef bieEditRef = new BieEditRef();
                    bieEditRef.setAsbieId(new AsbieId(record.get(ASBIE.ASBIE_ID).toBigInteger()));
                    bieEditRef.setBasedAsccManifestId(new AsccManifestId(record.get(ASBIE.BASED_ASCC_MANIFEST_ID).toBigInteger()));
                    bieEditRef.setHashPath(record.get(ASBIE.HASH_PATH));
                    bieEditRef.setTopLevelAsbiepId(new TopLevelAsbiepId(record.get(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.as("top_level_asbiep_id")).toBigInteger()));
                    ULong basedTopLevelAsbiepId = record.get(TOP_LEVEL_ASBIEP.as("asbie_top_level_asbiep").BASED_TOP_LEVEL_ASBIEP_ID.as("based_top_level_asbiep_id"));
                    if (basedTopLevelAsbiepId != null) {
                        bieEditRef.setBasedTopLevelAsbiepId(new TopLevelAsbiepId(basedTopLevelAsbiepId.toBigInteger()));
                    }
                    bieEditRef.setRefTopLevelAsbiepId(new TopLevelAsbiepId(record.get(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.as("ref_top_level_asbiep_id")).toBigInteger()));
                    ULong refBasedTopLevelAsbiepId = record.get(TOP_LEVEL_ASBIEP.as("asbiep_top_level_asbiep").BASED_TOP_LEVEL_ASBIEP_ID.as("ref_based_top_level_asbiep_id"));
                    if (refBasedTopLevelAsbiepId != null) {
                        bieEditRef.setRefBasedTopLevelAsbiepId(new TopLevelAsbiepId(refBasedTopLevelAsbiepId.toBigInteger()));
                    }
                    bieEditRef.setRefInverseMode(record.get(TOP_LEVEL_ASBIEP.INVERSE_MODE) == 1);
                    return bieEditRef;
                });
    }

}
