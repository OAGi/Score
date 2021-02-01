package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.BBIEP;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BBIEPRepository implements ScoreRepository<BBIEP> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<BBIEP> findAll() {
        return dslContext.select(Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.GUID,
                Tables.BBIEP.BASED_BCCP_MANIFEST_ID,
                Tables.BBIEP.DEFINITION,
                Tables.BBIEP.REMARK,
                Tables.BBIEP.BIZ_TERM,
                Tables.BBIEP.CREATED_BY,
                Tables.BBIEP.CREATION_TIMESTAMP,
                Tables.BBIEP.LAST_UPDATED_BY,
                Tables.BBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(Tables.BBIEP)
                .fetchInto(BBIEP.class);
    }

    @Override
    public BBIEP findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.GUID,
                Tables.BBIEP.BASED_BCCP_MANIFEST_ID,
                Tables.BBIEP.DEFINITION,
                Tables.BBIEP.REMARK,
                Tables.BBIEP.BIZ_TERM,
                Tables.BBIEP.CREATED_BY,
                Tables.BBIEP.CREATION_TIMESTAMP,
                Tables.BBIEP.LAST_UPDATED_BY,
                Tables.BBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(Tables.BBIEP)
                .where(Tables.BBIEP.BBIEP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(BBIEP.class);
    }

    public List<BBIEP> findByOwnerTopLevelAsbiepIds(Collection<BigInteger> ownerTopLevelAsbiepIds) {
        return dslContext.select(Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.GUID,
                Tables.BBIEP.BASED_BCCP_MANIFEST_ID,
                Tables.BBIEP.DEFINITION,
                Tables.BBIEP.REMARK,
                Tables.BBIEP.BIZ_TERM,
                Tables.BBIEP.CREATED_BY,
                Tables.BBIEP.CREATION_TIMESTAMP,
                Tables.BBIEP.LAST_UPDATED_BY,
                Tables.BBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(Tables.BBIEP)
                .where(
                        (ownerTopLevelAsbiepIds.size() == 1) ?
                                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(
                                        ULong.valueOf(ownerTopLevelAsbiepIds.iterator().next())) :
                                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(
                                        ownerTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                )
                .fetchInto(BBIEP.class);
    }

}
