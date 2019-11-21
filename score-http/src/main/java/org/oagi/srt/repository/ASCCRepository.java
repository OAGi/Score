package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record22;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.ASCC;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class ASCCRepository implements SrtRepository<ASCC> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record22<
            ULong, String, Integer, Integer, Integer,
            ULong, ULong, String, String, String,
            ULong, ULong, ULong, Timestamp, Timestamp,
            Integer, Integer, Integer, Byte, ULong,
            ULong, Byte>> getSelectJoinStep() {
        return dslContext.select(
                Tables.ASCC.ASCC_ID,
                Tables.ASCC.GUID,
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.DEN,
                Tables.ASCC.DEFINITION,
                Tables.ASCC.DEFINITION_SOURCE,
                Tables.ASCC.CREATED_BY,
                Tables.ASCC.OWNER_USER_ID,
                Tables.ASCC.LAST_UPDATED_BY,
                Tables.ASCC.CREATION_TIMESTAMP,
                Tables.ASCC.LAST_UPDATE_TIMESTAMP,
                Tables.ASCC.STATE,
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.REVISION_ACTION,
                Tables.ASCC.RELEASE_ID,
                Tables.ASCC.CURRENT_ASCC_ID,
                Tables.ASCC.IS_DEPRECATED.as("deprecated")
        ).from(Tables.ASCC);
    }

    @Override
    public List<ASCC> findAll() {
        return getSelectJoinStep().fetchInto(ASCC.class);
    }

    @Override
    public ASCC findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.ASCC.ASCC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(ASCC.class).orElse(null);
    }
}
