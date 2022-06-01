package org.oagi.score.repo.component.bbie_sc;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditUsed;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BbieScRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;
import org.oagi.score.repo.component.dt_sc.DtScReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BbieScReadRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private DtScReadRepository dtScReadRepository;

    private BbieScRecord getBbieScByTopLevelAsbiepIdAndHashPath(BigInteger topLevelAsbiepId, String hashPath) {
        return dslContext.selectFrom(BBIE_SC)
                .where(and(
                        BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        BBIE_SC.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);
    }

    public BbieScNode getBbieScNode(BigInteger topLevelAsbiepId, BigInteger dtScManifestId, String hashPath) {
        DtScRecord dtScRecord = dtScReadRepository.getDtScByManifestId(dtScManifestId);
        if (dtScRecord == null) {
            return null;
        }

        BbieScNode bbieScNode = new BbieScNode();

        BbieScNode.BdtSc bdtSc = bbieScNode.getBdtSc();
        bdtSc.setDtScManifestId(dtScManifestId);
        bdtSc.setGuid(dtScRecord.getGuid());
        bdtSc.setCardinalityMin(dtScRecord.getCardinalityMin());
        bdtSc.setCardinalityMax(dtScRecord.getCardinalityMax());
        bdtSc.setPropertyTerm(dtScRecord.getPropertyTerm());
        bdtSc.setRepresentationTerm(dtScRecord.getRepresentationTerm());
        bdtSc.setDefinition(dtScRecord.getDefinition());
        bdtSc.setDefaultValue(dtScRecord.getDefaultValue());
        bdtSc.setFixedValue(dtScRecord.getFixedValue());
        bdtSc.setState(CcState.valueOf(
                dslContext.select(DT.STATE)
                        .from(DT)
                        .where(DT.DT_ID.eq(dtScRecord.getOwnerDtId()))
                        .fetchOneInto(String.class)));

        BbieScNode.BbieSc bbieSc = getBbieSc(topLevelAsbiepId, hashPath);
        bbieScNode.setBbieSc(bbieSc);

        if (bbieSc.getBbieScId() == null) {
            bbieSc.setBasedDtScManifestId(bdtSc.getDtScManifestId());
            bbieSc.setCardinalityMin(dtScRecord.getCardinalityMin());
            bbieSc.setCardinalityMax(dtScRecord.getCardinalityMax());
            bbieSc.setDefaultValue(dtScRecord.getDefaultValue());
            bbieSc.setFixedValue(dtScRecord.getFixedValue());
            bbieSc.setBdtScPriRestriId(getDefaultDtScPriRestriIdByDtScId(dtScManifestId));
        }

        return bbieScNode;
    }

    public BigInteger getDefaultDtScPriRestriIdByDtScId(BigInteger dtScManifestId) {
        ULong bdtScManifestId = ULong.valueOf(dtScManifestId);
        String bdtScRepresentationTerm = dslContext.select(DT_SC.REPRESENTATION_TERM)
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(bdtScManifestId))
                .fetchOneInto(String.class);

        /*
         * Issue #808
         */
        List<Condition> conds = new ArrayList();
        conds.add(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(bdtScManifestId));
        if ("Date Time".equals(bdtScRepresentationTerm)) {
            conds.add(XBT.NAME.eq("date time"));
        } else if ("Date".equals(bdtScRepresentationTerm)) {
            conds.add(XBT.NAME.eq("date"));
        } else if ("Time".equals(bdtScRepresentationTerm)) {
            conds.add(XBT.NAME.eq("time"));
        } else {
            conds.add(BDT_SC_PRI_RESTRI.IS_DEFAULT.eq((byte) 1));
        }

        SelectOnConditionStep<Record1<ULong>> step = dslContext.select(
                BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID)
                .from(BDT_SC_PRI_RESTRI)
                .join(DT_SC).on(BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(CDT_SC_AWD_PRI_XPS_TYPE_MAP).on(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID))
                .join(XBT).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID));
        return step.where(conds)
                .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
    }

    public BbieScNode.BbieSc getBbieSc(BigInteger topLevelAsbiepId, String hashPath) {
        BbieScNode.BbieSc bbieSc = new BbieScNode.BbieSc();
        bbieSc.setHashPath(hashPath);

        BbieScRecord bbieScRecord = getBbieScByTopLevelAsbiepIdAndHashPath(topLevelAsbiepId, hashPath);
        if (bbieScRecord != null) {
            bbieSc.setBbieScId(bbieScRecord.getBbieScId().toBigInteger());
            bbieSc.setBbieHashPath(dslContext.select(BBIE.HASH_PATH)
                    .from(BBIE)
                    .where(and(
                            BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                            BBIE.BBIE_ID.eq(bbieScRecord.getBbieId())
                    ))
                    .fetchOneInto(String.class));
            bbieSc.setBasedDtScManifestId(bbieScRecord.getBasedDtScManifestId().toBigInteger());
            bbieSc.setUsed(bbieScRecord.getIsUsed() == 1);
            bbieSc.setGuid(bbieScRecord.getGuid());
            bbieSc.setCardinalityMin(bbieScRecord.getCardinalityMin());
            bbieSc.setCardinalityMax(bbieScRecord.getCardinalityMax());
            bbieSc.setRemark(bbieScRecord.getRemark());
            bbieSc.setBizTerm(bbieScRecord.getBizTerm());
            bbieSc.setDefinition(bbieScRecord.getDefinition());
            bbieSc.setDefaultValue(bbieScRecord.getDefaultValue());
            bbieSc.setFixedValue(bbieScRecord.getFixedValue());
            bbieSc.setExample(bbieScRecord.getExample());

            bbieSc.setBdtScPriRestriId((bbieScRecord.getDtScPriRestriId() != null) ?
                    bbieScRecord.getDtScPriRestriId().toBigInteger() : null);
            bbieSc.setCodeListId((bbieScRecord.getCodeListId() != null) ?
                    bbieScRecord.getCodeListId().toBigInteger() : null);
            bbieSc.setAgencyIdListId((bbieScRecord.getAgencyIdListId() != null) ?
                    bbieScRecord.getAgencyIdListId().toBigInteger() : null);
        }

        return bbieSc;
    }

    public List<BieEditUsed> getUsedBbieScList(BigInteger topLevelAsbiepId) {
        return dslContext.select(BBIE_SC.BBIE_SC_ID, BBIE_SC.BASED_DT_SC_MANIFEST_ID, BBIE_SC.HASH_PATH, BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(BBIE_SC)
                .where(and(
                        BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        BBIE_SC.IS_USED.eq((byte) 1)
                ))
                .fetchStream().map(record -> {
                    BieEditUsed bieEditUsed = new BieEditUsed();
                    bieEditUsed.setType("BBIE_SC");
                    bieEditUsed.setBieId(record.get(BBIE_SC.BBIE_SC_ID).toBigInteger());
                    bieEditUsed.setManifestId(record.get(BBIE_SC.BASED_DT_SC_MANIFEST_ID).toBigInteger());
                    bieEditUsed.setHashPath(record.get(BBIE_SC.HASH_PATH));
                    bieEditUsed.setOwnerTopLevelAsbiepId(record.get(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
                    return bieEditUsed;
                })
                .collect(Collectors.toList());
    }
}
