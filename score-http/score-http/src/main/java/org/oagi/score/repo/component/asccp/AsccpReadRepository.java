package org.oagi.score.repo.component.asccp;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcASCCPType;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class AsccpReadRepository {
    
    @Autowired
    private DSLContext dslContext;

    public AsccpRecord getAsccpByManifestId(BigInteger asccpManifestId) {
        return dslContext.select(ASCCP.fields())
                .from(ASCCP)
                .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptionalInto(AsccpRecord.class).orElse(null);
    }

    public AsccpManifestRecord getAsccpManifestById(BigInteger asccpManifestId) {
        return dslContext.select(ASCCP_MANIFEST.fields())
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptionalInto(AsccpManifestRecord.class).orElse(null);
    }

    public AsccpManifestRecord getExtensionAsccpManifestByAccManifestId(BigInteger accManifestId) {
        return dslContext.select(ASCCP_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(and(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)),
                        ASCCP.TYPE.eq(CcASCCPType.Extension.name())))
                .fetchOptionalInto(AsccpManifestRecord.class).orElse(null);
    }

    public AsccpManifestRecord getUserExtensionAsccpManifestByAccManifestId(BigInteger accManifestId) {
        return dslContext.select(ASCCP_MANIFEST.fields())
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(AsccpManifestRecord.class).orElse(null);
    }
}
