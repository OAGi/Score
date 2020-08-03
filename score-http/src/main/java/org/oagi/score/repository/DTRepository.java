package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.DT;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DTRepository implements SrtRepository<DT> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.DT.DT_ID,
                Tables.DT.GUID,
                Tables.DT.TYPE,
                Tables.DT.VERSION_NUM,
                Tables.DT.PREVIOUS_VERSION_DT_ID,
                Tables.DT.DATA_TYPE_TERM,
                Tables.DT.QUALIFIER,
                Tables.DT.BASED_DT_ID,
                Tables.DT.DEN,
                Tables.DT.CONTENT_COMPONENT_DEN,
                Tables.DT.DEFINITION,
                Tables.DT.DEFINITION_SOURCE,
                Tables.DT.CONTENT_COMPONENT_DEFINITION,
                Tables.DT.REVISION_DOC,
                Tables.DT.STATE,
                Tables.DT.MODULE_ID,
                Tables.DT.CREATED_BY,
                Tables.DT.LAST_UPDATED_BY,
                Tables.DT.OWNER_USER_ID,
                Tables.DT.CREATION_TIMESTAMP,
                Tables.DT.LAST_UPDATE_TIMESTAMP,
                Tables.DT.REVISION_NUM,
                Tables.DT.REVISION_TRACKING_NUM,
                Tables.DT.RELEASE_ID,
                Tables.DT.CURRENT_BDT_ID,
                Tables.DT.IS_DEPRECATED.as("deprecated"),
                Tables.MODULE.MODULE_.as("module")
        ).from(Tables.DT)
                .leftJoin(Tables.MODULE).on(Tables.DT.MODULE_ID.eq(Tables.MODULE.MODULE_ID));
    }

    @Override
    public List<DT> findAll() {
        return getSelectOnConditionStep().fetchInto(DT.class);
    }

    @Override
    public DT findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.DT.DT_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(DT.class).orElse(null);
    }
}
