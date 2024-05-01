package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
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
                .fetchInto(TopLevelAsbiep.class);
    }

    @Override
    public TopLevelAsbiep findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(TopLevelAsbiep.class);
    }

    public List<TopLevelAsbiep> findByIdIn(Collection<BigInteger> topLevelAsbiepIds) {
        return dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIds))
                .fetchInto(TopLevelAsbiep.class);
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
                .fetchInto(TopLevelAsbiep.class);
    }
}
