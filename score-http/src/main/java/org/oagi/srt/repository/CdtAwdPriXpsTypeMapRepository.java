package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.CdtAwdPriXpsTypeMap;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CdtAwdPriXpsTypeMapRepository implements SrtRepository<CdtAwdPriXpsTypeMap> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<CdtAwdPriXpsTypeMap> findAll() {
        return dslContext.select(Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                Tables.CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID, Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID)
                .from(Tables.CDT_AWD_PRI_XPS_TYPE_MAP).fetchInto(CdtAwdPriXpsTypeMap.class);
    }

    @Override
    public CdtAwdPriXpsTypeMap findById(long id) {
        return dslContext.select(Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                Tables.CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID, Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID)
                .from(Tables.CDT_AWD_PRI_XPS_TYPE_MAP)
                .where(Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CdtAwdPriXpsTypeMap.class);
    }

}
