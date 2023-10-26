package org.oagi.score.repo.component.asbie;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsbieRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccRecord;
import org.oagi.score.repo.component.ascc.AsccReadRepository;
import org.oagi.score.service.common.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.helper.Utility.emptyToNull;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class AsbieWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AsccReadRepository asccReadRepository;

    @Autowired
    private AsbieReadRepository asbieReadRepository;

    public AsbieNode.Asbie upsertAsbie(UpsertAsbieRequest request) {
        AsbieNode.Asbie asbie = request.getAsbie();
        ULong topLevelAsbiepId = ULong.valueOf(request.getTopLevelAsbiepId());
        String hashPath = asbie.getHashPath();
        AsbieRecord asbieRecord = dslContext.selectFrom(ASBIE)
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        ASBIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong requesterId = ULong.valueOf(user.getAppUserId());

        if (asbieRecord == null) {
            asbieRecord = new AsbieRecord();
            asbieRecord.setGuid(ScoreGuid.randomGuid());
            asbieRecord.setBasedAsccManifestId(ULong.valueOf(asbie.getBasedAsccManifestId()));
            asbieRecord.setPath(asbie.getPath());
            asbieRecord.setHashPath(hashPath);
            asbieRecord.setFromAbieId(dslContext.select(ABIE.ABIE_ID)
                    .from(ABIE)
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            ABIE.HASH_PATH.eq(asbie.getFromAbieHashPath())
                    ))
                    .fetchOneInto(ULong.class));
            asbieRecord.setToAsbiepId(dslContext.select(ASBIEP.ASBIEP_ID)
                    .from(ASBIEP)
                    .where(and(
                            ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            ASBIEP.HASH_PATH.eq(asbie.getToAsbiepHashPath())
                    ))
                    .fetchOneInto(ULong.class));
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
            AsccRecord asccRecord = asccReadRepository.getAsccByManifestId(asbie.getBasedAsccManifestId());
            if (asccRecord == null) {
                throw new IllegalArgumentException();
            }

            if (asbie.getCardinalityMin() == null) {
                asbieRecord.setCardinalityMin(asccRecord.getCardinalityMin());
            } else {
                asbieRecord.setCardinalityMin(asbie.getCardinalityMin());
            }

            if (asbie.getCardinalityMax() == null) {
                asbieRecord.setCardinalityMax(asccRecord.getCardinalityMax());
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
            asbieRecord.setCreationTimestamp(request.getLocalDateTime());
            asbieRecord.setLastUpdateTimestamp(request.getLocalDateTime());

            asbieRecord.setAsbieId(
                    dslContext.insertInto(ASBIE)
                            .set(asbieRecord)
                            .returning(ASBIE.ASBIE_ID)
                            .fetchOne().getAsbieId()
            );
        } else {
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
                asbieRecord.setLastUpdateTimestamp(request.getLocalDateTime());
                asbieRecord.update(
                        ASBIE.SEQ_KEY,
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

        return asbieReadRepository.getAsbie(request.getTopLevelAsbiepId(), hashPath);
    }

}
