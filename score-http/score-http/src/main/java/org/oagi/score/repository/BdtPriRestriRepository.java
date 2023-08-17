package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.data.BdtPriRestri;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BDT_PRI_RESTRI;

@Repository
public class BdtPriRestriRepository implements ScoreRepository<BdtPriRestri> {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record6<ULong, ULong, ULong, ULong, ULong, Byte>> getSelectJoinStep() {
        return dslContext.select(
                BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID,
                BDT_PRI_RESTRI.BDT_MANIFEST_ID,
                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID,
                BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID,
                BDT_PRI_RESTRI.IS_DEFAULT.as("defaulted")
        ).from(BDT_PRI_RESTRI);
    }

    @Override
    public List<BdtPriRestri> findAll() {
        return getSelectJoinStep().fetchInto(BdtPriRestri.class);
    }

    @Override
    public BdtPriRestri findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BdtPriRestri.class).orElse(null);
    }
}
