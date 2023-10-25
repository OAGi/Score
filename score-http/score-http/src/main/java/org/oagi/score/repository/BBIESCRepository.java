package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.data.BBIESC;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;

@Repository
public class BBIESCRepository implements ScoreRepository<BBIESC> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record> getSelectJoinStep() {
        return dslContext.select(
                Tables.BBIE_SC.BBIE_SC_ID,
                Tables.BBIE_SC.GUID,
                Tables.BBIE_SC.BBIE_ID,
                Tables.BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                Tables.BBIE_SC.DT_SC_PRI_RESTRI_ID,
                Tables.BBIE_SC.CODE_LIST_MANIFEST_ID,
                Tables.BBIE_SC.AGENCY_ID_LIST_MANIFEST_ID,
                Tables.BBIE_SC.CARDINALITY_MIN,
                Tables.BBIE_SC.CARDINALITY_MAX,
                Tables.BBIE_SC.DEFAULT_VALUE,
                Tables.BBIE_SC.FIXED_VALUE,
                Tables.BBIE_SC.FACET_MIN_LENGTH,
                Tables.BBIE_SC.FACET_MAX_LENGTH,
                Tables.BBIE_SC.FACET_PATTERN,
                Tables.BBIE_SC.FACET_MIN_INCLUSIVE,
                Tables.BBIE_SC.FACET_MIN_EXCLUSIVE,
                Tables.BBIE_SC.FACET_MAX_INCLUSIVE,
                Tables.BBIE_SC.FACET_MAX_EXCLUSIVE,
                Tables.BBIE_SC.DEFINITION,
                Tables.BBIE_SC.REMARK,
                Tables.BBIE_SC.BIZ_TERM,
                Tables.BBIE_SC.EXAMPLE,
                Tables.BBIE_SC.IS_USED.as("used"),
                Tables.BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(Tables.BBIE_SC);
    }

    @Override
    public List<BBIESC> findAll() {
        return getSelectJoinStep().fetchInto(BBIESC.class);
    }

    @Override
    public BBIESC findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.BBIE_SC.BBIE_SC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BBIESC.class).orElse(null);
    }

    public List<BBIESC> findByOwnerTopLevelAsbiepIdsAndUsed(Collection<BigInteger> ownerTopLevelAsbiepIds, boolean used) {
        return getSelectJoinStep()
                .where(and(
                        (ownerTopLevelAsbiepIds.size() == 1) ?
                                Tables.BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(
                                        ULong.valueOf(ownerTopLevelAsbiepIds.iterator().next())) :
                                Tables.BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.in(
                                        ownerTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())),
                        Tables.BBIE_SC.IS_USED.eq((byte) (used ? 1 : 0))))
                .fetchInto(BBIESC.class);
    }

}
