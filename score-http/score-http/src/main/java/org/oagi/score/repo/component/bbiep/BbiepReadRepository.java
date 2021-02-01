package org.oagi.score.repo.component.bbiep;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BbiepRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.oagi.score.repo.component.bccp.BccpReadRepository;
import org.oagi.score.repo.component.dt.DtReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BBIEP;

@Repository
public class BbiepReadRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BccpReadRepository bccpReadRepository;

    @Autowired
    private DtReadRepository dtReadRepository;

    private BbiepRecord getBbiepByTopLevelAsbiepIdAndHashPath(BigInteger topLevelAsbiepId, String hashPath) {
        return dslContext.selectFrom(BBIEP)
                .where(and(
                        BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        BBIEP.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);
    }

    public BbiepNode getBbiepNode(BigInteger topLevelAsbiepId, BigInteger bccpManifestId, String hashPath) {
        BccpRecord bccpRecord = bccpReadRepository.getBccpByManifestId(bccpManifestId);
        if (bccpRecord == null) {
            return null;
        }

        BbiepNode bbiepNode = new BbiepNode();

        BbiepNode.Bccp bccp = bbiepNode.getBccp();
        bccp.setBccpManifestId(bccpManifestId);
        bccp.setGuid(bccpRecord.getGuid());
        bccp.setPropertyTerm(bccpRecord.getPropertyTerm());
        bccp.setDen(bccpRecord.getDen());
        bccp.setDefinition(bccpRecord.getDefinition());
        bccp.setState(CcState.valueOf(bccpRecord.getState()));
        bccp.setNillable(bccpRecord.getIsNillable() == 1);
        bccp.setDefaultValue(bccpRecord.getDefaultValue());
        bccp.setFixedValue(bccpRecord.getFixedValue());

        DtRecord bdtRecord = dtReadRepository.getDtByBccpManifestId(bccpManifestId);

        BbiepNode.Bdt bdt = bbiepNode.getBdt();
        bdt.setGuid(bdtRecord.getGuid());
        bdt.setDataTypeTerm(bdtRecord.getDataTypeTerm());
        bdt.setDen(bdtRecord.getDen());
        bdt.setDefinition(bdtRecord.getDefinition());
        bdt.setState(CcState.valueOf(bdtRecord.getState()));

        BbiepNode.Bbiep bbiep = getBbiep(topLevelAsbiepId, hashPath);
        bbiepNode.setBbiep(bbiep);

        if (bbiep.getBbiepId() == null) {
            bbiep.setBasedBccpManifestId(bccp.getBccpManifestId());
        }

        return bbiepNode;
    }

    public BbiepNode.Bbiep getBbiep(BigInteger topLevelAsbiepId, String hashPath) {
        BbiepNode.Bbiep bbiep = new BbiepNode.Bbiep();
        bbiep.setUsed(true);
        bbiep.setHashPath(hashPath);

        BbiepRecord bbiepRecord = getBbiepByTopLevelAsbiepIdAndHashPath(topLevelAsbiepId, hashPath);
        if (bbiepRecord != null) {
            bbiep.setBbiepId(bbiepRecord.getBbiepId().toBigInteger());
            bbiep.setBasedBccpManifestId(bbiepRecord.getBasedBccpManifestId().toBigInteger());
            bbiep.setGuid(bbiepRecord.getGuid());
            bbiep.setRemark(bbiepRecord.getRemark());
            bbiep.setBizTerm(bbiepRecord.getBizTerm());
            bbiep.setDefinition(bbiepRecord.getDefinition());
        }

        return bbiep;
    }
    
}
