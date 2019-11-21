package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record20;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.Xbt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static org.oagi.srt.entity.jooq.Tables.XBT;

@Repository
public class XbtRepository implements SrtRepository<Xbt> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record20<
            ULong, ULong, ULong, ULong, ULong,
            String, ULong, ULong, ULong, String,
            Timestamp, Byte, String, Timestamp, Byte,
            String, Integer, Integer, String, Integer>> getSelectJoinStep() {
        return dslContext.select(XBT.XBT_ID, XBT.CREATED_BY, XBT.CURRENT_XBT_ID,
                XBT.LAST_UPDATED_BY, XBT.MODULE_ID, XBT.NAME, XBT.OWNER_USER_ID,
                XBT.RELEASE_ID, XBT.SUBTYPE_OF_XBT_ID, XBT.BUILTIN_TYPE,
                XBT.CREATION_TIMESTAMP, XBT.IS_DEPRECATED, XBT.JBT_DRAFT05_MAP,
                XBT.LAST_UPDATE_TIMESTAMP, XBT.REVISION_ACTION, XBT.REVISION_DOC,
                XBT.REVISION_NUM, XBT.REVISION_TRACKING_NUM, XBT.SCHEMA_DEFINITION,
                XBT.STATE)
                .from(XBT);
    }

    @Override
    public List<Xbt> findAll() {
        return getSelectJoinStep()
                .fetchInto(Xbt.class);
    }

    @Override
    public Xbt findById(long id) {
        return getSelectJoinStep()
                .where(XBT.XBT_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(Xbt.class);
    }

}
