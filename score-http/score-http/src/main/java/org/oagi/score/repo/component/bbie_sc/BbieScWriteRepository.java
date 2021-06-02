package org.oagi.score.repo.component.bbie_sc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BbieScRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;
import org.oagi.score.repo.component.bdt_sc_pri_restri.AvailableBdtScPriRestri;
import org.oagi.score.repo.component.bdt_sc_pri_restri.BdtScPriRestriReadRepository;
import org.oagi.score.repo.component.dt_sc.DtScReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.helper.Utility.emptyToNull;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BBIE;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BBIE_SC;

@Repository
public class BbieScWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DtScReadRepository dtScReadRepository;

    @Autowired
    private BdtScPriRestriReadRepository bdtScPriRestriReadRepository;

    @Autowired
    private BbieScReadRepository bbieScReadRepository;

    public BbieScNode.BbieSc upsertBbieSc(UpsertBbieScRequest request) {
        BbieScNode.BbieSc bbieSc = request.getBbieSc();
        ULong topLevelAsbiepId = ULong.valueOf(request.getTopLevelAsbiepId());
        String hashPath = bbieSc.getHashPath();
        BbieScRecord bbieScRecord = dslContext.selectFrom(BBIE_SC)
                .where(and(
                        BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        BBIE_SC.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        AppUser user = sessionService.getAppUser(request.getUser());
        ULong requesterId = ULong.valueOf(user.getAppUserId());

        if (bbieScRecord == null) {
            bbieScRecord = new BbieScRecord();
            bbieScRecord.setGuid(ScoreGuid.randomGuid());
            bbieScRecord.setBasedDtScManifestId(ULong.valueOf(bbieSc.getBasedDtScManifestId()));
            bbieScRecord.setPath(bbieSc.getPath());
            bbieScRecord.setHashPath(hashPath);
            bbieScRecord.setBbieId(dslContext.select(BBIE.BBIE_ID)
                    .from(BBIE)
                    .where(and(
                            BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            BBIE.HASH_PATH.eq(bbieSc.getBbieHashPath())
                    ))
                    .fetchOneInto(ULong.class));

            if (bbieSc.getUsed() != null){
                bbieScRecord.setIsUsed((byte) (bbieSc.getUsed() ? 1 : 0));
            }
            
            bbieScRecord.setDefinition(bbieSc.getDefinition());
            DtScRecord dtScRecord = dtScReadRepository.getDtScByManifestId(bbieSc.getBasedDtScManifestId());
            if (dtScRecord == null) {
                throw new IllegalArgumentException();
            }
            if (bbieSc.getCardinalityMin() == null) {
                bbieScRecord.setCardinalityMin(dtScRecord.getCardinalityMin());
            } else {
                bbieScRecord.setCardinalityMin(bbieSc.getCardinalityMin());
            }

            if (bbieSc.getCardinalityMax() == null) {
                bbieScRecord.setCardinalityMax(dtScRecord.getCardinalityMax());
            } else {
                bbieScRecord.setCardinalityMax(bbieSc.getCardinalityMax());
            }
            if (bbieScRecord.getCardinalityMax() > 0 && bbieScRecord.getCardinalityMin() > bbieScRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }
            bbieScRecord.setBizTerm(bbieSc.getBizTerm());
            bbieScRecord.setExample(bbieSc.getExample());
            bbieScRecord.setRemark(bbieSc.getRemark());

            if (StringUtils.hasLength(bbieSc.getDefaultValue())) {
                bbieScRecord.setDefaultValue(bbieSc.getDefaultValue());
                bbieScRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbieSc.getFixedValue())) {
                bbieScRecord.setDefaultValue(null);
                bbieScRecord.setFixedValue(bbieSc.getFixedValue());
            }

            if (bbieSc.isEmptyPrimitive()) {
                List<AvailableBdtScPriRestri> bdtScPriRestriList =
                        bdtScPriRestriReadRepository.availableBdtScPriRestriListByBdtScManifestId(bbieSc.getBasedDtScManifestId());
                bdtScPriRestriList = bdtScPriRestriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (bdtScPriRestriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                bbieScRecord.setDtScPriRestriId(ULong.valueOf(bdtScPriRestriList.get(0).getBdtScPriRestriId()));
            } else {
                if (bbieSc.getBdtScPriRestriId() != null) {
                    bbieScRecord.setDtScPriRestriId(ULong.valueOf(bbieSc.getBdtScPriRestriId()));
                    bbieScRecord.setCodeListId(null);
                    bbieScRecord.setAgencyIdListId(null);
                } else if (bbieSc.getCodeListId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListId(ULong.valueOf(bbieSc.getCodeListId()));
                    bbieScRecord.setAgencyIdListId(null);
                } else if (bbieSc.getAgencyIdListId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListId(null);
                    bbieScRecord.setAgencyIdListId(ULong.valueOf(bbieSc.getAgencyIdListId()));
                }
            }

            bbieScRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            bbieScRecord.setCreatedBy(requesterId);
            bbieScRecord.setLastUpdatedBy(requesterId);
            bbieScRecord.setCreationTimestamp(request.getLocalDateTime());
            bbieScRecord.setLastUpdateTimestamp(request.getLocalDateTime());

            bbieScRecord.setBbieScId(
                    dslContext.insertInto(BBIE_SC)
                            .set(bbieScRecord)
                            .returning(BBIE_SC.BBIE_SC_ID)
                            .fetchOne().getBbieScId()
            );
        } else {
            if (bbieSc.getUsed() != null) {
                bbieScRecord.setIsUsed((byte) (bbieSc.getUsed() ? 1 : 0));
            }

            if (bbieSc.getDefinition() != null) {
                bbieScRecord.setDefinition(emptyToNull(bbieSc.getDefinition()));
            }

            if (bbieSc.getCardinalityMin() != null) {
                bbieScRecord.setCardinalityMin(bbieSc.getCardinalityMin());
            }

            if (bbieSc.getCardinalityMax() != null) {
                bbieScRecord.setCardinalityMax(bbieSc.getCardinalityMax());
            }

            if (bbieScRecord.getCardinalityMax() > 0 && bbieScRecord.getCardinalityMin() > bbieScRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (bbieSc.getBizTerm() != null) {
                bbieScRecord.setBizTerm(emptyToNull(bbieSc.getBizTerm()));
            }

            if (bbieSc.getExample() != null) {
                bbieScRecord.setExample(emptyToNull(bbieSc.getExample()));
            }

            if (bbieSc.getRemark() != null) {
                bbieScRecord.setRemark(emptyToNull(bbieSc.getRemark()));
            }

            if (StringUtils.hasLength(bbieSc.getDefaultValue())) {
                bbieScRecord.setDefaultValue(bbieSc.getDefaultValue());
                bbieScRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbieSc.getFixedValue())) {
                bbieScRecord.setDefaultValue(null);
                bbieScRecord.setFixedValue(bbieSc.getFixedValue());
            }

            if (!bbieSc.isEmptyPrimitive()) {
                if (bbieSc.getBdtScPriRestriId() != null) {
                    bbieScRecord.setDtScPriRestriId(ULong.valueOf(bbieSc.getBdtScPriRestriId()));
                    bbieScRecord.setCodeListId(null);
                    bbieScRecord.setAgencyIdListId(null);
                } else if (bbieSc.getCodeListId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListId(ULong.valueOf(bbieSc.getCodeListId()));
                    bbieScRecord.setAgencyIdListId(null);
                } else if (bbieSc.getAgencyIdListId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListId(null);
                    bbieScRecord.setAgencyIdListId(ULong.valueOf(bbieSc.getAgencyIdListId()));
                }
            }

            if (bbieScRecord.changed()) {
                bbieScRecord.setLastUpdatedBy(requesterId);
                bbieScRecord.setLastUpdateTimestamp(request.getLocalDateTime());

                bbieScRecord.update(
                        BBIE_SC.IS_USED,
                        BBIE_SC.DEFINITION,
                        BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.BIZ_TERM,
                        BBIE_SC.EXAMPLE,
                        BBIE_SC.REMARK,
                        BBIE_SC.DEFAULT_VALUE,
                        BBIE_SC.FIXED_VALUE,
                        BBIE_SC.DT_SC_PRI_RESTRI_ID,
                        BBIE_SC.CODE_LIST_ID,
                        BBIE_SC.AGENCY_ID_LIST_ID,
                        BBIE_SC.LAST_UPDATED_BY,
                        BBIE_SC.LAST_UPDATE_TIMESTAMP
                );
            }
        }

        return bbieScReadRepository.getBbieSc(request.getTopLevelAsbiepId(), hashPath);
    }
}
