package org.oagi.score.repo.component.bbie;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditUsed;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.bcc.BccReadRepository;
import org.oagi.score.repo.component.bccp.BccpReadRepository;
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
        BccManifestRecord bccManifestRecord = bccReadRepository.getBccManifestByManifestId(bccManifestId);
        BccRecord bccRecord = bccReadRepository.getBccByManifestId(bccManifestId);
        if (bccRecord == null) {
            return null;
        }
        BccpManifestRecord bccpManifestRecord = bccpReadRepository.getBccpManifestByManifestId(
                bccManifestRecord.getToBccpManifestId().toBigInteger());
        BccpRecord bccpRecord = bccpReadRepository.getBccpByManifestId(
                bccpManifestRecord.getBccpManifestId().toBigInteger());

        BbieNode bbieNode = new BbieNode();

        BbieNode.Bcc bcc = bbieNode.getBcc();
        bcc.setBccManifestId(bccManifestId);
        bcc.setGuid(bccRecord.getGuid());
        bcc.setCardinalityMin(bccRecord.getCardinalityMin());
        bcc.setCardinalityMax(bccRecord.getCardinalityMax());
        bcc.setDen(bccRecord.getDen());
        bcc.setDefinition(bccRecord.getDefinition());
        bcc.setState(CcState.valueOf(bccRecord.getState()));
        bcc.setDefaultValue(bccRecord.getDefaultValue());
        bcc.setFixedValue(bccRecord.getFixedValue());
        bcc.setDeprecated(bccRecord.getIsDeprecated() == 1);
        bcc.setNillable(bccRecord.getIsNillable() == 1);

        BbieNode.Bbie bbie = getBbie(topLevelAsbiepId, hashPath);
        bbieNode.setBbie(bbie);

        if (bbie.getBbieId() == null) {
            bbie.setBasedBccManifestId(bcc.getBccManifestId());
            bbie.setCardinalityMin(bccRecord.getCardinalityMin());
            bbie.setCardinalityMax(bccRecord.getCardinalityMax());
            bbie.setDefaultValue(bccRecord.getDefaultValue());
            bbie.setFixedValue(bccRecord.getFixedValue());
            bbie.setNillable(bccpRecord.getIsNillable() == 1);
            BigInteger defaultBdtPriRestriId = getDefaultBdtPriRestriIdByBdtId(
                    bccpManifestRecord.getBdtManifestId().toBigInteger());
            bbie.setBdtPriRestriId(defaultBdtPriRestriId);
        }

        return bbieNode;
    }

    public BigInteger getDefaultBdtPriRestriIdByBdtId(BigInteger bdtManifestId) {
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
                .join(DT).on(BDT_PRI_RESTRI.BDT_ID.eq(DT.DT_ID))
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(CDT_AWD_PRI_XPS_TYPE_MAP).on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .join(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID));
        return step.where(conds)
                .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
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
            bbie.setNillable(bbieRecord.getIsNillable() == 1);
            bbie.setRemark(bbieRecord.getRemark());
            bbie.setDefinition(bbieRecord.getDefinition());
            bbie.setDefaultValue(bbieRecord.getDefaultValue());
            bbie.setFixedValue(bbieRecord.getFixedValue());
            bbie.setExample(bbieRecord.getExample());

            bbie.setBdtPriRestriId((bbieRecord.getBdtPriRestriId() != null) ?
                    bbieRecord.getBdtPriRestriId().toBigInteger() : null);
            bbie.setCodeListId((bbieRecord.getCodeListId() != null) ?
                    bbieRecord.getCodeListId().toBigInteger() : null);
            bbie.setAgencyIdListId((bbieRecord.getAgencyIdListId() != null) ?
                    bbieRecord.getAgencyIdListId().toBigInteger() : null);
        }

        return bbie;
    }

    public List<BieEditUsed> getUsedBbieList(BigInteger topLevelAsbiepId) {
        return dslContext.select(BBIE.BBIE_ID, BBIE.BASED_BCC_MANIFEST_ID, BBIE.HASH_PATH)
                .from(BBIE)
                .join(BBIEP).on(and(
                        BBIE.TO_BBIEP_ID.eq(BBIEP.BBIEP_ID),
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .where(and(
                        BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        BBIE.IS_USED.eq((byte) 1)
                ))
                .fetchStream().map(record -> {
                    BieEditUsed bieEditUsed = new BieEditUsed();
                    bieEditUsed.setType("BBIE");
                    bieEditUsed.setBieId(record.get(BBIE.BBIE_ID).toBigInteger());
                    bieEditUsed.setManifestId(record.get(BBIE.BASED_BCC_MANIFEST_ID).toBigInteger());
                    bieEditUsed.setHashPath(record.get(BBIE.HASH_PATH));
                    return bieEditUsed;
                })
                .collect(Collectors.toList());
    }
}
