package org.oagi.score.repo.component.abie;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AbieRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.component.acc.AccReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ABIE;

@Repository
public class AbieReadRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private AccReadRepository accReadRepository;

    private AbieRecord getAbieByTopLevelAsbiepIdAndHashPath(BigInteger topLevelAsbiepId, String hashPath) {
        return dslContext.selectFrom(ABIE)
                .where(and(
                        ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        ABIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);
    }

    public AbieNode getAbieNode(BigInteger topLevelAsbiepId, BigInteger accManifestId, String hashPath) {
        AccRecord accRecord = accReadRepository.getAccByManifestId(accManifestId);
        if (accRecord == null) {
            return null;
        }

        AbieNode abieNode = new AbieNode();

        AbieNode.Acc acc = abieNode.getAcc();
        acc.setAccManifestId(accManifestId);
        acc.setGuid(accRecord.getGuid());
        acc.setObjectClassTerm(accRecord.getObjectClassTerm());
        acc.setDen(accRecord.getObjectClassTerm() + ". Details");
        acc.setDefinition(accRecord.getDefinition());
        acc.setState(CcState.valueOf(accRecord.getState()));

        AbieNode.Abie abie = getAbie(topLevelAsbiepId, hashPath);
        abieNode.setAbie(abie);

        if (abie.getAbieId() == null) {
            abie.setBasedAccManifestId(acc.getAccManifestId());
        }

        return abieNode;
    }

    public AbieNode.Abie getAbie(BigInteger topLevelAsbiepId, String hashPath) {
        AbieNode.Abie abie = new AbieNode.Abie();
        abie.setUsed(true);
        abie.setHashPath(hashPath);

        AbieRecord abieRecord = getAbieByTopLevelAsbiepIdAndHashPath(topLevelAsbiepId, hashPath);
        if (abieRecord != null) {
            abie.setAbieId(abieRecord.getAbieId().toBigInteger());
            abie.setGuid(abieRecord.getGuid());
            abie.setBasedAccManifestId(abieRecord.getBasedAccManifestId().toBigInteger());
            abie.setRemark(abieRecord.getRemark());
            abie.setBizTerm(abieRecord.getBizTerm());
            abie.setDefinition(abieRecord.getDefinition());
        }

        return abie;
    }
}
