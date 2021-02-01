package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.ASCC;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class ASCCRepository implements ScoreRepository<ASCC> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectJoinStep() {
        return dslContext.select(
                Tables.ASCC_MANIFEST.ASCC_MANIFEST_ID,
                Tables.ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                Tables.ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                Tables.ASCC.ASCC_ID,
                Tables.ASCC.GUID,
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.DEN,
                Tables.ASCC.DEFINITION,
                Tables.ASCC.DEFINITION_SOURCE,
                Tables.ASCC.CREATED_BY,
                Tables.ASCC.OWNER_USER_ID,
                Tables.ASCC.LAST_UPDATED_BY,
                Tables.ASCC.CREATION_TIMESTAMP,
                Tables.ASCC.LAST_UPDATE_TIMESTAMP,
                Tables.ASCC.STATE,
                Tables.ASCC_MANIFEST.RELEASE_ID,
                Tables.RELEASE.RELEASE_NUM,
                Tables.ASCC.IS_DEPRECATED.as("deprecated"),
                Tables.ASCC_MANIFEST.SEQ_KEY_ID,
                Tables.SEQ_KEY.PREV_SEQ_KEY_ID,
                Tables.SEQ_KEY.NEXT_SEQ_KEY_ID,
                Tables.LOG.REVISION_NUM,
                Tables.LOG.REVISION_TRACKING_NUM)
                .from(Tables.ASCC)
                .join(Tables.ASCC_MANIFEST)
                .on(Tables.ASCC.ASCC_ID.eq(Tables.ASCC_MANIFEST.ASCC_ID))
                .join(Tables.RELEASE)
                .on(Tables.ASCC_MANIFEST.RELEASE_ID.eq(Tables.RELEASE.RELEASE_ID))
                .join(Tables.SEQ_KEY).on(Tables.SEQ_KEY.SEQ_KEY_ID.eq(Tables.ASCC_MANIFEST.SEQ_KEY_ID))
                .join(Tables.ACC_MANIFEST)
                .on(Tables.ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(Tables.ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(Tables.LOG).on(Tables.ACC_MANIFEST.LOG_ID.eq(Tables.LOG.LOG_ID));
    }

    @Override
    public List<ASCC> findAll() {
        return getSelectJoinStep().fetchInto(ASCC.class);
    }

    @Override
    public List<ASCC> findAllByReleaseId(BigInteger releaseId) {
        return getSelectJoinStep()
                .where(Tables.ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(ASCC.class);
    }

    @Override
    public ASCC findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.ASCC.ASCC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(ASCC.class).orElse(null);
    }
}
