package org.oagi.score.repo.component.dt_sc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.DT_SC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.DT_SC_MANIFEST;

@Repository
public class DtScReadRepository {

    @Autowired
    private DSLContext dslContext;

    public DtScRecord getDtScByManifestId(BigInteger dtScManifestId) {
        return dslContext.select(DT_SC.fields())
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(dtScManifestId)))
                .fetchOptionalInto(DtScRecord.class).orElse(null);
    }
}
