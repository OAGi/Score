package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.types.ULong;
import org.oagi.score.data.BCC;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class BCCRepository implements ScoreRepository<BCC> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectJoinStep() {
        return dslContext.select(
                Tables.BCC_MANIFEST.BCC_MANIFEST_ID,
                Tables.BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                Tables.BCC.BCC_ID,
                Tables.BCC.GUID,
                Tables.BCC.CARDINALITY_MIN,
                Tables.BCC.CARDINALITY_MAX,
                Tables.BCC.SEQ_KEY,
                Tables.BCC.ENTITY_TYPE,
                Tables.BCC.FROM_ACC_ID,
                Tables.BCC.TO_BCCP_ID,
                Tables.BCC.DEN,
                Tables.BCC.DEFINITION,
                Tables.BCC.DEFINITION_SOURCE,
                Tables.BCC.DEFAULT_VALUE,
                Tables.BCC.CREATED_BY,
                Tables.BCC.OWNER_USER_ID,
                Tables.BCC.LAST_UPDATED_BY,
                Tables.BCC.CREATION_TIMESTAMP,
                Tables.BCC.LAST_UPDATE_TIMESTAMP,
                Tables.BCC.STATE,
                Tables.BCC_MANIFEST.RELEASE_ID,
                Tables.RELEASE.RELEASE_NUM,
                Tables.BCC.IS_DEPRECATED.as("deprecated"),
                Tables.BCC.IS_NILLABLE.as("nillable"),
                Tables.BCC_MANIFEST.SEQ_KEY_ID,
                Tables.SEQ_KEY.PREV_SEQ_KEY_ID,
                Tables.SEQ_KEY.NEXT_SEQ_KEY_ID,
                Tables.LOG.REVISION_NUM,
                Tables.LOG.REVISION_TRACKING_NUM)
                .from(Tables.BCC)
                .join(Tables.BCC_MANIFEST)
                .on(Tables.BCC.BCC_ID.eq(Tables.BCC_MANIFEST.BCC_ID))
                .join(Tables.RELEASE)
                .on(Tables.BCC_MANIFEST.RELEASE_ID.eq(Tables.RELEASE.RELEASE_ID))
                .join(Tables.SEQ_KEY).on(Tables.SEQ_KEY.SEQ_KEY_ID.eq(Tables.BCC_MANIFEST.SEQ_KEY_ID))
                .join(Tables.ACC_MANIFEST)
                .on(Tables.BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(Tables.ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(Tables.LOG).on(Tables.ACC_MANIFEST.LOG_ID.eq(Tables.LOG.LOG_ID));
    }

    @Override
    public List<BCC> findAll() {
        return getSelectJoinStep().fetchInto(BCC.class);
    }

    @Override
    public List<BCC> findAllByReleaseId(BigInteger releaseId) {
        return getSelectJoinStep()
                .where(Tables.BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(BCC.class);
    }

    @Override
    public BCC findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BCC.class).orElse(null);
    }
}
