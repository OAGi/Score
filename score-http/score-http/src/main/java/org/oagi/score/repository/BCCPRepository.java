package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.BCCP;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BCCPRepository implements ScoreRepository<BCCP> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.BCCP_MANIFEST.BCCP_MANIFEST_ID,
                Tables.BCCP.BCCP_ID,
                Tables.BCCP.GUID,
                Tables.BCCP.PROPERTY_TERM,
                Tables.BCCP.REPRESENTATION_TERM,
                Tables.BCCP.DEFAULT_VALUE,
                Tables.BCCP.FIXED_VALUE,
                Tables.BCCP_MANIFEST.BDT_MANIFEST_ID,
                Tables.BCCP.BDT_ID,
                Tables.BCCP.DEN,
                Tables.BCCP.DEFINITION,
                Tables.BCCP.DEFINITION_SOURCE,
                Tables.BCCP.NAMESPACE_ID,
                Tables.BCCP.CREATED_BY,
                Tables.BCCP.OWNER_USER_ID,
                Tables.BCCP.LAST_UPDATED_BY,
                Tables.BCCP.CREATION_TIMESTAMP,
                Tables.BCCP.LAST_UPDATE_TIMESTAMP,
                Tables.BCCP.STATE,
                Tables.BCCP_MANIFEST.RELEASE_ID,
                Tables.RELEASE.RELEASE_NUM,
                Tables.BCCP_MANIFEST.LOG_ID,
                Tables.LOG.REVISION_NUM,
                Tables.LOG.REVISION_TRACKING_NUM,
                Tables.BCCP.IS_DEPRECATED.as("deprecated"),
                Tables.BCCP.IS_NILLABLE.as("nillable"))
                .from(Tables.BCCP)
                .join(Tables.BCCP_MANIFEST)
                .on(Tables.BCCP.BCCP_ID.eq(Tables.BCCP_MANIFEST.BCCP_ID))
                .join(Tables.RELEASE)
                .on(Tables.BCCP_MANIFEST.RELEASE_ID.eq(Tables.RELEASE.RELEASE_ID))
                .join(Tables.LOG)
                .on(Tables.BCCP_MANIFEST.LOG_ID.eq(Tables.LOG.LOG_ID));
    }

    @Override
    public List<BCCP> findAll() {
        return getSelectOnConditionStep().fetchInto(BCCP.class);
    }

    @Override
    public List<BCCP> findAllByReleaseIds(Collection<BigInteger> releaseIds) {
        if (releaseIds == null || releaseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getSelectOnConditionStep()
                .where(
                        (releaseIds.size() == 1) ?
                                Tables.BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseIds.iterator().next())) :
                                Tables.BCCP_MANIFEST.RELEASE_ID.in(releaseIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                )
                .fetchInto(BCCP.class);
    }

    @Override
    public BCCP findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(BCCP.class).orElse(null);
    }
}
