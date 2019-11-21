package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BdtPriRestri;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BdtPriRestriRepository implements SrtRepository<BdtPriRestri> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record6<ULong, ULong, ULong, ULong, ULong, Byte>> getSelectJoinStep() {
        return dslContext.select(
                Tables.BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID,
                Tables.BDT_PRI_RESTRI.BDT_ID,
                Tables.BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                Tables.BDT_PRI_RESTRI.CODE_LIST_ID,
                Tables.BDT_PRI_RESTRI.AGENCY_ID_LIST_ID,
                Tables.BDT_PRI_RESTRI.IS_DEFAULT.as("defaulted")
        ).from(Tables.BDT_PRI_RESTRI);
    }

    @Override
    public List<BdtPriRestri> findAll() {
        return getSelectJoinStep().fetchInto(BdtPriRestri.class);
    }

    @Override
    public BdtPriRestri findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BdtPriRestri.class).orElse(null);
    }
}
