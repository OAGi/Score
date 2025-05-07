package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertAsbieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieNode;
import org.oagi.score.gateway.http.api.bie_management.repository.AsbieCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AsbieRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.Utility.emptyToNull;

public class JooqAsbieCommandRepository extends JooqBaseRepository implements AsbieCommandRepository {

    public JooqAsbieCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AsbieNode.Asbie upsertAsbie(UpsertAsbieRequest request) {
        AsbieNode.Asbie asbie = request.getAsbie();
        ULong topLevelAsbiepId = valueOf(request.getTopLevelAsbiepId());
        String hashPath = asbie.getHashPath();
        AsbieRecord asbieRecord = dslContext().selectFrom(ASBIE)
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        ASBIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        ScoreUser requester = requester();
        ULong requesterId = valueOf(requester.userId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (asbieRecord == null) {
            asbieRecord = new AsbieRecord();
            asbieRecord.setGuid(ScoreGuidUtils.randomGuid());
            asbieRecord.setBasedAsccManifestId(valueOf(asbie.getBasedAsccManifestId()));
            asbieRecord.setPath(asbie.getPath());
            asbieRecord.setHashPath(hashPath);
            asbieRecord.setFromAbieId(dslContext().select(ABIE.ABIE_ID)
                    .from(ABIE)
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            ABIE.HASH_PATH.eq(asbie.getFromAbieHashPath())
                    ))
                    .fetchOneInto(ULong.class));
            if (asbie.getToAsbiepId() != null) {
                asbieRecord.setToAsbiepId(valueOf(asbie.getToAsbiepId()));
            } else {
                asbieRecord.setToAsbiepId(dslContext().select(ASBIEP.ASBIEP_ID)
                        .from(ASBIEP)
                        .where(and(
                                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                                ASBIEP.HASH_PATH.eq(asbie.getToAsbiepHashPath())
                        ))
                        .fetchOneInto(ULong.class));
            }
            asbieRecord.setSeqKey(BigDecimal.valueOf(asbie.getSeqKey().longValue()));

            if (asbie.getUsed() != null) {
                asbieRecord.setIsUsed((byte) (asbie.getUsed() ? 1 : 0));
            }

            if (asbie.getNillable() != null) {
                asbieRecord.setIsNillable((byte) (asbie.getNillable() ? 1 : 0));
            }

            if (asbie.getDeprecated() != null) {
                asbieRecord.setIsDeprecated((byte) (asbie.getDeprecated() ? 1 : 0));
            }

            asbieRecord.setDefinition(asbie.getDefinition());

            var accQuery = repositoryFactory().accQueryRepository(requester());
            AsccSummaryRecord asccRecord = accQuery.getAsccSummary(asbie.getBasedAsccManifestId());
            if (asccRecord == null) {
                throw new IllegalArgumentException();
            }

            if (asbie.getCardinalityMin() == null) {
                asbieRecord.setCardinalityMin(asccRecord.cardinality().min());
            } else {
                asbieRecord.setCardinalityMin(asbie.getCardinalityMin());
            }

            if (asbie.getCardinalityMax() == null) {
                asbieRecord.setCardinalityMax(asccRecord.cardinality().max());
            } else {
                asbieRecord.setCardinalityMax(asbie.getCardinalityMax());
            }
            if (asbieRecord.getCardinalityMax() > 0 && asbieRecord.getCardinalityMin() > asbieRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            asbieRecord.setRemark(asbie.getRemark());

            asbieRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            asbieRecord.setCreatedBy(requesterId);
            asbieRecord.setLastUpdatedBy(requesterId);
            asbieRecord.setCreationTimestamp(timestamp);
            asbieRecord.setLastUpdateTimestamp(timestamp);

            asbieRecord.setAsbieId(
                    dslContext().insertInto(ASBIE)
                            .set(asbieRecord)
                            .returning(ASBIE.ASBIE_ID)
                            .fetchOne().getAsbieId()
            );
        } else {
            asbieRecord.setSeqKey(BigDecimal.valueOf(asbie.getSeqKey().longValue()));
            if (asbie.getToAsbiepId() != null) {
                asbieRecord.setToAsbiepId(valueOf(asbie.getToAsbiepId()));
            }

            if (asbie.getUsed() != null) {
                asbieRecord.setIsUsed((byte) (asbie.getUsed() ? 1 : 0));
            }

            if (asbie.getNillable() != null) {
                asbieRecord.setIsNillable((byte) (asbie.getNillable() ? 1 : 0));
            }

            if (asbie.getDeprecated() != null) {
                asbieRecord.setIsDeprecated((byte) (asbie.getDeprecated() ? 1 : 0));
            }

            if (asbie.getDefinition() != null) {
                asbieRecord.setDefinition(emptyToNull(asbie.getDefinition()));
            }

            if (asbie.getCardinalityMin() != null) {
                asbieRecord.setCardinalityMin(asbie.getCardinalityMin());
            }

            if (asbie.getCardinalityMax() != null) {
                asbieRecord.setCardinalityMax(asbie.getCardinalityMax());
            }

            if (asbieRecord.getCardinalityMax() > 0 && asbieRecord.getCardinalityMin() > asbieRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (asbie.getRemark() != null) {
                asbieRecord.setRemark(emptyToNull(asbie.getRemark()));
            }

            if (asbieRecord.changed()) {
                asbieRecord.setLastUpdatedBy(requesterId);
                asbieRecord.setLastUpdateTimestamp(timestamp);
                asbieRecord.update(
                        ASBIE.SEQ_KEY,
                        ASBIE.TO_ASBIEP_ID,
                        ASBIE.IS_USED,
                        ASBIE.IS_NILLABLE,
                        ASBIE.IS_DEPRECATED,
                        ASBIE.DEFINITION,
                        ASBIE.CARDINALITY_MIN,
                        ASBIE.CARDINALITY_MAX,
                        ASBIE.REMARK,
                        ASBIE.LAST_UPDATED_BY,
                        ASBIE.LAST_UPDATE_TIMESTAMP
                );
            }
        }

        return getAsbie(request.getTopLevelAsbiepId(), hashPath);
    }

    private AsbieNode.Asbie getAsbie(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        AsbieNode.Asbie asbie = new AsbieNode.Asbie();
        asbie.setHashPath(hashPath);

        var query = repositoryFactory().asbieQueryRepository(requester());
        AsbieDetailsRecord asbieDetails = query.getAsbieDetails(topLevelAsbiepId, hashPath);
        if (asbieDetails != null) {
            asbie.setOwnerTopLevelAsbiepId(asbieDetails.ownerTopLevelAsbiep().topLevelAsbiepId());
            asbie.setAsbieId(asbieDetails.asbieId());
            asbie.setToAsbiepId(asbieDetails.toAsbiepId());
            asbie.setGuid(asbieDetails.getGuid().value());
            asbie.setFromAbieHashPath(dslContext().select(ABIE.HASH_PATH)
                    .from(ABIE)
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)),
                            ABIE.ABIE_ID.eq(valueOf(asbieDetails.fromAbieId()))
                    ))
                    .fetchOneInto(String.class));
            asbie.setToAsbiepHashPath(dslContext().select(ASBIEP.HASH_PATH)
                    .from(ASBIEP)
                    .where(and(
                            ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)),
                            ASBIEP.ASBIEP_ID.eq(valueOf(asbieDetails.toAsbiepId()))
                    ))
                    .fetchOneInto(String.class));
            asbie.setBasedAsccManifestId(asbieDetails.basedAscc().asccManifestId());
            asbie.setUsed(asbieDetails.used());
            asbie.setCardinalityMin(asbieDetails.cardinality().min());
            asbie.setCardinalityMax(asbieDetails.cardinality().max());
            asbie.setNillable(asbieDetails.nillable());
            asbie.setRemark(asbieDetails.remark());
            asbie.setDefinition(asbieDetails.definition());
            asbie.setDeprecated(asbieDetails.deprecated());
        }

        return asbie;
    }

}
