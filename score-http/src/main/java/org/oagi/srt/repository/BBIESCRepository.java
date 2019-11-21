package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record16;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BBIESC;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.impl.DSL.and;

@Repository
public class BBIESCRepository implements SrtRepository<BBIESC> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record16<
            ULong, String, ULong, ULong, ULong,
            ULong, ULong, Integer, Integer, String,
            String, String, String, String, Byte,
            ULong>> getSelectJoinStep() {
        return dslContext.select(
                Tables.BBIE_SC.BBIE_SC_ID,
                Tables.BBIE_SC.GUID,
                Tables.BBIE_SC.BBIE_ID,
                Tables.BBIE_SC.DT_SC_ID,
                Tables.BBIE_SC.DT_SC_PRI_RESTRI_ID,
                Tables.BBIE_SC.CODE_LIST_ID,
                Tables.BBIE_SC.AGENCY_ID_LIST_ID,
                Tables.BBIE_SC.CARDINALITY_MIN,
                Tables.BBIE_SC.CARDINALITY_MAX,
                Tables.BBIE_SC.DEFAULT_VALUE,
                Tables.BBIE_SC.FIXED_VALUE,
                Tables.BBIE_SC.DEFINITION,
                Tables.BBIE_SC.REMARK,
                Tables.BBIE_SC.BIZ_TERM,
                Tables.BBIE_SC.IS_USED.as("used"),
                Tables.BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID)
                .from(Tables.BBIE_SC);
    }

    @Override
    public List<BBIESC> findAll() {
        return getSelectJoinStep().fetchInto(BBIESC.class);
    }

    @Override
    public BBIESC findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BBIESC.class).orElse(null);
    }

    public List<BBIESC> findByOwnerTopLevelAbieIdAndUsed(long ownerTopLevelAbieId, boolean used) {
        return getSelectJoinStep()
                .where(and(Tables.BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)),
                        Tables.BBIE_SC.IS_USED.eq((byte) (used ? 1 : 0))))
                .fetchInto(BBIESC.class);
    }

}
