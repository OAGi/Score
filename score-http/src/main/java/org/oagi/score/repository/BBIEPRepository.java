package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.BBIEP;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BBIEPRepository implements SrtRepository<BBIEP> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<BBIEP> findAll() {
        return dslContext.select(Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.GUID,
                Tables.BBIEP.BASED_BCCP_ID,
                Tables.BBIEP.DEFINITION,
                Tables.BBIEP.REMARK,
                Tables.BBIEP.BIZ_TERM,
                Tables.BBIEP.CREATED_BY,
                Tables.BBIEP.CREATION_TIMESTAMP,
                Tables.BBIEP.LAST_UPDATED_BY,
                Tables.BBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(Tables.BBIEP).fetchInto(BBIEP.class);
    }

    @Override
    public BBIEP findById(long id) {
        return dslContext.select(Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.GUID,
                Tables.BBIEP.BASED_BCCP_ID,
                Tables.BBIEP.DEFINITION,
                Tables.BBIEP.REMARK,
                Tables.BBIEP.BIZ_TERM,
                Tables.BBIEP.CREATED_BY,
                Tables.BBIEP.CREATION_TIMESTAMP,
                Tables.BBIEP.LAST_UPDATED_BY,
                Tables.BBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(Tables.BBIEP).where(Tables.BBIEP.BBIEP_ID.eq(ULong.valueOf(id))).fetchOneInto(BBIEP.class);
    }

    public List<BBIEP> findByOwnerTopLevelAsbiepId(long ownerTopLevelAsbiepId) {
        if (ownerTopLevelAsbiepId <= 0L) {
            return Collections.emptyList();
        }
        return findByOwnerTopLevelAsbiepIds(Arrays.asList(ownerTopLevelAsbiepId));
    }

    public List<BBIEP> findByOwnerTopLevelAsbiepIds(List<Long> ownerTopLevelAsbiepIds) {
        return dslContext.select(Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.GUID,
                Tables.BBIEP.BASED_BCCP_ID,
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
                                        ULong.valueOf(ownerTopLevelAsbiepIds.get(0))) :
                                Tables.BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(
                                        ownerTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                )
                .fetchInto(BBIEP.class);
    }

}
