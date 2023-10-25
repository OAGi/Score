package org.oagi.score.repo.component.dt;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BdtReadRepository {

    @Autowired
    private DSLContext dslContext;

    public DtRecord getDtByBccpManifestId(BigInteger bccpManifestId) {
        return dslContext.select(DT.fields())
                .from(DT)
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(BCCP_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOptionalInto(DtRecord.class).orElse(null);
    }

    public DtRecord getDtByDtManifestId(BigInteger dtManifestId) {
        return dslContext.select(DT.fields())
                .from(DT)
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .fetchOptionalInto(DtRecord.class).orElse(null);
    }

    public BdtNode getBdtNode(BigInteger topLevelAsbiepId, BigInteger dtManifestId) {
        DtRecord dtRecord = getDtByDtManifestId(dtManifestId);
        if (dtRecord == null) {
            return null;
        }

        BdtNode bdtNode = new BdtNode();

        bdtNode.setDataTypeTerm(dtRecord.getDataTypeTerm());
        bdtNode.setQualifier(dtRecord.getQualifier_());
        if (dtRecord.getFacetMinLength() != null) {
            bdtNode.setFacetMinLength(dtRecord.getFacetMinLength().toBigInteger());
        }
        if (dtRecord.getFacetMaxLength() != null) {
            bdtNode.setFacetMaxLength(dtRecord.getFacetMaxLength().toBigInteger());
        }
        bdtNode.setFacetPattern(dtRecord.getFacetPattern());
        bdtNode.setFacetMinInclusive(dtRecord.getFacetMinInclusive());
        bdtNode.setFacetMinExclusive(dtRecord.getFacetMinExclusive());
        bdtNode.setFacetMaxInclusive(dtRecord.getFacetMaxInclusive());
        bdtNode.setFacetMaxExclusive(dtRecord.getFacetMaxExclusive());
        bdtNode.setDefinition(dtRecord.getDefinition());
        String den = dslContext.select(DT_MANIFEST.DEN)
                .from(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .fetchOneInto(String.class);
        bdtNode.setDen(den);
        bdtNode.setBdtManifestId(dtManifestId);

        return bdtNode;
    }
}
