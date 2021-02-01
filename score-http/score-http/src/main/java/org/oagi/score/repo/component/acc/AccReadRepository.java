package org.oagi.score.repo.component.acc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcACCType;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ACC_MANIFEST;

@Repository
public class AccReadRepository {

    @Autowired
    private DSLContext dslContext;

    public AccRecord getAccByManifestId(BigInteger accManifestId) {
        return dslContext.select(ACC.fields())
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(AccRecord.class).orElse(null);
    }

    public AccManifestRecord getAllExtensionAccManifest(BigInteger releaseId) {
        return dslContext.select(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.TYPE.eq(CcACCType.AllExtension.name())))
                .fetchOptionalInto(AccManifestRecord.class).orElse(null);
    }

    public AccManifestRecord getAccManifest(BigInteger accManifestId) {
        return dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(AccManifestRecord.class).orElse(null);
    }
}
