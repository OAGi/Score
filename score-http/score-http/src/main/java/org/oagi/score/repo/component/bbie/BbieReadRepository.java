package org.oagi.score.repo.component.bbie;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.BdtPriRestri;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditUsed;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.bcc.BccReadRepository;
import org.oagi.score.repo.component.bccp.BccpReadRepository;
import org.oagi.score.service.common.data.CcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BbieReadRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BccReadRepository bccReadRepository;

    @Autowired
    private BccpReadRepository bccpReadRepository;

    private BbieRecord getBbieByTopLevelAsbiepIdAndHashPath(BigInteger topLevelAsbiepId, String hashPath) {
        return dslContext.selectFrom(BBIE)
                .where(and(
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        BBIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);
    }

    public BbieNode getBbieNode(BigInteger topLevelAsbiepId, BigInteger bccManifestId, String hashPath) {
        BccManifestRecord bccManifestRecord = bccReadRepository.getBccManifestById(bccManifestId);
        BccRecord bccRecord = bccReadRepository.getBccByManifestId(bccManifestId);
        if (bccRecord == null) {
            return null;
        }
        BccpManifestRecord bccpManifestRecord = bccpReadRepository.getBccpManifestByManifestId(
                bccManifestRecord.getToBccpManifestId().toBigInteger());
        BccpRecord bccpRecord = bccpReadRepository.getBccpByManifestId(
                bccpManifestRecord.getBccpManifestId().toBigInteger());
        List<String> cdtPrimitives = bccpReadRepository.getCdtPrimitivesByManifestId(
                bccpManifestRecord.getBccpManifestId());

        BbieNode bbieNode = new BbieNode();

        BbieNode.Bcc bcc = bbieNode.getBcc();
        bcc.setBccManifestId(bccManifestId);
        bcc.setGuid(bccRecord.getGuid());
        bcc.setCardinalityMin(bccRecord.getCardinalityMin());
        bcc.setCardinalityMax(bccRecord.getCardinalityMax());
        String den = dslContext.select(BCC_MANIFEST.DEN)
                .from(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                .fetchOneInto(String.class);
        bcc.setDen(den);
        bcc.setDefinition(bccRecord.getDefinition());
        bcc.setState(CcState.valueOf(bccRecord.getState()));
        if (bccRecord.getDefaultValue() != null || bccRecord.getFixedValue() != null) {
            bcc.setDefaultValue(bccRecord.getDefaultValue());
            bcc.setFixedValue(bccRecord.getFixedValue());
        } else if (bccpRecord.getDefaultValue() != null || bccpRecord.getFixedValue() != null) {
            bcc.setDefaultValue(bccpRecord.getDefaultValue());
            bcc.setFixedValue(bccpRecord.getFixedValue());
        }
        bcc.setDeprecated(bccRecord.getIsDeprecated() == 1);
        bcc.setNillable(bccRecord.getIsNillable() == 1);
        bcc.setCdtPrimitives(cdtPrimitives);

        BbieNode.Bbie bbie = getBbie(topLevelAsbiepId, hashPath);
        bbieNode.setBbie(bbie);

        if (bbie.getBbieId() == null) {
            bbie.setBasedBccManifestId(bcc.getBccManifestId());
            bbie.setCardinalityMin(bccRecord.getCardinalityMin());
            bbie.setCardinalityMax(bccRecord.getCardinalityMax());
            if (bccRecord.getDefaultValue() != null || bccRecord.getFixedValue() != null) {
                bbie.setDefaultValue(bccRecord.getDefaultValue());
                bbie.setFixedValue(bccRecord.getFixedValue());
            } else if (bccpRecord.getDefaultValue() != null || bccpRecord.getFixedValue() != null) {
                bbie.setDefaultValue(bccpRecord.getDefaultValue());
                bbie.setFixedValue(bccpRecord.getFixedValue());
            }
            bbie.setNillable(bccpRecord.getIsNillable() == 1);
            BdtPriRestri defaultBdtPriRestri = getDefaultBdtPriRestriByBdtManifestId(
                    bccpManifestRecord.getBdtManifestId().toBigInteger());
            if (defaultBdtPriRestri.getCodeListManifestId() != null) {
                bbie.setCodeListManifestId(defaultBdtPriRestri.getCodeListManifestId());
            } else if (defaultBdtPriRestri.getAgencyIdListManifestId() != null) {
                bbie.setAgencyIdListManifestId(defaultBdtPriRestri.getAgencyIdListManifestId());
            } else {
                BigInteger defaultBdtPriRestriId = getDefaultBdtPriRestriIdByBdtManifestId(
                        bccpManifestRecord.getBdtManifestId().toBigInteger());
                bbie.setBdtPriRestriId(defaultBdtPriRestriId);
            }
        }

        return bbieNode;
    }

    public BigInteger getDefaultBdtPriRestriIdByBdtManifestId(BigInteger bdtManifestId) {
        ULong dtManifestId = ULong.valueOf(bdtManifestId);
        String bdtDataTypeTerm = dslContext.select(DT.DATA_TYPE_TERM)
                .from(DT)
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestId))
                .fetchOneInto(String.class);

        /*
         * Issue #808
         */
        List<Condition> conds = new ArrayList();
        conds.add(DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestId));
        if ("Date Time".equals(bdtDataTypeTerm)) {
            conds.add(XBT.NAME.eq("date time"));
        } else if ("Date".equals(bdtDataTypeTerm)) {
            conds.add(XBT.NAME.eq("date"));
        } else if ("Time".equals(bdtDataTypeTerm)) {
            conds.add(XBT.NAME.eq("time"));
        } else {
            conds.add(BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) 1));
        }

        SelectOnConditionStep<Record1<ULong>> step = dslContext.select(
                BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID)
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST).on(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(DT).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(CDT_AWD_PRI_XPS_TYPE_MAP).on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .join(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID));
        return step.where(conds)
                .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
    }

    public BdtPriRestri getDefaultBdtPriRestriByBdtManifestId(BigInteger bdtManifestId) {
        ULong dtManifestId = ULong.valueOf(bdtManifestId);
        return dslContext.select(BDT_PRI_RESTRI.fields())
                .from(BDT_PRI_RESTRI)
                .where(and(
                        BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestId),
                        BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)
                ))
                .fetchOptionalInto(BdtPriRestri.class).orElse(null);
    }

    public BbieNode.Bbie getBbie(BigInteger topLevelAsbiepId, String hashPath) {
        BbieNode.Bbie bbie = new BbieNode.Bbie();
        bbie.setHashPath(hashPath);

        BbieRecord bbieRecord = getBbieByTopLevelAsbiepIdAndHashPath(topLevelAsbiepId, hashPath);
        if (bbieRecord != null) {
            bbie.setBbieId(bbieRecord.getBbieId().toBigInteger());
            bbie.setFromAbieHashPath(dslContext.select(ABIE.HASH_PATH)
                    .from(ABIE)
                    .where(and(
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                            ABIE.ABIE_ID.eq(bbieRecord.getFromAbieId())
                    ))
                    .fetchOneInto(String.class));
            bbie.setToBbiepHashPath(dslContext.select(BBIEP.HASH_PATH)
                    .from(BBIEP)
                    .where(and(
                            BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                            BBIEP.BBIEP_ID.eq(bbieRecord.getToBbiepId())
                    ))
                    .fetchOneInto(String.class));
            bbie.setBasedBccManifestId(bbieRecord.getBasedBccManifestId().toBigInteger());
            bbie.setUsed(bbieRecord.getIsUsed() == 1);
            bbie.setGuid(bbieRecord.getGuid());
            bbie.setCardinalityMin(bbieRecord.getCardinalityMin());
            bbie.setCardinalityMax(bbieRecord.getCardinalityMax());
            if (bbieRecord.getFacetMinLength() != null) {
                bbie.setFacetMinLength(bbieRecord.getFacetMinLength().toBigInteger());
            }
            if (bbieRecord.getFacetMaxLength() != null) {
                bbie.setFacetMaxLength(bbieRecord.getFacetMaxLength().toBigInteger());
            }
            bbie.setFacetPattern(bbieRecord.getFacetPattern());
            bbie.setNillable(bbieRecord.getIsNillable() == 1);
            bbie.setRemark(bbieRecord.getRemark());
            bbie.setDefinition(bbieRecord.getDefinition());
            bbie.setDefaultValue(bbieRecord.getDefaultValue());
            bbie.setFixedValue(bbieRecord.getFixedValue());
            bbie.setExample(bbieRecord.getExample());
            bbie.setDeprecated(bbieRecord.getIsDeprecated() == 1);

            bbie.setBdtPriRestriId((bbieRecord.getBdtPriRestriId() != null) ?
                    bbieRecord.getBdtPriRestriId().toBigInteger() : null);
            bbie.setCodeListManifestId((bbieRecord.getCodeListManifestId() != null) ?
                    bbieRecord.getCodeListManifestId().toBigInteger() : null);
            bbie.setAgencyIdListManifestId((bbieRecord.getAgencyIdListManifestId() != null) ?
                    bbieRecord.getAgencyIdListManifestId().toBigInteger() : null);
        }

        return bbie;
    }

    public List<BieEditUsed> getUsedBbieList(BigInteger topLevelAsbiepId) {
        return dslContext.select(BBIE.IS_USED, BBIE.BBIE_ID, BBIE.BASED_BCC_MANIFEST_ID,
                        BBIE.HASH_PATH, BBIE.OWNER_TOP_LEVEL_ASBIEP_ID,
                        BBIE.CARDINALITY_MIN, BBIE.CARDINALITY_MAX,
                        BBIE.IS_DEPRECATED)
                .from(BBIE)
                .join(BBIEP).on(and(
                        BBIE.TO_BBIEP_ID.eq(BBIEP.BBIEP_ID),
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .fetchStream().map(record -> {
                    BieEditUsed bieEditUsed = new BieEditUsed();
                    bieEditUsed.setUsed(record.get(BBIE.IS_USED) == 1);
                    bieEditUsed.setType("BBIE");
                    bieEditUsed.setBieId(record.get(BBIE.BBIE_ID).toBigInteger());
                    bieEditUsed.setManifestId(record.get(BBIE.BASED_BCC_MANIFEST_ID).toBigInteger());
                    bieEditUsed.setHashPath(record.get(BBIE.HASH_PATH));
                    bieEditUsed.setOwnerTopLevelAsbiepId(record.get(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
                    bieEditUsed.setCardinalityMin(record.get(BBIE.CARDINALITY_MIN));
                    bieEditUsed.setCardinalityMax(record.get(BBIE.CARDINALITY_MAX));
                    bieEditUsed.setDeprecated(record.get(BBIE.IS_DEPRECATED) == 1);
                    return bieEditUsed;
                })
                .collect(Collectors.toList());
    }
}
