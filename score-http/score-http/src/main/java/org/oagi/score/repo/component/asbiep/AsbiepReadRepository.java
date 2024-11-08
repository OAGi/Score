package org.oagi.score.repo.component.asbiep;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsbiepRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;
import org.oagi.score.repo.component.asccp.AsccpReadRepository;
import org.oagi.score.service.common.data.CcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class AsbiepReadRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private AsccpReadRepository asccpReadRepository;

    private AsbiepRecord getAsbiepByTopLevelAsbiepIdAndHashPath(BigInteger topLevelAsbiepId, String hashPath) {
        return dslContext.selectFrom(ASBIEP)
                .where(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        ASBIEP.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);
    }

    public AsbiepNode getAsbiepNode(BigInteger topLevelAsbiepId, BigInteger asccpManifestId, String hashPath) {
        AsccpRecord asccpRecord = asccpReadRepository.getAsccpByManifestId(asccpManifestId);
        if (asccpRecord == null) {
            return null;
        }

        AsbiepNode asbiepNode = new AsbiepNode();

        AsbiepNode.Asccp asccp = asbiepNode.getAsccp();
        asccp.setAsccpManifestId(asccpManifestId);
        asccp.setGuid(asccpRecord.getGuid());
        asccp.setPropertyTerm(asccpRecord.getPropertyTerm());
        String den = dslContext.select(ASCCP_MANIFEST.DEN)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOneInto(String.class);
        asccp.setDen(den);
        asccp.setDefinition(asccpRecord.getDefinition());
        asccp.setState(CcState.valueOf(asccpRecord.getState()));
        asccp.setNillable(asccpRecord.getIsNillable() == 1);

        AsbiepNode.Asbiep asbiep = getAsbiep(topLevelAsbiepId, hashPath);
        asbiepNode.setAsbiep(asbiep);

        if (asbiep.getAsbiepId() == null) {
            asbiep.setBasedAsccpManifestId(asccp.getAsccpManifestId());
        }

        return asbiepNode;
    }

    public AsbiepNode.Asbiep getAsbiep(BigInteger topLevelAsbiepId, String hashPath) {
        AsbiepNode.Asbiep asbiep = new AsbiepNode.Asbiep();
        asbiep.setUsed(true);
        asbiep.setHashPath(hashPath);

        AsbiepRecord asbiepRecord = getAsbiepByTopLevelAsbiepIdAndHashPath(topLevelAsbiepId, hashPath);
        if (asbiepRecord != null) {

            asbiep.setAsbiepId(asbiepRecord.getAsbiepId().toBigInteger());
            if (asbiepRecord.getRoleOfAbieId() != null) {
                asbiep.setRoleOfAbieHashPath(dslContext.select(ABIE.HASH_PATH)
                        .from(ABIE)
                        .where(ABIE.ABIE_ID.eq(asbiepRecord.getRoleOfAbieId()))
                        .fetchOneInto(String.class));
            } else {
                asbiep.setRoleOfAbieHashPath(dslContext.select(ABIE.HASH_PATH)
                        .from(ABIE)
                        .where(and(
                                ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(asbiepRecord.getOwnerTopLevelAsbiepId()),
                                ABIE.ABIE_ID.eq(asbiepRecord.getRoleOfAbieId())
                        ))
                        .fetchOneInto(String.class));
            }
            asbiep.setBasedAsccpManifestId(asbiepRecord.getBasedAsccpManifestId().toBigInteger());
            asbiep.setDerived(false); // TODO
            asbiep.setGuid(asbiepRecord.getGuid());
            asbiep.setRemark(asbiepRecord.getRemark());
            asbiep.setBizTerm(asbiepRecord.getBizTerm());
            asbiep.setDefinition(asbiepRecord.getDefinition());
            asbiep.setDisplayName(asbiepRecord.getDisplayName());
        }

        return asbiep;
    }

    public AsbiepNode.Asbiep getAsbiepByTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        String asbiepHashPath = dslContext.select(ASBIEP.HASH_PATH)
                .from(ASBIEP)
                .join(TOP_LEVEL_ASBIEP).on(and(
                        ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID),
                        ASBIEP.ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.ASBIEP_ID)
                ))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .fetchOneInto(String.class);
        return getAsbiep(topLevelAsbiepId, asbiepHashPath);
    }

}
