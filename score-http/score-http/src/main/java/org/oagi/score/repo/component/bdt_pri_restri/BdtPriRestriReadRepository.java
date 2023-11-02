package org.oagi.score.repo.component.bdt_pri_restri;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class BdtPriRestriReadRepository {

    @Autowired
    private DSLContext dslContext;

    private Function<Record, AvailableBdtPriRestri> mapper() {
        return e -> {
            AvailableBdtPriRestri availableBdtPriRestri = new AvailableBdtPriRestri();
            availableBdtPriRestri.setBdtPriRestriId(e.get(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID).toBigInteger());
            boolean isDefault = e.get(BDT_PRI_RESTRI.IS_DEFAULT) == (byte) 1;
            /*
             * Issue #808
             */
            String dataTypeTerm = e.get(DT.DATA_TYPE_TERM);
            String xbtName = e.get(XBT.NAME);
            if ("Date Time".equals(dataTypeTerm)) {
                isDefault = "date time".equalsIgnoreCase(xbtName);
            } else if ("Date".equals(dataTypeTerm)) {
                isDefault = "date".equalsIgnoreCase(xbtName);
            } else if ("Time".equals(dataTypeTerm)) {
                isDefault = "time".equalsIgnoreCase(xbtName);
            }
            availableBdtPriRestri.setDefault(isDefault);
            if (e.get(XBT.XBT_ID) != null) {
                availableBdtPriRestri.setXbtId(e.get(XBT.XBT_ID).toBigInteger());
            }
            if (xbtName != null) {
                availableBdtPriRestri.setXbtName(xbtName);
            }
            if (e.get(BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID) != null) {
                availableBdtPriRestri.setCodeListManifestId(e.get(BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID).toBigInteger());
            }
            if (e.get(BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID) != null) {
                availableBdtPriRestri.setAgencyIdListManifestId(e.get(BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
            }
            return availableBdtPriRestri;
        };
    }

    public List<AvailableBdtPriRestri> availableBdtPriRestriListByBccManifestId(BigInteger bccManifestId) {
        return dslContext.select(
                BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID, DT.DATA_TYPE_TERM,
                BDT_PRI_RESTRI.IS_DEFAULT, XBT.XBT_ID, XBT.NAME,
                BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID, BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID)
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST)
                .on(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(BCCP_MANIFEST)
                .on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .join(DT)
                .on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(BCC_MANIFEST)
                .on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .leftJoin(CDT_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                .fetchStream().map(mapper())
                .collect(Collectors.toList());
    }

    public List<AvailableBdtPriRestri> availableBdtPriRestriListByBccpManifestId(BigInteger bccpManifestId) {
        return dslContext.select(
                BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID, DT.DATA_TYPE_TERM,
                BDT_PRI_RESTRI.IS_DEFAULT, XBT.XBT_ID, XBT.NAME,
                BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID, BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID)
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST)
                .on(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(DT)
                .on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(BCCP_MANIFEST)
                .on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .leftJoin(CDT_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchStream().map(mapper())
                .collect(Collectors.toList());
    }

}
