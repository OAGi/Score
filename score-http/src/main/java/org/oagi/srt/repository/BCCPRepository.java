package org.oagi.srt.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BCCP;
import org.oagi.srt.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BCCPRepository implements SrtRepository<BCCP> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.BCCP.BCCP_ID,
                Tables.BCCP.GUID,
                Tables.BCCP.PROPERTY_TERM,
                Tables.BCCP.REPRESENTATION_TERM,
                Tables.BCCP.DEFAULT_VALUE,
                Tables.BCCP.BDT_ID,
                Tables.BCCP.DEN,
                Tables.BCCP.DEFINITION,
                Tables.BCCP.DEFINITION_SOURCE,
                Tables.BCCP.MODULE_ID,
                Tables.BCCP.NAMESPACE_ID,
                Tables.BCCP.CREATED_BY,
                Tables.BCCP.OWNER_USER_ID,
                Tables.BCCP.LAST_UPDATED_BY,
                Tables.BCCP.CREATION_TIMESTAMP,
                Tables.BCCP.LAST_UPDATE_TIMESTAMP,
                Tables.BCCP.STATE,
                Tables.BCCP.REVISION_NUM,
                Tables.BCCP.REVISION_TRACKING_NUM,
                Tables.BCCP.REVISION_ACTION,
                Tables.BCCP.RELEASE_ID,
                Tables.BCCP.CURRENT_BCCP_ID,
                Tables.BCCP.IS_DEPRECATED.as("deprecated"),
                Tables.BCCP.IS_NILLABLE.as("nillable"),
                Tables.MODULE.MODULE_.as("module")
        ).from(Tables.BCCP)
                .leftJoin(Tables.MODULE).on(Tables.BCCP.MODULE_ID.eq(Tables.MODULE.MODULE_ID));
    }

    @Override
    public List<BCCP> findAll() {
        return getSelectOnConditionStep().fetchInto(BCCP.class);
    }

    @Override
    public BCCP findById(long id) {
        if (id <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BCCP.class).orElse(null);
    }
}
