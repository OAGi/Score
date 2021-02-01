package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.CdtScAwdPriXpsTypeMap;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class CdtScAwdPriXpsTypeMapRepository implements ScoreRepository<CdtScAwdPriXpsTypeMap> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<CdtScAwdPriXpsTypeMap> findAll() {
        return dslContext.select(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP).fetchInto(CdtScAwdPriXpsTypeMap.class);
    }

    @Override
    public CdtScAwdPriXpsTypeMap findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .where(Tables.CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(CdtScAwdPriXpsTypeMap.class);
    }

}
