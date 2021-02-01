package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.CdtAwdPriXpsTypeMap;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class CdtAwdPriXpsTypeMapRepository implements ScoreRepository<CdtAwdPriXpsTypeMap> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<CdtAwdPriXpsTypeMap> findAll() {
        return dslContext.select(Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                Tables.CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID, Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID)
                .from(Tables.CDT_AWD_PRI_XPS_TYPE_MAP).fetchInto(CdtAwdPriXpsTypeMap.class);
    }

    @Override
    public CdtAwdPriXpsTypeMap findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                Tables.CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID, Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID)
                .from(Tables.CDT_AWD_PRI_XPS_TYPE_MAP)
                .where(Tables.CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CdtAwdPriXpsTypeMap.class);
    }

}
