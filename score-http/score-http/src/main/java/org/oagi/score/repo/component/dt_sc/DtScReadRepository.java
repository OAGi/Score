package org.oagi.score.repo.component.dt_sc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

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

    public List<String> getCdtPrimitivesByManifestId(ULong dtScManifestId) {
        return dslContext.selectDistinct(CDT_PRI.NAME)
                .from(DT_SC_MANIFEST)
                .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(BDT_SC_PRI_RESTRI).on(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID))
                .join(CDT_SC_AWD_PRI_XPS_TYPE_MAP).on(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID))
                .join(CDT_SC_AWD_PRI).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID))
                .join(CDT_PRI).on(CDT_SC_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(dtScManifestId))
                .fetchInto(String.class);
    }
}
