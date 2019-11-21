package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BCC;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BCCRepository implements SrtRepository<BCC> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record> getSelectJoinStep() {
        return dslContext.select(
                Tables.BCC.BCC_ID,
                Tables.BCC.GUID,
                Tables.BCC.CARDINALITY_MIN,
                Tables.BCC.CARDINALITY_MAX,
                Tables.BCC.SEQ_KEY,
                Tables.BCC.ENTITY_TYPE,
                Tables.BCC.FROM_ACC_ID,
                Tables.BCC.TO_BCCP_ID,
                Tables.BCC.DEN,
                Tables.BCC.DEFINITION,
                Tables.BCC.DEFINITION_SOURCE,
                Tables.BCC.DEFAULT_VALUE,
                Tables.BCC.CREATED_BY,
                Tables.BCC.OWNER_USER_ID,
                Tables.BCC.LAST_UPDATED_BY,
                Tables.BCC.CREATION_TIMESTAMP,
                Tables.BCC.LAST_UPDATE_TIMESTAMP,
                Tables.BCC.STATE,
                Tables.BCC.REVISION_NUM,
                Tables.BCC.REVISION_TRACKING_NUM,
                Tables.BCC.REVISION_ACTION,
                Tables.BCC.RELEASE_ID,
                Tables.BCC.CURRENT_BCC_ID,
                Tables.BCC.IS_DEPRECATED.as("deprecated"),
                Tables.BCC.IS_NILLABLE.as("nillable")
        ).from(Tables.BCC);
    }

    @Override
    public List<BCC> findAll() {
        return getSelectJoinStep().fetchInto(BCC.class);
    }

    @Override
    public BCC findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BCC.class).orElse(null);
    }
}
