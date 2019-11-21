package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BdtScPriRestri;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BdtScPriRestriRepository implements SrtRepository<BdtScPriRestri> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record6<ULong, ULong, ULong, ULong, ULong, Byte>> getSelectJoinStep() {
        return dslContext.select(
                Tables.BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID,
                Tables.BDT_SC_PRI_RESTRI.BDT_SC_ID,
                Tables.BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                Tables.BDT_SC_PRI_RESTRI.CODE_LIST_ID,
                Tables.BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID,
                Tables.BDT_SC_PRI_RESTRI.IS_DEFAULT.as("defaulted")
        ).from(Tables.BDT_SC_PRI_RESTRI);
    }

    @Override
    public List<BdtScPriRestri> findAll() {
        return getSelectJoinStep().fetchInto(BdtScPriRestri.class);
    }

    @Override
    public BdtScPriRestri findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BdtScPriRestri.class).orElse(null);
    }
}
