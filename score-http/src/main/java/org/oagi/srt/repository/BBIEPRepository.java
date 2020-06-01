package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.BBIEP;
import org.oagi.srt.entity.jooq.Tables;
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
                Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID)
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
                Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID)
                .from(Tables.BBIEP).where(Tables.BBIEP.BBIEP_ID.eq(ULong.valueOf(id))).fetchOneInto(BBIEP.class);
    }

    public List<BBIEP> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        if (ownerTopLevelAbieId <= 0L) {
            return Collections.emptyList();
        }
        return findByOwnerTopLevelAbieIds(Arrays.asList(ownerTopLevelAbieId));
    }

    public List<BBIEP> findByOwnerTopLevelAbieIds(List<Long> ownerTopLevelAbieIds) {
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
                Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID)
                .from(Tables.BBIEP)
                .where(
                        (ownerTopLevelAbieIds.size() == 1) ?
                                Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID.eq(
                                        ULong.valueOf(ownerTopLevelAbieIds.get(0))) :
                                Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID.in(
                                        ownerTopLevelAbieIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                )
                .fetchInto(BBIEP.class);
    }

}
