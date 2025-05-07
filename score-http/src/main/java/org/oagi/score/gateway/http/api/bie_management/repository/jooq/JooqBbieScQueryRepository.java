package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
import org.oagi.score.gateway.http.api.bie_management.model.PrimitiveRestriction;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditUsed;
import org.oagi.score.gateway.http.api.bie_management.repository.BbieScQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
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
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BBIE_SC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.TOP_LEVEL_ASBIEP;
import static org.springframework.util.StringUtils.hasLength;

public class JooqBbieScQueryRepository extends JooqBaseRepository implements BbieScQueryRepository {

    public JooqBbieScQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BbieScSummaryRecord getBbieScSummary(BbieScId bbieId) {
        if (bbieId == null) {
            return null;
        }
        var queryBuilder = new GetBbieScSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BBIE_SC.BBIE_SC_ID.eq(valueOf(bbieId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<BbieScSummaryRecord> getBbieScSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList) {
        if (ownerTopLevelAsbiepIdList == null || ownerTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBbieScSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(ownerTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BbieScSummaryRecord> getBbieScSummaryList(BbieId bbieId, TopLevelAsbiepId topLevelAsbiepId) {
        if (bbieId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBbieScSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        BBIE_SC.BBIE_ID.eq(valueOf(bbieId)),
                        BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId))
                ))
                .fetch(queryBuilder.mapper());
    }

    private class GetBbieScSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BBIE_SC.BBIE_SC_ID,
                            BBIE_SC.GUID,
                            BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                            BBIE_SC.BBIE_ID,
                            BBIE_SC.PATH,
                            BBIE_SC.HASH_PATH,
                            BBIE_SC.XBT_MANIFEST_ID,
                            BBIE_SC.CODE_LIST_MANIFEST_ID,
                            BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                            BBIE_SC.CARDINALITY_MIN,
                            BBIE_SC.CARDINALITY_MAX,
                            BBIE_SC.DEFAULT_VALUE,
                            BBIE_SC.FIXED_VALUE,
                            BBIE_SC.FACET_MIN_LENGTH,
                            BBIE_SC.FACET_MAX_LENGTH,
                            BBIE_SC.FACET_PATTERN,
                            BBIE_SC.DEFINITION,
                            BBIE_SC.REMARK,
                            BBIE_SC.BIZ_TERM,
                            BBIE_SC.DISPLAY_NAME,
                            BBIE_SC.EXAMPLE,
                            BBIE_SC.IS_DEPRECATED,
                            BBIE_SC.IS_USED,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            BBIE_SC.CREATION_TIMESTAMP,
                            BBIE_SC.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BBIE_SC)
                    .join(TOP_LEVEL_ASBIEP).on(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BBIE_SC.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BBIE_SC.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, BbieScSummaryRecord> mapper() {
            return record -> {
                BbieScId bbieScId = new BbieScId(record.get(BBIE_SC.BBIE_SC_ID).toBigInteger());
                DtScManifestId basedDtScManifestId = new DtScManifestId(record.get(BBIE_SC.BASED_DT_SC_MANIFEST_ID).toBigInteger());
                BbieId bbieId = new BbieId(record.get(BBIE_SC.BBIE_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new BbieScSummaryRecord(bbieScId,
                        new Guid(record.get(BBIE_SC.GUID)),
                        basedDtScManifestId,
                        bbieId,
                        record.get(BBIE_SC.PATH),
                        record.get(BBIE_SC.HASH_PATH),
                        new Cardinality(
                                record.get(BBIE_SC.CARDINALITY_MIN),
                                record.get(BBIE_SC.CARDINALITY_MAX)),
                        new PrimitiveRestriction(
                                (record.get(BBIE_SC.XBT_MANIFEST_ID) != null) ?
                                        new XbtManifestId(record.get(BBIE_SC.XBT_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE_SC.CODE_LIST_MANIFEST_ID) != null) ?
                                        new CodeListManifestId(record.get(BBIE_SC.CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                        new AgencyIdListManifestId(record.get(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null
                        ),
                        (record.get(BBIE_SC.DEFAULT_VALUE) != null || record.get(BBIE_SC.FIXED_VALUE) != null) ?
                                new ValueConstraint(record.get(BBIE_SC.DEFAULT_VALUE), record.get(BBIE_SC.FIXED_VALUE)) : null,
                        (record.get(BBIE_SC.FACET_MIN_LENGTH) != null || record.get(BBIE_SC.FACET_MAX_LENGTH) != null || record.get(BBIE_SC.FACET_PATTERN) != null) ?
                                new Facet(
                                        record.get(BBIE_SC.FACET_MIN_LENGTH).toBigInteger(),
                                        record.get(BBIE_SC.FACET_MAX_LENGTH).toBigInteger(),
                                        record.get(BBIE_SC.FACET_PATTERN)) : null,
                        record.get(BBIE_SC.DEFINITION),
                        record.get(BBIE_SC.REMARK),
                        record.get(BBIE_SC.BIZ_TERM),
                        record.get(BBIE_SC.DISPLAY_NAME),
                        record.get(BBIE_SC.EXAMPLE),
                        (byte) 1 == record.get(BBIE_SC.IS_DEPRECATED),
                        (byte) 1 == record.get(BBIE_SC.IS_USED),
                        BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)),
                        ownerTopLevelAsbiepId,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BBIE_SC.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BBIE_SC.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public BbieScDetailsRecord getBbieScDetails(BbieScId bbieScId) {
        if (bbieScId == null) {
            return null;
        }
        var queryBuilder = new GetBbieScDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BBIE_SC.BBIE_SC_ID.eq(valueOf(bbieScId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public BbieScDetailsRecord getBbieScDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        if (topLevelAsbiepId == null || !hasLength(hashPath)) {
            return null;
        }
        var queryBuilder = new GetBbieScDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())),
                        BBIE_SC.HASH_PATH.eq(hashPath)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetBbieScDetailsQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BBIE_SC.BBIE_SC_ID,
                            BBIE_SC.GUID,
                            BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                            BBIE_SC.BBIE_ID,
                            BBIE_SC.PATH,
                            BBIE_SC.HASH_PATH,
                            BBIE_SC.XBT_MANIFEST_ID,
                            BBIE_SC.CODE_LIST_MANIFEST_ID,
                            BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                            BBIE_SC.CARDINALITY_MIN,
                            BBIE_SC.CARDINALITY_MAX,
                            BBIE_SC.DEFAULT_VALUE,
                            BBIE_SC.FIXED_VALUE,
                            BBIE_SC.FACET_MIN_LENGTH,
                            BBIE_SC.FACET_MAX_LENGTH,
                            BBIE_SC.FACET_PATTERN,
                            BBIE_SC.DEFINITION,
                            BBIE_SC.REMARK,
                            BBIE_SC.BIZ_TERM,
                            BBIE_SC.DISPLAY_NAME,
                            BBIE_SC.EXAMPLE,
                            BBIE_SC.IS_DEPRECATED,
                            BBIE_SC.IS_USED,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            BBIE_SC.CREATION_TIMESTAMP,
                            BBIE_SC.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BBIE_SC)
                    .join(TOP_LEVEL_ASBIEP).on(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BBIE_SC.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BBIE_SC.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, BbieScDetailsRecord> mapper() {
            return record -> {
                var dtQuery = repositoryFactory().dtQueryRepository(requester());
                var topLevelAsbiepQuery = repositoryFactory().topLevelAsbiepQueryRepository(requester());

                BbieScId bbieScId = new BbieScId(record.get(BBIE_SC.BBIE_SC_ID).toBigInteger());
                DtScManifestId basedDtScManifestId = new DtScManifestId(record.get(BBIE_SC.BASED_DT_SC_MANIFEST_ID).toBigInteger());
                BbieId bbieId = new BbieId(record.get(BBIE_SC.BBIE_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new BbieScDetailsRecord(bbieScId,
                        new Guid(record.get(BBIE_SC.GUID)),
                        dtQuery.getDtScSummary(basedDtScManifestId),
                        bbieId,
                        record.get(BBIE_SC.PATH),
                        record.get(BBIE_SC.HASH_PATH),
                        new Cardinality(
                                record.get(BBIE_SC.CARDINALITY_MIN),
                                record.get(BBIE_SC.CARDINALITY_MAX)),
                        new PrimitiveRestriction(
                                (record.get(BBIE_SC.XBT_MANIFEST_ID) != null) ?
                                        new XbtManifestId(record.get(BBIE_SC.XBT_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE_SC.CODE_LIST_MANIFEST_ID) != null) ?
                                        new CodeListManifestId(record.get(BBIE_SC.CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                        new AgencyIdListManifestId(record.get(BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null
                        ),
                        (record.get(BBIE_SC.DEFAULT_VALUE) != null || record.get(BBIE_SC.FIXED_VALUE) != null) ?
                                new ValueConstraint(record.get(BBIE_SC.DEFAULT_VALUE), record.get(BBIE_SC.FIXED_VALUE)) : null,
                        (record.get(BBIE_SC.FACET_MIN_LENGTH) != null || record.get(BBIE_SC.FACET_MAX_LENGTH) != null || record.get(BBIE_SC.FACET_PATTERN) != null) ?
                                new Facet(
                                        record.get(BBIE_SC.FACET_MIN_LENGTH).toBigInteger(),
                                        record.get(BBIE_SC.FACET_MAX_LENGTH).toBigInteger(),
                                        record.get(BBIE_SC.FACET_PATTERN)) : null,
                        record.get(BBIE_SC.DEFINITION),
                        record.get(BBIE_SC.REMARK),
                        record.get(BBIE_SC.BIZ_TERM),
                        record.get(BBIE_SC.DISPLAY_NAME),
                        record.get(BBIE_SC.EXAMPLE),
                        (byte) 1 == record.get(BBIE_SC.IS_DEPRECATED),
                        (byte) 1 == record.get(BBIE_SC.IS_USED),
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(ownerTopLevelAsbiepId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BBIE_SC.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BBIE_SC.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public List<BieEditUsed> getUsedBbieScList(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().select(BBIE_SC.IS_USED, BBIE_SC.BBIE_SC_ID, BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                        BBIE_SC.HASH_PATH, BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID,
                        BBIE_SC.DISPLAY_NAME,
                        BBIE_SC.CARDINALITY_MIN, BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.IS_DEPRECATED)
                .from(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetchStream().map(record -> {
                    BieEditUsed bieEditUsed = new BieEditUsed();
                    bieEditUsed.setUsed(record.get(BBIE_SC.IS_USED) == 1);
                    bieEditUsed.setType("BBIE_SC");
                    bieEditUsed.setBieId(new BbieScId(record.get(BBIE_SC.BBIE_SC_ID).toBigInteger()));
                    bieEditUsed.setManifestId(new DtScManifestId(record.get(BBIE_SC.BASED_DT_SC_MANIFEST_ID).toBigInteger()));
                    bieEditUsed.setHashPath(record.get(BBIE_SC.HASH_PATH));
                    bieEditUsed.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
                    bieEditUsed.setDisplayName(record.get(BBIE_SC.DISPLAY_NAME));
                    bieEditUsed.setCardinalityMin(record.get(BBIE_SC.CARDINALITY_MIN));
                    bieEditUsed.setCardinalityMax(record.get(BBIE_SC.CARDINALITY_MAX));
                    bieEditUsed.setDeprecated(record.get(BBIE_SC.IS_DEPRECATED) == 1);
                    return bieEditUsed;
                })
                .collect(Collectors.toList());
    }

}
