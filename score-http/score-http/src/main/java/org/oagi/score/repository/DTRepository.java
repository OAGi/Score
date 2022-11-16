package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.DT;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class DTRepository implements ScoreRepository<DT> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.DT_MANIFEST.DT_MANIFEST_ID,
                Tables.DT.DT_ID,
                Tables.DT.GUID,
                Tables.DT.DATA_TYPE_TERM,
                Tables.DT.REPRESENTATION_TERM,
                Tables.DT.QUALIFIER,
                Tables.DT.SIX_DIGIT_ID,
                Tables.DT_MANIFEST.BASED_DT_MANIFEST_ID,
                Tables.DT.BASED_DT_ID,
                Tables.DT.DEN,
                Tables.DT.DEFINITION,
                Tables.DT.DEFINITION_SOURCE,
                Tables.DT.CONTENT_COMPONENT_DEFINITION,
                Tables.DT.STATE,
                Tables.DT_MANIFEST.RELEASE_ID,
                Tables.RELEASE.RELEASE_NUM,
                Tables.DT_MANIFEST.LOG_ID,
                Tables.LOG.REVISION_NUM,
                Tables.LOG.REVISION_TRACKING_NUM,
                Tables.DT.CREATED_BY,
                Tables.DT.LAST_UPDATED_BY,
                Tables.DT.OWNER_USER_ID,
                Tables.DT.CREATION_TIMESTAMP,
                Tables.DT.LAST_UPDATE_TIMESTAMP,
                Tables.DT.IS_DEPRECATED.as("deprecated"))
                .from(Tables.DT)
                .join(Tables.DT_MANIFEST)
                .on(Tables.DT.DT_ID.eq(Tables.DT_MANIFEST.DT_ID))
                .join(Tables.RELEASE)
                .on(Tables.DT_MANIFEST.RELEASE_ID.eq(Tables.RELEASE.RELEASE_ID))
                .join(Tables.LOG)
                .on(Tables.DT_MANIFEST.LOG_ID.eq(Tables.LOG.LOG_ID));
    }

    @Override
    public List<DT> findAll() {
        return getSelectOnConditionStep().fetchInto(DT.class);
    }

    @Override
    public DT findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.DT.DT_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(DT.class).orElse(null);
    }
}
