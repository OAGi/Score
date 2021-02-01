package org.oagi.score.repo.component.top_level_asbiep;

import org.jooq.DSLContext;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import static org.jooq.impl.DSL.and;

@Repository
public class TopLevelAsbiepReadRepository {

    @Autowired
    private DSLContext dslContext;

    public List<TopLevelAsbiep> findRefTopLevelAsbieps(Collection<BigInteger> topLevelAsbiepIds) {
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

}
