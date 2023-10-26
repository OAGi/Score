package org.oagi.score.repo.component.bbie_sc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BbieScRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;
import org.oagi.score.repo.component.bdt_sc_pri_restri.AvailableBdtScPriRestri;
import org.oagi.score.repo.component.bdt_sc_pri_restri.BdtScPriRestriReadRepository;
import org.oagi.score.repo.component.dt_sc.DtScReadRepository;
import org.oagi.score.service.common.data.AppUser;
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

        AppUser user = sessionService.getAppUserByUsername(request.getUser());
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

            if (bbieSc.getUsed() != null) {
                bbieScRecord.setIsUsed((byte) (bbieSc.getUsed() ? 1 : 0));
            }

            if (bbieSc.getDeprecated() != null) {
                bbieScRecord.setIsDeprecated((byte) (bbieSc.getDeprecated() ? 1 : 0));
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

            if (bbieSc.getFacetMinLength() != null) {
                bbieScRecord.setFacetMinLength(ULong.valueOf(bbieSc.getFacetMinLength()));
            } else {
                bbieScRecord.setFacetMinLength(null);
            }
            if (bbieSc.getFacetMaxLength() != null) {
                bbieScRecord.setFacetMaxLength(ULong.valueOf(bbieSc.getFacetMaxLength()));
            } else {
                bbieScRecord.setFacetMaxLength(null);
            }
            if (bbieScRecord.getFacetMinLength() != null && bbieScRecord.getFacetMaxLength() != null) {
                if (bbieScRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieScRecord.getFacetMinLength().compareTo(bbieScRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbieSc.getFacetPattern())) {
                bbieScRecord.setFacetPattern(bbieSc.getFacetPattern());
            } else {
                bbieScRecord.setFacetPattern(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMinInclusive())) {
                bbieScRecord.setFacetMinInclusive(bbieSc.getFacetMinInclusive());
            } else {
                bbieScRecord.setFacetMinInclusive(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMinExclusive())) {
                bbieScRecord.setFacetMinExclusive(bbieSc.getFacetMinExclusive());
            } else {
                bbieScRecord.setFacetMinExclusive(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMaxInclusive())) {
                bbieScRecord.setFacetMaxInclusive(bbieSc.getFacetMaxInclusive());
            } else {
                bbieScRecord.setFacetMaxInclusive(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMaxExclusive())) {
                bbieScRecord.setFacetMaxExclusive(bbieSc.getFacetMaxExclusive());
            } else {
                bbieScRecord.setFacetMaxExclusive(null);
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

                AvailableBdtScPriRestri defaultBdtScPriRestri = bdtScPriRestriList.get(0);
                if (defaultBdtScPriRestri.getCodeListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(ULong.valueOf(defaultBdtScPriRestri.getCodeListManifestId()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (defaultBdtScPriRestri.getAgencyIdListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(ULong.valueOf(defaultBdtScPriRestri.getAgencyIdListManifestId()));
                } else {
                    bbieScRecord.setDtScPriRestriId(ULong.valueOf(defaultBdtScPriRestri.getBdtScPriRestriId()));
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(null);
                }
            } else {
                if (bbieSc.getBdtScPriRestriId() != null) {
                    bbieScRecord.setDtScPriRestriId(ULong.valueOf(bbieSc.getBdtScPriRestriId()));
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getCodeListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(ULong.valueOf(bbieSc.getCodeListManifestId()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getAgencyIdListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(ULong.valueOf(bbieSc.getAgencyIdListManifestId()));
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

            if (bbieSc.getDeprecated() != null) {
                bbieScRecord.setIsDeprecated((byte) (bbieSc.getDeprecated() ? 1 : 0));
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

            if (bbieSc.getFacetMinLength() != null) {
                bbieScRecord.setFacetMinLength(ULong.valueOf(bbieSc.getFacetMinLength()));
            } else {
                bbieScRecord.setFacetMinLength(null);
            }
            if (bbieSc.getFacetMaxLength() != null) {
                bbieScRecord.setFacetMaxLength(ULong.valueOf(bbieSc.getFacetMaxLength()));
            } else {
                bbieScRecord.setFacetMaxLength(null);
            }
            if (bbieScRecord.getFacetMinLength() != null && bbieScRecord.getFacetMaxLength() != null) {
                if (bbieScRecord.getFacetMinLength().intValue() < 0) {
                    throw new IllegalArgumentException("Minimum Length must be greater than or equals to 0.");
                }
                if (bbieScRecord.getFacetMinLength().compareTo(bbieScRecord.getFacetMaxLength()) > 0) {
                    throw new IllegalArgumentException("Minimum Length must be less than equals to Maximum Length.");
                }
            }
            if (StringUtils.hasLength(bbieSc.getFacetPattern())) {
                bbieScRecord.setFacetPattern(bbieSc.getFacetPattern());
            } else {
                bbieScRecord.setFacetPattern(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMinInclusive())) {
                bbieScRecord.setFacetMinInclusive(bbieSc.getFacetMinInclusive());
            } else {
                bbieScRecord.setFacetMinInclusive(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMinExclusive())) {
                bbieScRecord.setFacetMinExclusive(bbieSc.getFacetMinExclusive());
            } else {
                bbieScRecord.setFacetMinExclusive(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMaxInclusive())) {
                bbieScRecord.setFacetMaxInclusive(bbieSc.getFacetMaxInclusive());
            } else {
                bbieScRecord.setFacetMaxInclusive(null);
            }
            if (StringUtils.hasLength(bbieSc.getFacetMaxExclusive())) {
                bbieScRecord.setFacetMaxExclusive(bbieSc.getFacetMaxExclusive());
            } else {
                bbieScRecord.setFacetMaxExclusive(null);
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
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getCodeListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(ULong.valueOf(bbieSc.getCodeListManifestId()));
                    bbieScRecord.setAgencyIdListManifestId(null);
                } else if (bbieSc.getAgencyIdListManifestId() != null) {
                    bbieScRecord.setDtScPriRestriId(null);
                    bbieScRecord.setCodeListManifestId(null);
                    bbieScRecord.setAgencyIdListManifestId(ULong.valueOf(bbieSc.getAgencyIdListManifestId()));
                }
            }

            if (bbieScRecord.changed()) {
                bbieScRecord.setLastUpdatedBy(requesterId);
                bbieScRecord.setLastUpdateTimestamp(request.getLocalDateTime());

                bbieScRecord.update(
                        BBIE_SC.IS_USED,
                        BBIE_SC.IS_DEPRECATED,
                        BBIE_SC.DEFINITION,
                        BBIE_SC.CARDINALITY_MIN,
                        BBIE_SC.CARDINALITY_MAX,
                        BBIE_SC.FACET_MIN_LENGTH,
                        BBIE_SC.FACET_MAX_LENGTH,
                        BBIE_SC.FACET_PATTERN,
                        BBIE_SC.FACET_MIN_INCLUSIVE,
                        BBIE_SC.FACET_MIN_EXCLUSIVE,
                        BBIE_SC.FACET_MAX_INCLUSIVE,
                        BBIE_SC.FACET_MAX_EXCLUSIVE,
                        BBIE_SC.BIZ_TERM,
                        BBIE_SC.EXAMPLE,
                        BBIE_SC.REMARK,
                        BBIE_SC.DEFAULT_VALUE,
                        BBIE_SC.FIXED_VALUE,
                        BBIE_SC.DT_SC_PRI_RESTRI_ID,
                        BBIE_SC.CODE_LIST_MANIFEST_ID,
                        BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                        BBIE_SC.LAST_UPDATED_BY,
                        BBIE_SC.LAST_UPDATE_TIMESTAMP
                );
            }
        }

        return bbieScReadRepository.getBbieSc(request.getTopLevelAsbiepId(), hashPath);
    }
}
