package org.oagi.score.repo.component.bcc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BCC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BCC_MANIFEST;

@Repository
public class BccReadRepository {

    @Autowired
    private DSLContext dslContext;

    public BccRecord getBccByManifestId(BigInteger bccManifestId) {
        return dslContext.select(BCC.fields())
                .from(BCC)
                .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                .fetchOptionalInto(BccRecord.class).orElse(null);
    }

    public BccManifestRecord getBccManifestByManifestId(BigInteger bccManifestId) {
        return dslContext.select(BCC_MANIFEST.fields())
                .from(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                .fetchOptionalInto(BccManifestRecord.class).orElse(null);
    }
}
