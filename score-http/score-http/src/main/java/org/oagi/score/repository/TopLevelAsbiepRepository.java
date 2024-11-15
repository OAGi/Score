package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.jooq.types.ULong;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class TopLevelAsbiepRepository implements ScoreRepository<TopLevelAsbiep> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<TopLevelAsbiep> findAll() {
        return dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .fetch(topLevelAsbiepRecordMapper());
    }

    private RecordMapper<org.jooq.Record, TopLevelAsbiep> topLevelAsbiepRecordMapper() {
        return record -> {
            TopLevelAsbiep topLevelAsbiep = new TopLevelAsbiep();
            topLevelAsbiep.setTopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
            if (record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID) != null) {
                topLevelAsbiep.setBasedTopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID).toBigInteger());
            }
            if (record.get(TOP_LEVEL_ASBIEP.ASBIEP_ID) != null) {
                topLevelAsbiep.setAsbiepId(record.get(TOP_LEVEL_ASBIEP.ASBIEP_ID).toBigInteger());
            }
            topLevelAsbiep.setOwnerUserId(record.get(TOP_LEVEL_ASBIEP.OWNER_USER_ID).toBigInteger());
            topLevelAsbiep.setReleaseId(record.get(TOP_LEVEL_ASBIEP.RELEASE_ID).toBigInteger());
            topLevelAsbiep.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
            topLevelAsbiep.setStatus(record.get(TOP_LEVEL_ASBIEP.STATUS));
            topLevelAsbiep.setState(BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)));
            topLevelAsbiep.setLastUpdatedBy(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY).toBigInteger());
            topLevelAsbiep.setLastUpdateTimestamp(Date.from(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP, LocalDateTime.class)
                    .atZone(ZoneId.systemDefault()).toInstant()));
            topLevelAsbiep.setDeprecated(record.get(TOP_LEVEL_ASBIEP.IS_DEPRECATED) == (byte) 1);
            topLevelAsbiep.setInverseMode(record.get(TOP_LEVEL_ASBIEP.INVERSE_MODE) == (byte) 1);
            return topLevelAsbiep;
        };
    }

    @Override
    public TopLevelAsbiep findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(id)))
                .fetchOne(topLevelAsbiepRecordMapper());
    }

    public List<TopLevelAsbiep> findByIdIn(Collection<BigInteger> topLevelAsbiepIds) {
        return dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
                .fetch(topLevelAsbiepRecordMapper());
    }

    public List<TopLevelAsbiep> findByBasedTopLevelAsbiepId(BigInteger basedTopLevelAsbiepId) {
        return dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.BASED_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(basedTopLevelAsbiepId)))
                .fetch(topLevelAsbiepRecordMapper());
    }

    public List<TopLevelAsbiep> findRefTopLevelAsbieps(Collection<BigInteger> topLevelAsbiepIds) {
        return dslContext.select(TOP_LEVEL_ASBIEP.fields())
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID))
                .join(ASBIE).on(and(
                        ASBIEP.ASBIEP_ID.eq(ASBIE.TO_ASBIEP_ID),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)
                ))
                .fetch(topLevelAsbiepRecordMapper());
    }
}
