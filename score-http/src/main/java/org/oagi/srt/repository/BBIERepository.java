package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BBIE;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.impl.DSL.and;

@Repository
public class BBIERepository implements SrtRepository<BBIE> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record> getSelectJoinStep() {
        return dslContext.select(Tables.BBIE.BBIE_ID,
                Tables.BBIE.GUID,
                Tables.BBIE.BASED_BCC_ID,
                Tables.BBIE.FROM_ABIE_ID,
                Tables.BBIE.TO_BBIEP_ID,
                Tables.BBIE.BDT_PRI_RESTRI_ID,
                Tables.BBIE.CODE_LIST_ID,
                Tables.BBIE.AGENCY_ID_LIST_ID,
                Tables.BBIE.CARDINALITY_MAX,
                Tables.BBIE.CARDINALITY_MIN,
                Tables.BBIE.DEFAULT_VALUE,
                Tables.BBIE.IS_NILLABLE.as("nillable"),
                Tables.BBIE.FIXED_VALUE,
                Tables.BBIE.IS_NULL.as("nill"),
                Tables.BBIE.DEFINITION,
                Tables.BBIE.REMARK,
                Tables.BBIE.CREATED_BY,
                Tables.BBIE.CREATION_TIMESTAMP,
                Tables.BBIE.LAST_UPDATED_BY,
                Tables.BBIE.LAST_UPDATE_TIMESTAMP,
                Tables.BBIE.SEQ_KEY,
                Tables.BBIE.IS_USED.as("used"),
                Tables.BBIE.OWNER_TOP_LEVEL_ABIE_ID)
                .from(Tables.BBIE);
    }

    @Override
    public List<BBIE> findAll() {
        return getSelectJoinStep().fetchInto(BBIE.class);
    }

    @Override
    public BBIE findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.BBIE.BBIE_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BBIE.class).orElse(null);
    }

    public List<BBIE> findByOwnerTopLevelAbieIdAndUsed(long ownerTopLevelAbieId, boolean used) {
        return getSelectJoinStep()
                .where(and(
                        Tables.BBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)),
                        Tables.BBIE.IS_USED.eq((byte) (used ? 1 : 0))))
                .fetchInto(BBIE.class);
    }

}
