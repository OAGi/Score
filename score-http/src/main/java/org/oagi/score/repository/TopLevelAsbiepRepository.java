package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import static org.jooq.impl.DSL.and;

@Repository
public class TopLevelAsbiepRepository implements SrtRepository<TopLevelAsbiep> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<TopLevelAsbiep> findAll() {
        return dslContext.select(Tables.TOP_LEVEL_ASBIEP.fields())
                .from(Tables.TOP_LEVEL_ASBIEP)
                .fetchInto(TopLevelAsbiep.class);
    }

    @Override
    public TopLevelAsbiep findById(long id) {
        return dslContext.select(Tables.TOP_LEVEL_ASBIEP.fields())
                .from(Tables.TOP_LEVEL_ASBIEP)
                .where(Tables.TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(TopLevelAsbiep.class);
    }

    public List<TopLevelAsbiep> findByIdIn(List<Long> topLevelAsbiepIds) {
        return dslContext.select(Tables.TOP_LEVEL_ASBIEP.fields())
                .from(Tables.TOP_LEVEL_ASBIEP)
                .where(Tables.TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
                .fetchInto(TopLevelAsbiep.class);
    }

    public List<TopLevelAsbiep> findRefTopLevelAsbieps(Collection<Long> topLevelAsbiepIds) {
        return dslContext.select(Tables.TOP_LEVEL_ASBIEP.fields())
                .from(Tables.TOP_LEVEL_ASBIEP)
                .join(Tables.ASBIEP).on(Tables.TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(Tables.ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID))
                .join(Tables.ASBIE).on(and(
                        Tables.ASBIEP.ASBIEP_ID.eq(Tables.ASBIE.TO_ASBIEP_ID),
                        Tables.ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(Tables.ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                ))
                .where(and(
                        Tables.ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds)
                ))
                .fetchInto(TopLevelAsbiep.class);
    }

    public void updateTopLevelAsbiepLastUpdated(long userId, long topLevelAsbiepId) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        dslContext.update(Tables.TOP_LEVEL_ASBIEP)
                .set(Tables.TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(Tables.TOP_LEVEL_ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .where(Tables.TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .execute();
    }
}
