package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record18;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.Xbt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.XBT;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.XBT_MANIFEST;

@Repository
public class XbtRepository implements ScoreRepository<Xbt> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record18<ULong, ULong, ULong, ULong, String, ULong, ULong, ULong, String, LocalDateTime,
                    Byte, String, String, String, LocalDateTime, String, String, Integer>> getSelectJoinStep() {
        return dslContext.selectDistinct(XBT.XBT_ID, XBT_MANIFEST.XBT_MANIFEST_ID.as("manifestId"), XBT.CREATED_BY,
                XBT.LAST_UPDATED_BY, XBT.NAME, XBT.OWNER_USER_ID,
                XBT_MANIFEST.RELEASE_ID, XBT.SUBTYPE_OF_XBT_ID, XBT.BUILTIN_TYPE,
                XBT.CREATION_TIMESTAMP, XBT.IS_DEPRECATED, XBT.JBT_DRAFT05_MAP, XBT.OPENAPI30_MAP, XBT.AVRO_MAP,
                XBT.LAST_UPDATE_TIMESTAMP, XBT.REVISION_DOC, XBT.SCHEMA_DEFINITION, XBT.STATE)
                .from(XBT)
                .join(XBT_MANIFEST).on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID));
    }

    @Override
    public List<Xbt> findAll() {
        return getSelectJoinStep()
                .fetchInto(Xbt.class);
    }

    @Override
    public Xbt findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(XBT.XBT_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(Xbt.class);
    }

}
