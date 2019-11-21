package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.ACC;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ACCRepository implements SrtRepository<ACC> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.DEN,
                Tables.ACC.DEFINITION,
                Tables.ACC.DEFINITION_SOURCE,
                Tables.ACC.BASED_ACC_ID,
                Tables.ACC.OBJECT_CLASS_QUALIFIER,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.MODULE_ID,
                Tables.ACC.NAMESPACE_ID,
                Tables.ACC.CREATED_BY,
                Tables.ACC.OWNER_USER_ID,
                Tables.ACC.LAST_UPDATED_BY,
                Tables.ACC.CREATION_TIMESTAMP,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                Tables.ACC.STATE,
                Tables.ACC.REVISION_NUM,
                Tables.ACC.REVISION_TRACKING_NUM,
                Tables.ACC.REVISION_ACTION,
                Tables.ACC.RELEASE_ID,
                Tables.ACC.CURRENT_ACC_ID,
                Tables.ACC.IS_DEPRECATED.as("deprecated"),
                Tables.ACC.IS_ABSTRACT.as("abstracted"),
                Tables.MODULE.MODULE_.as("module")
        ).from(Tables.ACC)
                .leftJoin(Tables.MODULE).on(Tables.ACC.MODULE_ID.eq(Tables.MODULE.MODULE_ID));
    }

    @Override
    public List<ACC> findAll() {
        return getSelectOnConditionStep().fetchInto(ACC.class);
    }

    @Override
    public ACC findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(ACC.class).orElse(null);
    }
}
