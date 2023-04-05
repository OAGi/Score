package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.AgencyIdListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.AGENCY_ID_LIST_VALUE;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.AGENCY_ID_LIST_VALUE_MANIFEST;

@Repository
public class AgencyIdListValueRepository implements ScoreRepository<AgencyIdListValue> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<AgencyIdListValue> findAll() {
        return dslContext.select(
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_VALUE.OWNER_LIST_ID,
                        AGENCY_ID_LIST_VALUE.NAME, AGENCY_ID_LIST_VALUE.DEFINITION,
                        AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE, AGENCY_ID_LIST_VALUE.VALUE)
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID))
                .fetchInto(AgencyIdListValue.class);
    }

    @Override
    public AgencyIdListValue findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_VALUE.OWNER_LIST_ID,
                        AGENCY_ID_LIST_VALUE.NAME, AGENCY_ID_LIST_VALUE.DEFINITION,
                        AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE, AGENCY_ID_LIST_VALUE.VALUE)
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID))
                .where(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(AgencyIdListValue.class);
    }

}
