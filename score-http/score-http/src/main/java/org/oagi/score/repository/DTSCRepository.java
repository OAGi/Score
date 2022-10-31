package org.oagi.score.repository;

import org.jooq.*;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.data.DTSC;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class DTSCRepository implements ScoreRepository<DTSC> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record16<ULong, ULong, String, String, String,
            String, String, String, String, ULong,
            ULong, Integer, Integer, ULong, ULong,
            UInteger>> getSelectJoinStep() {
        return dslContext.select(
                Tables.DT_SC_MANIFEST.DT_SC_MANIFEST_ID,
                Tables.DT_SC.DT_SC_ID,
                Tables.DT_SC.GUID,
                Tables.DT_SC.PROPERTY_TERM,
                Tables.DT_SC.REPRESENTATION_TERM,
                Tables.DT_SC.DEFINITION,
                Tables.DT_SC.DEFINITION_SOURCE,
                Tables.DT_SC.DEFAULT_VALUE,
                Tables.DT_SC.FIXED_VALUE,
                Tables.DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                Tables.DT_SC.OWNER_DT_ID,
                Tables.DT_SC.CARDINALITY_MIN,
                Tables.DT_SC.CARDINALITY_MAX,
                Tables.DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID,
                Tables.DT_SC.BASED_DT_SC_ID,
                Tables.LOG.REVISION_NUM)
                .from(Tables.DT_SC)
                .join(Tables.DT_SC_MANIFEST).on(Tables.DT_SC.DT_SC_ID.eq(Tables.DT_SC_MANIFEST.DT_SC_ID))
                .join(Tables.DT_MANIFEST).on(Tables.DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(Tables.DT_MANIFEST.DT_MANIFEST_ID))
                .join(Tables.LOG).on(Tables.DT_MANIFEST.LOG_ID.eq(Tables.LOG.LOG_ID));
    }

    @Override
    public List<DTSC> findAll() {
        return getSelectJoinStep().fetchInto(DTSC.class);
    }

    @Override
    public List<DTSC> findAllByReleaseId(BigInteger releaseId) {
        return getSelectJoinStep()
                .where(Tables.DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(DTSC.class);
    }

    @Override
    public DTSC findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.DT_SC.DT_SC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(DTSC.class).orElse(null);
    }
}
