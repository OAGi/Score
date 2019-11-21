package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.ASCCP;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ASCCPRepository implements SrtRepository<ASCCP> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.DEN,
                Tables.ASCCP.DEFINITION,
                Tables.ASCCP.DEFINITION_SOURCE,
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.MODULE_ID,
                Tables.ASCCP.NAMESPACE_ID,
                Tables.ASCCP.CREATED_BY,
                Tables.ASCCP.OWNER_USER_ID,
                Tables.ASCCP.LAST_UPDATED_BY,
                Tables.ASCCP.CREATION_TIMESTAMP,
                Tables.ASCCP.LAST_UPDATE_TIMESTAMP,
                Tables.ASCCP.STATE,
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.REVISION_ACTION,
                Tables.ASCCP.RELEASE_ID,
                Tables.ASCCP.CURRENT_ASCCP_ID,
                Tables.ASCCP.REUSABLE_INDICATOR,
                Tables.ASCCP.IS_DEPRECATED.as("deprecated"),
                Tables.ASCCP.IS_NILLABLE.as("nillable"),
                Tables.MODULE.MODULE_.as("module")
        ).from(Tables.ASCCP)
                .leftJoin(Tables.MODULE).on(Tables.ASCCP.MODULE_ID.eq(Tables.MODULE.MODULE_ID));
    }

    @Override
    public List<ASCCP> findAll() {
        return getSelectOnConditionStep().fetchInto(ASCCP.class);
    }

    @Override
    public ASCCP findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(ASCCP.class).orElse(null);
    }
}
