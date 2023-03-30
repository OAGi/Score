package org.oagi.score.repo.component.bbie;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BbieRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccRecord;
import org.oagi.score.repo.component.bcc.BccReadRepository;
import org.oagi.score.repo.component.bdt_pri_restri.AvailableBdtPriRestri;
import org.oagi.score.repo.component.bdt_pri_restri.BdtPriRestriReadRepository;
import org.oagi.score.service.common.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.helper.Utility.emptyToNull;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BbieWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BccReadRepository bccReadRepository;

    @Autowired
    private BdtPriRestriReadRepository bdtPriRestriReadRepository;

    @Autowired
    private BbieReadRepository bbieReadRepository;

    public BbieNode.Bbie upsertBbie(UpsertBbieRequest request) {
        BbieNode.Bbie bbie = request.getBbie();
        ULong topLevelAsbiepId = ULong.valueOf(request.getTopLevelAsbiepId());
        String hashPath = bbie.getHashPath();
        BbieRecord bbieRecord = dslContext.selectFrom(BBIE)
                .where(and(
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        BBIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong requesterId = ULong.valueOf(user.getAppUserId());

        if (bbieRecord == null) {
            bbieRecord = new BbieRecord();
            bbieRecord.setGuid(ScoreGuid.randomGuid());
            bbieRecord.setBasedBccManifestId(ULong.valueOf(bbie.getBasedBccManifestId()));
            bbieRecord.setPath(bbie.getPath());
            bbieRecord.setHashPath(hashPath);
            bbieRecord.setFromAbieId(dslContext.select(ABIE.ABIE_ID)
                    .from(ABIE)
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            ABIE.HASH_PATH.eq(bbie.getFromAbieHashPath())
                    ))
                    .fetchOneInto(ULong.class));
            bbieRecord.setToBbiepId(dslContext.select(BBIEP.BBIEP_ID)
                    .from(BBIEP)
                    .where(and(
                            BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            BBIEP.HASH_PATH.eq(bbie.getToBbiepHashPath())
                    ))
                    .fetchOneInto(ULong.class));
            bbieRecord.setSeqKey(BigDecimal.valueOf(bbie.getSeqKey().longValue()));

            if (bbie.getUsed() != null) {
                bbieRecord.setIsUsed((byte) (bbie.getUsed() ? 1 : 0));
            }

            if (bbie.getNillable() != null) {
                bbieRecord.setIsNillable((byte) (bbie.getNillable() ? 1 : 0));
            }

            bbieRecord.setDefinition(bbie.getDefinition());
            BccRecord bccRecord = bccReadRepository.getBccByManifestId(bbie.getBasedBccManifestId());
            if (bccRecord == null) {
                throw new IllegalArgumentException();
            }

            if (bbie.getCardinalityMin() == null) {
                bbieRecord.setCardinalityMin(bccRecord.getCardinalityMin());
            } else {
                bbieRecord.setCardinalityMin(bbie.getCardinalityMin());
            }

            if (bbie.getCardinalityMax() == null) {
                bbieRecord.setCardinalityMax(bccRecord.getCardinalityMax());
            } else {
                bbieRecord.setCardinalityMax(bbie.getCardinalityMax());
            }
            if (bbieRecord.getCardinalityMax() > 0 && bbieRecord.getCardinalityMin() > bbieRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (bbie.getMinLength() != null) {
                bbieRecord.setFacetMinLength(ULong.valueOf(bbie.getMinLength()));
            } else {
                bbieRecord.setFacetMinLength(null);
            }
            if (bbie.getMaxLength() != null) {
                bbieRecord.setFacetMaxLength(ULong.valueOf(bbie.getMaxLength()));
            } else {
                bbieRecord.setFacetMaxLength(null);
            }
            if (bbieRecord.getFacetMinLength() != null && bbieRecord.getFacetMaxLength() != null) {
                if (bbieRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieRecord.getFacetMinLength().compareTo(bbieRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbie.getPattern())) {
                bbieRecord.setFacetPattern(bbie.getPattern());
            } else {
                bbieRecord.setFacetPattern(null);
            }

            bbieRecord.setExample(bbie.getExample());
            bbieRecord.setRemark(bbie.getRemark());

            if (StringUtils.hasLength(bbie.getDefaultValue())) {
                bbieRecord.setDefaultValue(bbie.getDefaultValue());
                bbieRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbie.getFixedValue())) {
                bbieRecord.setDefaultValue(null);
                bbieRecord.setFixedValue(bbie.getFixedValue());
            }

            if (bbie.isEmptyPrimitive()) {
                List<AvailableBdtPriRestri> bdtPriRestriList =
                        bdtPriRestriReadRepository.availableBdtPriRestriListByBccManifestId(bbie.getBasedBccManifestId());
                bdtPriRestriList = bdtPriRestriList.stream().filter(e -> e.isDefault())
                        .collect(Collectors.toList());
                if (bdtPriRestriList.size() != 1) {
                    throw new IllegalArgumentException();
                }

                bbieRecord.setBdtPriRestriId(ULong.valueOf(bdtPriRestriList.get(0).getBdtPriRestriId()));
            } else {
                if (bbie.getBdtPriRestriId() != null) {
                    bbieRecord.setBdtPriRestriId(ULong.valueOf(bbie.getBdtPriRestriId()));
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getCodeListManifestId() != null) {
                    bbieRecord.setBdtPriRestriId(null);
                    bbieRecord.setCodeListManifestId(ULong.valueOf(bbie.getCodeListManifestId()));
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getAgencyIdListManifestId() != null) {
                    bbieRecord.setBdtPriRestriId(null);
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(ULong.valueOf(bbie.getAgencyIdListManifestId()));
                }
            }

            bbieRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            bbieRecord.setCreatedBy(requesterId);
            bbieRecord.setLastUpdatedBy(requesterId);
            bbieRecord.setCreationTimestamp(request.getLocalDateTime());
            bbieRecord.setLastUpdateTimestamp(request.getLocalDateTime());

            bbieRecord.setBbieId(
                    dslContext.insertInto(BBIE)
                            .set(bbieRecord)
                            .returning(BBIE.BBIE_ID)
                            .fetchOne().getBbieId()
            );
        } else {
            bbieRecord.setSeqKey(BigDecimal.valueOf(bbie.getSeqKey().longValue()));

            if (bbie.getUsed() != null) {
                bbieRecord.setIsUsed((byte) (bbie.getUsed() ? 1 : 0));
            }

            if (bbie.getNillable() != null) {
                bbieRecord.setIsNillable((byte) (bbie.getNillable() ? 1 : 0));
            }

            if (bbie.getDefinition() != null) {
                bbieRecord.setDefinition(emptyToNull(bbie.getDefinition()));
            }

            if (bbie.getCardinalityMin() != null) {
                bbieRecord.setCardinalityMin(bbie.getCardinalityMin());
            }

            if (bbie.getCardinalityMax() != null) {
                bbieRecord.setCardinalityMax(bbie.getCardinalityMax());
            }

            if (bbieRecord.getCardinalityMax() > 0 && bbieRecord.getCardinalityMin() > bbieRecord.getCardinalityMax()) {
                throw new IllegalArgumentException("Cardinality is not valid.");
            }

            if (bbie.getMinLength() != null) {
                bbieRecord.setFacetMinLength(ULong.valueOf(bbie.getMinLength()));
            } else {
                bbieRecord.setFacetMinLength(null);
            }
            if (bbie.getMaxLength() != null) {
                bbieRecord.setFacetMaxLength(ULong.valueOf(bbie.getMaxLength()));
            } else {
                bbieRecord.setFacetMaxLength(null);
            }
            if (bbieRecord.getFacetMinLength() != null && bbieRecord.getFacetMaxLength() != null) {
                if (bbieRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieRecord.getFacetMinLength().compareTo(bbieRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbie.getPattern())) {
                bbieRecord.setFacetPattern(bbie.getPattern());
            } else {
                bbieRecord.setFacetPattern(null);
            }

            if (bbie.getExample() != null) {
                bbieRecord.setExample(emptyToNull(bbie.getExample()));
            }

            if (bbie.getRemark() != null) {
                bbieRecord.setRemark(emptyToNull(bbie.getRemark()));
            }

            if (StringUtils.hasLength(bbie.getDefaultValue())) {
                bbieRecord.setDefaultValue(bbie.getDefaultValue());
                bbieRecord.setFixedValue(null);
            } else if (StringUtils.hasLength(bbie.getFixedValue())) {
                bbieRecord.setDefaultValue(null);
                bbieRecord.setFixedValue(bbie.getFixedValue());
            }

            if (!bbie.isEmptyPrimitive()) {
                if (bbie.getBdtPriRestriId() != null) {
                    bbieRecord.setBdtPriRestriId(ULong.valueOf(bbie.getBdtPriRestriId()));
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getCodeListManifestId() != null) {
                    bbieRecord.setBdtPriRestriId(null);
                    bbieRecord.setCodeListManifestId(ULong.valueOf(bbie.getCodeListManifestId()));
                    bbieRecord.setAgencyIdListManifestId(null);
                } else if (bbie.getAgencyIdListManifestId() != null) {
                    bbieRecord.setBdtPriRestriId(null);
                    bbieRecord.setCodeListManifestId(null);
                    bbieRecord.setAgencyIdListManifestId(ULong.valueOf(bbie.getAgencyIdListManifestId()));
                }
            }

            if (bbieRecord.changed()){
                bbieRecord.setLastUpdatedBy(requesterId);
                bbieRecord.setLastUpdateTimestamp(request.getLocalDateTime());
                bbieRecord.update(
                        BBIE.SEQ_KEY,
                        BBIE.IS_USED,
                        BBIE.IS_NILLABLE,
                        BBIE.DEFINITION,
                        BBIE.CARDINALITY_MIN,
                        BBIE.CARDINALITY_MAX,
                        BBIE.FACET_MIN_LENGTH,
                        BBIE.FACET_MAX_LENGTH,
                        BBIE.FACET_PATTERN,
                        BBIE.EXAMPLE,
                        BBIE.REMARK,
                        BBIE.DEFAULT_VALUE,
                        BBIE.FIXED_VALUE,
                        BBIE.BDT_PRI_RESTRI_ID,
                        BBIE.CODE_LIST_MANIFEST_ID,
                        BBIE.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE.LAST_UPDATED_BY,
                        BBIE.LAST_UPDATE_TIMESTAMP
                );
            }
        }
        return bbieReadRepository.getBbie(request.getTopLevelAsbiepId(), hashPath);
    }

}
