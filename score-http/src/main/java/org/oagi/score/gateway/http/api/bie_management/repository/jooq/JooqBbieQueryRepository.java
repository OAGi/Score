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
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditUsed;
import org.oagi.score.gateway.http.api.bie_management.repository.BbieQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
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
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

public class JooqBbieQueryRepository extends JooqBaseRepository implements BbieQueryRepository {

    public JooqBbieQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BbieSummaryRecord getBbieSummary(BbieId bbieId) {
        if (bbieId == null) {
            return null;
        }
        var queryBuilder = new GetBbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BBIE.BBIE_ID.eq(valueOf(bbieId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<BbieSummaryRecord> getBbieSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList) {
        if (ownerTopLevelAsbiepIdList == null || ownerTopLevelAsbiepIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(ownerTopLevelAsbiepIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<BbieSummaryRecord> getBbieSummaryList(AbieId fromAbieId, TopLevelAsbiepId topLevelAsbiepId) {
        if (fromAbieId == null || topLevelAsbiepId == null) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetBbieSummaryQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        BBIE.FROM_ABIE_ID.eq(valueOf(fromAbieId)),
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId))
                ))
                .fetch(queryBuilder.mapper());
    }

    private class GetBbieSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BBIE.BBIE_ID,
                            BBIE.GUID,
                            BBIE.BASED_BCC_MANIFEST_ID,
                            BBIE.FROM_ABIE_ID,
                            BBIE.TO_BBIEP_ID,
                            BBIE.PATH,
                            BBIE.HASH_PATH,
                            BBIE.XBT_MANIFEST_ID,
                            BBIE.CODE_LIST_MANIFEST_ID,
                            BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                            BBIE.CARDINALITY_MIN,
                            BBIE.CARDINALITY_MAX,
                            BBIE.DEFAULT_VALUE,
                            BBIE.FIXED_VALUE,
                            BBIE.FACET_MIN_LENGTH,
                            BBIE.FACET_MAX_LENGTH,
                            BBIE.FACET_PATTERN,
                            BBIE.DEFINITION,
                            BBIE.REMARK,
                            BBIE.EXAMPLE,
                            BBIE.IS_NILLABLE,
                            BBIE.IS_DEPRECATED,
                            BBIE.IS_USED,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            BBIE.CREATION_TIMESTAMP,
                            BBIE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BBIE)
                    .join(TOP_LEVEL_ASBIEP).on(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BBIE.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BBIE.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, BbieSummaryRecord> mapper() {
            return record -> {
                BbieId bbieId = new BbieId(record.get(BBIE.BBIE_ID).toBigInteger());
                BccManifestId basedBccManifestId = new BccManifestId(record.get(BBIE.BASED_BCC_MANIFEST_ID).toBigInteger());
                AbieId fromAbieId = new AbieId(record.get(BBIE.FROM_ABIE_ID).toBigInteger());
                BbiepId toBbiepId = new BbiepId(record.get(BBIE.TO_BBIEP_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new BbieSummaryRecord(bbieId,
                        new Guid(record.get(BBIE.GUID)),
                        basedBccManifestId,
                        record.get(BBIE.PATH),
                        record.get(BBIE.HASH_PATH),
                        fromAbieId,
                        toBbiepId,
                        new Cardinality(
                                record.get(BBIE.CARDINALITY_MIN),
                                record.get(BBIE.CARDINALITY_MAX)),
                        new PrimitiveRestriction(
                                (record.get(BBIE.XBT_MANIFEST_ID) != null) ?
                                        new XbtManifestId(record.get(BBIE.XBT_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE.CODE_LIST_MANIFEST_ID) != null) ?
                                        new CodeListManifestId(record.get(BBIE.CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                        new AgencyIdListManifestId(record.get(BBIE.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null
                        ),
                        (record.get(BBIE.DEFAULT_VALUE) != null || record.get(BBIE.FIXED_VALUE) != null) ?
                                new ValueConstraint(record.get(BBIE.DEFAULT_VALUE), record.get(BBIE.FIXED_VALUE)) : null,
                        (record.get(BBIE.FACET_MIN_LENGTH) != null || record.get(BBIE.FACET_MAX_LENGTH) != null || record.get(BBIE.FACET_PATTERN) != null) ?
                                new Facet(
                                        record.get(BBIE.FACET_MIN_LENGTH).toBigInteger(),
                                        record.get(BBIE.FACET_MAX_LENGTH).toBigInteger(),
                                        record.get(BBIE.FACET_PATTERN)) : null,
                        record.get(BBIE.DEFINITION),
                        record.get(BBIE.REMARK),
                        record.get(BBIE.EXAMPLE),
                        (byte) 1 == record.get(BBIE.IS_NILLABLE),
                        (byte) 1 == record.get(BBIE.IS_DEPRECATED),
                        (byte) 1 == record.get(BBIE.IS_USED),
                        BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)),
                        ownerTopLevelAsbiepId,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BBIE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BBIE.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public BbieDetailsRecord getBbieDetails(BbieId bbieId) {
        if (bbieId == null) {
            return null;
        }
        var queryBuilder = new GetBbieDetailsQueryBuilder();
        return queryBuilder.select()
                .where(BBIE.BBIE_ID.eq(valueOf(bbieId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public BbieDetailsRecord getBbieDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        if (topLevelAsbiepId == null || !hasLength(hashPath)) {
            return null;
        }
        var queryBuilder = new GetBbieDetailsQueryBuilder();
        return queryBuilder.select()
                .where(and(
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())),
                        BBIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetBbieDetailsQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            BBIE.BBIE_ID,
                            BBIE.GUID,
                            BBIE.BASED_BCC_MANIFEST_ID,
                            BBIE.FROM_ABIE_ID,
                            BBIE.TO_BBIEP_ID,
                            BBIE.PATH,
                            BBIE.HASH_PATH,
                            BBIE.XBT_MANIFEST_ID,
                            BBIE.CODE_LIST_MANIFEST_ID,
                            BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                            BBIE.CARDINALITY_MIN,
                            BBIE.CARDINALITY_MAX,
                            BBIE.DEFAULT_VALUE,
                            BBIE.FIXED_VALUE,
                            BBIE.FACET_MIN_LENGTH,
                            BBIE.FACET_MAX_LENGTH,
                            BBIE.FACET_PATTERN,
                            BBIE.DEFINITION,
                            BBIE.REMARK,
                            BBIE.EXAMPLE,
                            BBIE.IS_NILLABLE,
                            BBIE.IS_DEPRECATED,
                            BBIE.IS_USED,
                            TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                            TOP_LEVEL_ASBIEP.STATE,
                            BBIE.CREATION_TIMESTAMP,
                            BBIE.LAST_UPDATE_TIMESTAMP
                    ), ownerFields(), creatorFields(), updaterFields()))
                    .from(BBIE)
                    .join(TOP_LEVEL_ASBIEP).on(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(BBIE.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(BBIE.LAST_UPDATED_BY));
        }

        private RecordMapper<Record, BbieDetailsRecord> mapper() {
            return record -> {
                var accQuery = repositoryFactory().accQueryRepository(requester());
                var topLevelAsbiepQuery = repositoryFactory().topLevelAsbiepQueryRepository(requester());

                BbieId bbieId = new BbieId(record.get(BBIE.BBIE_ID).toBigInteger());
                BccManifestId basedBccManifestId = new BccManifestId(record.get(BBIE.BASED_BCC_MANIFEST_ID).toBigInteger());
                AbieId fromAbieId = new AbieId(record.get(BBIE.FROM_ABIE_ID).toBigInteger());
                BbiepId toBbiepId = new BbiepId(record.get(BBIE.TO_BBIEP_ID).toBigInteger());
                TopLevelAsbiepId ownerTopLevelAsbiepId =
                        new TopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
                return new BbieDetailsRecord(bbieId,
                        new Guid(record.get(BBIE.GUID)),
                        accQuery.getBccSummary(basedBccManifestId),
                        record.get(BBIE.PATH),
                        record.get(BBIE.HASH_PATH),
                        fromAbieId,
                        toBbiepId,
                        new Cardinality(
                                record.get(BBIE.CARDINALITY_MIN),
                                record.get(BBIE.CARDINALITY_MAX)),
                        new PrimitiveRestriction(
                                (record.get(BBIE.XBT_MANIFEST_ID) != null) ?
                                        new XbtManifestId(record.get(BBIE.XBT_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE.CODE_LIST_MANIFEST_ID) != null) ?
                                        new CodeListManifestId(record.get(BBIE.CODE_LIST_MANIFEST_ID).toBigInteger()) : null,
                                (record.get(BBIE.AGENCY_ID_LIST_MANIFEST_ID) != null) ?
                                        new AgencyIdListManifestId(record.get(BBIE.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()) : null
                        ),
                        (record.get(BBIE.DEFAULT_VALUE) != null || record.get(BBIE.FIXED_VALUE) != null) ?
                                new ValueConstraint(record.get(BBIE.DEFAULT_VALUE), record.get(BBIE.FIXED_VALUE)) : null,
                        (record.get(BBIE.FACET_MIN_LENGTH) != null || record.get(BBIE.FACET_MAX_LENGTH) != null || record.get(BBIE.FACET_PATTERN) != null) ?
                                new Facet(
                                        record.get(BBIE.FACET_MIN_LENGTH).toBigInteger(),
                                        record.get(BBIE.FACET_MAX_LENGTH).toBigInteger(),
                                        record.get(BBIE.FACET_PATTERN)) : null,
                        record.get(BBIE.DEFINITION),
                        record.get(BBIE.REMARK),
                        record.get(BBIE.EXAMPLE),
                        (byte) 1 == record.get(BBIE.IS_NILLABLE),
                        (byte) 1 == record.get(BBIE.IS_DEPRECATED),
                        (byte) 1 == record.get(BBIE.IS_USED),
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(ownerTopLevelAsbiepId),

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(BBIE.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(BBIE.LAST_UPDATE_TIMESTAMP))
                        ));
            };
        }
    }

    @Override
    public List<BieEditUsed> getUsedBbieList(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().select(BBIE.IS_USED, BBIE.BBIE_ID, BBIE.BASED_BCC_MANIFEST_ID,
                        BBIE.HASH_PATH, BBIE.OWNER_TOP_LEVEL_ASBIEP_ID,
                        BBIEP.DISPLAY_NAME,
                        BBIE.CARDINALITY_MIN, BBIE.CARDINALITY_MAX,
                        BBIE.IS_DEPRECATED)
                .from(BBIE)
                .join(BBIEP).on(and(
                        BBIE.TO_BBIEP_ID.eq(BBIEP.BBIEP_ID),
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .fetchStream().map(record -> {
                    BieEditUsed bieEditUsed = new BieEditUsed();
                    bieEditUsed.setUsed(record.get(BBIE.IS_USED) == 1);
                    bieEditUsed.setType("BBIE");
                    bieEditUsed.setBieId(new BbieId(record.get(BBIE.BBIE_ID).toBigInteger()));
                    bieEditUsed.setManifestId(new BccManifestId(record.get(BBIE.BASED_BCC_MANIFEST_ID).toBigInteger()));
                    bieEditUsed.setHashPath(record.get(BBIE.HASH_PATH));
                    bieEditUsed.setOwnerTopLevelAsbiepId(new TopLevelAsbiepId(record.get(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger()));
                    bieEditUsed.setDisplayName(record.get(BBIEP.DISPLAY_NAME));
                    bieEditUsed.setCardinalityMin(record.get(BBIE.CARDINALITY_MIN));
                    bieEditUsed.setCardinalityMax(record.get(BBIE.CARDINALITY_MAX));
                    bieEditUsed.setDeprecated(record.get(BBIE.IS_DEPRECATED) == 1);
                    return bieEditUsed;
                })
                .collect(Collectors.toList());
    }

}
