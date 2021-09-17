package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.AgencyIdListValue;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class AgencyIdListValueRepository implements ScoreRepository<AgencyIdListValue> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<AgencyIdListValue> findAll() {
        return dslContext.select(Tables.AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID, Tables.AGENCY_ID_LIST_VALUE.NAME,
                Tables.AGENCY_ID_LIST_VALUE.OWNER_LIST_ID, Tables.AGENCY_ID_LIST_VALUE.DEFINITION,
                Tables.AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE, Tables.AGENCY_ID_LIST_VALUE.VALUE)
                .from(Tables.AGENCY_ID_LIST_VALUE).fetchInto(AgencyIdListValue.class);
    }

    @Override
    public AgencyIdListValue findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(Tables.AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID, Tables.AGENCY_ID_LIST_VALUE.NAME,
                Tables.AGENCY_ID_LIST_VALUE.OWNER_LIST_ID, Tables.AGENCY_ID_LIST_VALUE.DEFINITION,
                Tables.AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE, Tables.AGENCY_ID_LIST_VALUE.VALUE)
                .from(Tables.AGENCY_ID_LIST_VALUE)
                .where(Tables.AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(AgencyIdListValue.class);
    }

}
