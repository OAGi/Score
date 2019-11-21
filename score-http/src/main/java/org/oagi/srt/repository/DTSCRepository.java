package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record10;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.DTSC;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DTSCRepository implements SrtRepository<DTSC> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record10<
            ULong, String, String, String, String,
            String, ULong, Integer, Integer, ULong>> getSelectJoinStep() {
        return dslContext.select(
                Tables.DT_SC.DT_SC_ID,
                Tables.DT_SC.GUID,
                Tables.DT_SC.PROPERTY_TERM,
                Tables.DT_SC.REPRESENTATION_TERM,
                Tables.DT_SC.DEFINITION,
                Tables.DT_SC.DEFINITION_SOURCE,
                Tables.DT_SC.OWNER_DT_ID,
                Tables.DT_SC.CARDINALITY_MIN,
                Tables.DT_SC.CARDINALITY_MAX,
                Tables.DT_SC.BASED_DT_SC_ID
        ).from(Tables.DT_SC);
    }

    @Override
    public List<DTSC> findAll() {
        return getSelectJoinStep().fetchInto(DTSC.class);
    }

    @Override
    public DTSC findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.DT_SC.DT_SC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(DTSC.class).orElse(null);
    }
}
