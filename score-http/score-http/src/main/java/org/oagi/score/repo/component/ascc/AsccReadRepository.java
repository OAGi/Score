package org.oagi.score.repo.component.ascc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ASCC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ASCC_MANIFEST;

@Repository
public class AsccReadRepository {

    @Autowired
    private DSLContext dslContext;

    public AsccRecord getAsccByManifestId(BigInteger asccManifestId) {
        return dslContext.select(ASCC.fields())
                .from(ASCC)
                .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                .fetchOptionalInto(AsccRecord.class).orElse(null);
    }

    public AsccManifestRecord getAsccManifestById(BigInteger asccManifestId) {
        return dslContext.select(ASCC_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                .fetchOptionalInto(AsccManifestRecord.class).orElse(null);
    }

    public AsccManifestRecord getUserExtensionAsccManifestByAsccpManifestId(BigInteger asccpManifestId) {
        return dslContext.select(ASCC_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptionalInto(AsccManifestRecord.class).orElse(null);
    }
}
