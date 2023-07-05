package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.ReleaseAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.ReleaseRecord;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.List;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.RELEASE;

public class DSLContextReleaseAPIImpl implements ReleaseAPI {

    private final DSLContext dslContext;

    public DSLContextReleaseAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public ReleaseObject getReleaseById(BigInteger releaseId) {
        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchOptional().orElse(null);
        return mapper(release);
    }

    @Override
    public ReleaseObject getReleaseByReleaseNumber(String releaseNumber) {
        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_NUM.eq(releaseNumber))
                .fetchOptional().orElse(null);
        return mapper(release);
    }

    @Override
    public List<ReleaseObject> getReleases() {
        return dslContext.selectFrom(RELEASE)
                .fetch(record -> mapper(record));
    }

    @Override
    public ReleaseObject getTheLatestRelease() {
        ULong maxReleaseId = dslContext.select(DSL.max(RELEASE.RELEASE_ID))
                .from(RELEASE)
                .fetchOneInto(ULong.class);
        ;
        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(maxReleaseId))
                .fetchOptional().orElse(null);
        return mapper(release);
    }

    private ReleaseObject mapper(ReleaseRecord releaseRecord) {
        if (releaseRecord == null) {
            return null;
        }
        ReleaseObject release = new ReleaseObject();
        release.setReleaseId(releaseRecord.getReleaseId().toBigInteger());
        release.setReleaseNumber(releaseRecord.getReleaseNum());
        release.setGuid(releaseRecord.getGuid());
        release.setReleaseNote(releaseRecord.getReleaseNote());
        release.setReleaseLicence(releaseRecord.getReleaseLicense());
        release.setNamespaceId(releaseRecord.getNamespaceId().toBigInteger());
        release.setCreatedby(releaseRecord.getCreatedBy().toBigInteger());
        release.setLastUpdatedBy(releaseRecord.getLastUpdatedBy().toBigInteger());
        release.setCreationTimestamp(releaseRecord.getCreationTimestamp());
        release.setLastUpdateTimestamp(releaseRecord.getLastUpdateTimestamp());
        release.setState(releaseRecord.getState());
        return release;
    }
}
