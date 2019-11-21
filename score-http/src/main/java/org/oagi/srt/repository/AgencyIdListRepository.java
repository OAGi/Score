package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.AgencyIdList;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgencyIdListRepository implements SrtRepository<AgencyIdList> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<AgencyIdList> findAll() {
        return dslContext.select(Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID, Tables.AGENCY_ID_LIST.NAME,
                Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID, Tables.AGENCY_ID_LIST.ENUM_TYPE_GUID,
                Tables.AGENCY_ID_LIST.GUID, Tables.AGENCY_ID_LIST.MODULE_ID, Tables.AGENCY_ID_LIST.DEFINITION,
                Tables.AGENCY_ID_LIST.LIST_ID, Tables.AGENCY_ID_LIST.VERSION_ID).from(Tables.AGENCY_ID_LIST)
                .fetchInto(AgencyIdList.class);
    }

    @Override
    public AgencyIdList findById(long id) {
        return dslContext.select(Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID, Tables.AGENCY_ID_LIST.NAME,
                Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID, Tables.AGENCY_ID_LIST.ENUM_TYPE_GUID,
                Tables.AGENCY_ID_LIST.GUID, Tables.AGENCY_ID_LIST.MODULE_ID, Tables.AGENCY_ID_LIST.DEFINITION,
                Tables.AGENCY_ID_LIST.LIST_ID, Tables.AGENCY_ID_LIST.VERSION_ID).from(Tables.AGENCY_ID_LIST)
                .where(Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(AgencyIdList.class);
    }

}
