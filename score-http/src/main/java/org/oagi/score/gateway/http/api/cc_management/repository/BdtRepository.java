package org.oagi.score.gateway.http.api.cc_management.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.BdtPriRestri;
import org.oagi.score.data.BdtScPriRestri;
import org.oagi.score.data.DT;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.oagi.score.entity.jooq.Tables.BDT_PRI_RESTRI;
import static org.oagi.score.entity.jooq.Tables.BDT_SC_PRI_RESTRI;

@Repository
public class BdtRepository {

    @Autowired
    private DSLContext dslContext;

    public List<DT> findAll() {
        return dslContext.select(Tables.DT.fields())
                .from(Tables.DT)
                .fetchInto(DT.class);
    }

    public List<BdtPriRestri> getBdtPriRestriListByBdtId(long bdtId) {
        return dslContext.select(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID,
                BDT_PRI_RESTRI.BDT_ID,
                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_PRI_RESTRI.CODE_LIST_ID,
                BDT_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_PRI_RESTRI.IS_DEFAULT)
                .from(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_ID.eq(ULong.valueOf(bdtId)))
                .fetchInto(BdtPriRestri.class);
    }

    public List<BdtScPriRestri> getBdtScPriRestriListByBdtId(long bdtScId) {
        return dslContext.select(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID,
                BDT_SC_PRI_RESTRI.BDT_SC_ID,
                BDT_SC_PRI_RESTRI.IS_DEFAULT,
                BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_SC_PRI_RESTRI.CODE_LIST_ID,
                BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID)
                .from(BDT_SC_PRI_RESTRI)
                .where(BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(ULong.valueOf(bdtScId)))
                .fetchInto(BdtScPriRestri.class);
    }
}
