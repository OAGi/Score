package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.ASBIE;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ASBIERepository implements SrtRepository<ASBIE> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<ASBIE> findAll() {
        return dslContext.select(Tables.ASBIE.ASBIE_ID,
                Tables.ASBIE.GUID,
                Tables.ASBIE.FROM_ABIE_ID,
                Tables.ASBIE.TO_ASBIEP_ID,
                Tables.ASBIE.BASED_ASCC_ID,
                Tables.ASBIE.DEFINITION,
                Tables.ASBIE.CARDINALITY_MAX,
                Tables.ASBIE.CARDINALITY_MIN,
                Tables.ASBIE.IS_NILLABLE.as("nillable"),
                Tables.ASBIE.IS_USED.as("used"),
                Tables.ASBIE.REMARK,
                Tables.ASBIE.CREATED_BY,
                Tables.ASBIE.CREATION_TIMESTAMP,
                Tables.ASBIE.LAST_UPDATED_BY,
                Tables.ASBIE.LAST_UPDATE_TIMESTAMP,
                Tables.ASBIE.SEQ_KEY,
                Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID).from(Tables.ASBIE).fetchInto(ASBIE.class);
    }

    @Override
    public ASBIE findById(long id) {
        return dslContext.select(Tables.ASBIE.ASBIE_ID,
                Tables.ASBIE.GUID,
                Tables.ASBIE.FROM_ABIE_ID,
                Tables.ASBIE.TO_ASBIEP_ID,
                Tables.ASBIE.BASED_ASCC_ID,
                Tables.ASBIE.DEFINITION,
                Tables.ASBIE.CARDINALITY_MAX,
                Tables.ASBIE.CARDINALITY_MIN,
                Tables.ASBIE.IS_NILLABLE.as("nillable"),
                Tables.ASBIE.IS_USED.as("used"),
                Tables.ASBIE.REMARK,
                Tables.ASBIE.CREATED_BY,
                Tables.ASBIE.CREATION_TIMESTAMP,
                Tables.ASBIE.LAST_UPDATED_BY,
                Tables.ASBIE.LAST_UPDATE_TIMESTAMP,
                Tables.ASBIE.SEQ_KEY,
                Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID).from(Tables.ASBIE)
                .where(Tables.ASBIE.ASBIE_ID.eq(ULong.valueOf(id))).fetchOneInto(ASBIE.class);
    }

    public List<ASBIE> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        return dslContext.select(Tables.ASBIE.ASBIE_ID,
                Tables.ASBIE.GUID,
                Tables.ASBIE.FROM_ABIE_ID,
                Tables.ASBIE.TO_ASBIEP_ID,
                Tables.ASBIE.BASED_ASCC_ID,
                Tables.ASBIE.DEFINITION,
                Tables.ASBIE.CARDINALITY_MAX,
                Tables.ASBIE.CARDINALITY_MIN,
                Tables.ASBIE.IS_NILLABLE.as("nillable"),
                Tables.ASBIE.IS_USED.as("used"),
                Tables.ASBIE.REMARK,
                Tables.ASBIE.CREATED_BY,
                Tables.ASBIE.CREATION_TIMESTAMP,
                Tables.ASBIE.LAST_UPDATED_BY,
                Tables.ASBIE.LAST_UPDATE_TIMESTAMP,
                Tables.ASBIE.SEQ_KEY,
                Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID).from(Tables.ASBIE)
                .where(Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId)))
                .fetchInto(ASBIE.class);

    }

}
