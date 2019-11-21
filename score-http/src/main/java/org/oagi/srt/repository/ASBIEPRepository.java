package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.ASBIEP;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ASBIEPRepository implements SrtRepository<ASBIEP> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<ASBIEP> findAll() {
        return dslContext.select(Tables.ASBIEP.BASED_ASCCP_ID, Tables.ASBIEP.ASBIEP_ID, Tables.ASBIEP.ROLE_OF_ABIE_ID,
                Tables.ASBIEP.OWNER_TOP_LEVEL_ABIE_ID, Tables.ASBIEP.LAST_UPDATED_BY, Tables.ASBIEP.BIZ_TERM,
                Tables.ASBIEP.CREATED_BY, Tables.ASBIEP.REMARK, Tables.ASBIEP.GUID, Tables.ASBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.ASBIEP.CREATION_TIMESTAMP, Tables.ASBIEP.DEFINITION).from(Tables.ASBIEP).fetchInto(ASBIEP.class);
    }

    @Override
    public ASBIEP findById(long id) {
        return dslContext.select(Tables.ASBIEP.BASED_ASCCP_ID, Tables.ASBIEP.ASBIEP_ID, Tables.ASBIEP.ROLE_OF_ABIE_ID,
                Tables.ASBIEP.OWNER_TOP_LEVEL_ABIE_ID, Tables.ASBIEP.LAST_UPDATED_BY, Tables.ASBIEP.BIZ_TERM,
                Tables.ASBIEP.CREATED_BY, Tables.ASBIEP.REMARK, Tables.ASBIEP.GUID, Tables.ASBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.ASBIEP.CREATION_TIMESTAMP, Tables.ASBIEP.DEFINITION).from(Tables.ASBIEP)
                .where(Tables.ASBIEP.ASBIEP_ID.eq(ULong.valueOf(id))).fetchOneInto(ASBIEP.class);
    }

    public List<ASBIEP> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(Tables.ASBIEP.BASED_ASCCP_ID, Tables.ASBIEP.ASBIEP_ID, Tables.ASBIEP.ROLE_OF_ABIE_ID,
                Tables.ASBIEP.OWNER_TOP_LEVEL_ABIE_ID, Tables.ASBIEP.LAST_UPDATED_BY, Tables.ASBIEP.BIZ_TERM,
                Tables.ASBIEP.CREATED_BY, Tables.ASBIEP.REMARK, Tables.ASBIEP.GUID, Tables.ASBIEP.LAST_UPDATE_TIMESTAMP,
                Tables.ASBIEP.CREATION_TIMESTAMP, Tables.ASBIEP.DEFINITION).from(Tables.ASBIEP)
                .where(Tables.ASBIEP.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(ASBIEP.class);
    }

}
