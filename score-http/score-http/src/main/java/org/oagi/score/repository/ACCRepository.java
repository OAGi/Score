package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.data.ACC;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ACCRepository implements ScoreRepository<ACC> {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.ACC_MANIFEST.ACC_MANIFEST_ID,
                Tables.ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                Tables.ACC.ACC_ID,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC_MANIFEST.DEN,
                Tables.ACC.DEFINITION,
                Tables.ACC.DEFINITION_SOURCE,
                Tables.ACC.BASED_ACC_ID,
                Tables.ACC.OBJECT_CLASS_QUALIFIER,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.NAMESPACE_ID,
                Tables.ACC.CREATED_BY,
                Tables.ACC.OWNER_USER_ID,
                Tables.ACC.LAST_UPDATED_BY,
                Tables.ACC.CREATION_TIMESTAMP,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                Tables.ACC.STATE,
                Tables.ACC_MANIFEST.RELEASE_ID,
                Tables.RELEASE.RELEASE_NUM,
                Tables.ACC_MANIFEST.LOG_ID,
                Tables.LOG.REVISION_NUM,
                Tables.LOG.REVISION_TRACKING_NUM,
                Tables.ACC.IS_DEPRECATED.as("deprecated"),
                Tables.ACC.IS_ABSTRACT.as("abstracted"))
                .from(Tables.ACC)
                .join(Tables.ACC_MANIFEST)
                .on(Tables.ACC.ACC_ID.eq(Tables.ACC_MANIFEST.ACC_ID))
                .join(Tables.RELEASE)
                .on(Tables.ACC_MANIFEST.RELEASE_ID.eq(Tables.RELEASE.RELEASE_ID))
                .join(Tables.LOG)
                .on(Tables.ACC_MANIFEST.LOG_ID.eq(Tables.LOG.LOG_ID));
    }

    @Override
    public List<ACC> findAll() {
        return getSelectOnConditionStep().fetchInto(ACC.class);
    }

    @Override
    public List<ACC> findAllByReleaseIds(Collection<BigInteger> releaseIds) {
        if (releaseIds == null || releaseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getSelectOnConditionStep()
                .where(
                        (releaseIds.size() == 1) ?
                                Tables.ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseIds.iterator().next())) :
                                Tables.ACC_MANIFEST.RELEASE_ID.in(releaseIds.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                )
                .fetchInto(ACC.class);
    }

    @Override
    public ACC findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectOnConditionStep()
                .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(ACC.class).orElse(null);
    }
}
