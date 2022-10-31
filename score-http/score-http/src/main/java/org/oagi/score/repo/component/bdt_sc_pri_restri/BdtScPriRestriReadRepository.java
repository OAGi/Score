package org.oagi.score.repo.component.bdt_sc_pri_restri;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BdtScPriRestriReadRepository {

    @Autowired
    private DSLContext dslContext;

    private Function<Record, AvailableBdtScPriRestri> mapper() {
        return e -> {
            AvailableBdtScPriRestri availableBdtScPriRestri = new AvailableBdtScPriRestri();
            availableBdtScPriRestri.setBdtScPriRestriId(e.get(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID).toBigInteger());
            boolean isDefault = e.get(BDT_SC_PRI_RESTRI.IS_DEFAULT) == (byte) 1;
            /*
             * Issue #808
             */
            String representationTerm = e.get(DT_SC.REPRESENTATION_TERM);
            String xbtName = e.get(XBT.NAME);
            if ("Date Time".equals(representationTerm)) {
                isDefault = "date time".equalsIgnoreCase(xbtName);
            } else if ("Date".equals(representationTerm)) {
                isDefault = "date".equalsIgnoreCase(xbtName);
            } else if ("Time".equals(representationTerm)) {
                isDefault = "time".equalsIgnoreCase(xbtName);
            }
            availableBdtScPriRestri.setDefault(isDefault);
            availableBdtScPriRestri.setXbtId(e.get(XBT.XBT_ID).toBigInteger());
            availableBdtScPriRestri.setXbtName(xbtName);
            return availableBdtScPriRestri;
        };
    }

    public List<AvailableBdtScPriRestri> availableBdtScPriRestriListByBdtScManifestId(BigInteger bdtScManifestId) {
        return dslContext.select(
                BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID,
                BDT_SC_PRI_RESTRI.IS_DEFAULT,
                DT_SC.REPRESENTATION_TERM,
                XBT.XBT_ID,
                XBT.NAME)
                .from(BDT_SC_PRI_RESTRI)
                .join(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID
                        .eq(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID))
                .join(XBT).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .join(DT_SC_MANIFEST)
                .on(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.DT_SC_MANIFEST_ID))
                .join(DT_SC)
                .on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bdtScManifestId)))
                .fetchStream().map(mapper())
                .sorted(Comparator.comparing(AvailableBdtScPriRestri::getXbtName))
                .collect(Collectors.toList());
    }
}
