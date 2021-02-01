package org.oagi.score.repo.component.bccp;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BCCP;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BCCP_MANIFEST;

@Repository
public class BccpReadRepository {

    @Autowired
    private DSLContext dslContext;

    public BccpRecord getBccpByManifestId(BigInteger bccpManifestId) {
        return dslContext.select(BCCP.fields())
                .from(BCCP)
                .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOptionalInto(BccpRecord.class).orElse(null);
    }

    public BccpManifestRecord getBccpManifestByManifestId(BigInteger bccpManifestId) {
        return dslContext.select(BCCP_MANIFEST.fields())
                .from(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOptionalInto(BccpManifestRecord.class).orElse(null);
    }
    
}
