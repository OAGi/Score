
package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record7;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BizCtx;
import org.oagi.srt.data.TopLevelAbie;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BizCtxRepository implements SrtRepository<BizCtx> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record7<
            ULong, String, String, ULong,
            ULong, Timestamp, Timestamp>> getSelectBizCtx() {
        return dslContext.select(
                Tables.BIZ_CTX.BIZ_CTX_ID,
                Tables.BIZ_CTX.GUID,
                Tables.BIZ_CTX.NAME,
                Tables.BIZ_CTX.CREATED_BY,
                Tables.BIZ_CTX.LAST_UPDATED_BY,
                Tables.BIZ_CTX.CREATION_TIMESTAMP,
                Tables.BIZ_CTX.LAST_UPDATE_TIMESTAMP)
                .from(Tables.BIZ_CTX);
    }

    @Override
    public List<BizCtx> findAll() {
        return getSelectBizCtx()
                .fetchInto(BizCtx.class);
    }

    @Override
    public BizCtx findById(long id) {
        if (id <= 0L) {
            return null;
        }

        return getSelectBizCtx()
                .where(Tables.BIZ_CTX.BIZ_CTX_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(BizCtx.class);
    }

    public List<BizCtx> findByTopLevelAbie(TopLevelAbie topLevelAbie) {
        List<Long> bizCtxIds = dslContext.select(
                Tables.BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                .from(Tables.BIZ_CTX_ASSIGNMENT)
                .where(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbie.getTopLevelAbieId())))
                .fetchInto(Long.class);

        return bizCtxIds.stream().map(bizCtxId -> findById(bizCtxId)).collect(Collectors.toList());
    }

}