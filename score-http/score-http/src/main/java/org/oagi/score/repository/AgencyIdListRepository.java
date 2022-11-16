package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record10;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.data.AgencyIdList;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class AgencyIdListRepository implements ScoreRepository<AgencyIdList> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<org.jooq.Record> getSelectOnConditionStep() {
        List<Field> fields = new ArrayList();
        fields.add(Tables.AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID);
        fields.addAll(Arrays.asList(Tables.AGENCY_ID_LIST.fields()));
        fields.add(Tables.AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID);
        return dslContext.select(fields)
                .from(Tables.AGENCY_ID_LIST)
                .join(Tables.AGENCY_ID_LIST_MANIFEST)
                .on(Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(Tables.AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID));
    }

    @Override
    public List<AgencyIdList> findAll() {
        return getSelectOnConditionStep().fetchInto(AgencyIdList.class);
    }

    @Override
    public List<AgencyIdList> findAllByReleaseId(BigInteger releaseId) {
        return getSelectOnConditionStep()
                .where(Tables.AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(AgencyIdList.class);
    }

    @Override
    public AgencyIdList findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.select(Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID, Tables.AGENCY_ID_LIST.NAME,
                Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID, Tables.AGENCY_ID_LIST.ENUM_TYPE_GUID,
                Tables.AGENCY_ID_LIST.GUID, Tables.AGENCY_ID_LIST.DEFINITION,
                Tables.AGENCY_ID_LIST.DEFINITION_SOURCE, Tables.AGENCY_ID_LIST.REMARK,
                Tables.AGENCY_ID_LIST.LIST_ID, Tables.AGENCY_ID_LIST.VERSION_ID).from(Tables.AGENCY_ID_LIST)
                .where(Tables.AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(AgencyIdList.class);
    }

}
