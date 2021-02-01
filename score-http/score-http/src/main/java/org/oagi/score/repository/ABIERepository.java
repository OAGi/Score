package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record13;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.data.ABIE;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ABIERepository implements ScoreRepository<ABIE> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record13<
                ULong, ULong, ULong, ULong, LocalDateTime,
                Integer, ULong, String, ULong, String, String, String,
                LocalDateTime>> getSelectJoinStep() {
        return dslContext.select(Tables.ABIE.ABIE_ID,
                Tables.ABIE.BASED_ACC_MANIFEST_ID,
                Tables.ABIE.BIZ_CTX_ID,
                Tables.ABIE.OWNER_TOP_LEVEL_ASBIEP_ID,
                Tables.ABIE.LAST_UPDATE_TIMESTAMP,
                Tables.ABIE.STATE,
                Tables.ABIE.LAST_UPDATED_BY,
                Tables.ABIE.BIZ_TERM,
                Tables.ABIE.CREATED_BY,
                Tables.ABIE.DEFINITION,
                Tables.ABIE.GUID,
                Tables.ABIE.REMARK,
                Tables.ABIE.CREATION_TIMESTAMP)
                .from(Tables.ABIE);
    }

    @Override
    public List<ABIE> findAll() {
        return getSelectJoinStep().fetchInto(ABIE.class);
    }

    @Override
    public ABIE findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.ABIE.ABIE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(ABIE.class);
    }

    public List<ABIE> findByOwnerTopLevelAsbiepIds(Collection<BigInteger> ownerTopLevelAsbiepIds) {
        if (ownerTopLevelAsbiepIds == null || ownerTopLevelAsbiepIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getSelectJoinStep()
                .where(
                        (ownerTopLevelAsbiepIds.size() == 1) ?
                                Tables.ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(
                                        ULong.valueOf(ownerTopLevelAsbiepIds.iterator().next())) :
                                Tables.ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(
                                        ownerTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                )
                .fetchInto(ABIE.class);
    }

}
