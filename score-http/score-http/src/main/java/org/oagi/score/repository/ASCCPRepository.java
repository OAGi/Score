package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.ASCCP;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class ASCCPRepository implements ScoreRepository<ASCCP> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                Tables.ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.DEN,
                Tables.ASCCP.DEFINITION,
                Tables.ASCCP.DEFINITION_SOURCE,
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.NAMESPACE_ID,
                Tables.ASCCP.CREATED_BY,
                Tables.ASCCP.OWNER_USER_ID,
                Tables.ASCCP.LAST_UPDATED_BY,
                Tables.ASCCP.CREATION_TIMESTAMP,
                Tables.ASCCP.LAST_UPDATE_TIMESTAMP,
                Tables.ASCCP.STATE,
                Tables.ASCCP_MANIFEST.RELEASE_ID,
                Tables.RELEASE.RELEASE_NUM,
                Tables.ASCCP_MANIFEST.LOG_ID,
                Tables.LOG.REVISION_NUM,
                Tables.LOG.REVISION_TRACKING_NUM,
                Tables.ASCCP.REUSABLE_INDICATOR,
                Tables.ASCCP.IS_DEPRECATED.as("deprecated"),
                Tables.ASCCP.IS_NILLABLE.as("nillable"))
                .from(Tables.ASCCP)
                .join(Tables.ASCCP_MANIFEST)
                .on(Tables.ASCCP.ASCCP_ID.eq(Tables.ASCCP_MANIFEST.ASCCP_ID))
                .join(Tables.RELEASE)
                .on(Tables.ASCCP_MANIFEST.RELEASE_ID.eq(Tables.RELEASE.RELEASE_ID))
                .join(Tables.LOG)
                .on(Tables.ASCCP_MANIFEST.LOG_ID.eq(Tables.LOG.LOG_ID));
    }

    @Override
    public List<ASCCP> findAll() {
        return getSelectOnConditionStep().fetchInto(ASCCP.class);
    }

    @Override
    public List<ASCCP> findAllByReleaseId(BigInteger releaseId) {
        return getSelectOnConditionStep()
                .where(Tables.ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(ASCCP.class);
    }

    @Override
    public ASCCP findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(ASCCP.class).orElse(null);
    }
}
