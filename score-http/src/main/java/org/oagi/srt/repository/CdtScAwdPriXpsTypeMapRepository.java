package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.CdtScAwdPriXpsTypeMap;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CdtScAwdPriXpsTypeMapRepository implements SrtRepository<CdtScAwdPriXpsTypeMap> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<CdtScAwdPriXpsTypeMap> findAll() {
        return dslContext.select(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP).fetchInto(CdtScAwdPriXpsTypeMap.class);
    }

    @Override
    public CdtScAwdPriXpsTypeMap findById(long id) {
        return dslContext.select(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .where(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CdtScAwdPriXpsTypeMap.class);
    }

}
