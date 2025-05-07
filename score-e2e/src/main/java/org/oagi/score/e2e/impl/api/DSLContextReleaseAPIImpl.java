package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.ReleaseAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.ReleaseRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.LIBRARY;
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
    public ReleaseObject getReleaseByReleaseNumber(LibraryObject library, String releaseNumber) {
        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(and(
                        RELEASE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())),
                        RELEASE.RELEASE_NUM.eq(releaseNumber)
                ))
                .fetchOptional().orElse(null);
        return mapper(release);
    }

    @Override
    public List<ReleaseObject> getReleasesByStates(LibraryObject library, List<String> states) {
        if (states == null || states.isEmpty()) {
            return Collections.emptyList();
        }
        return dslContext.selectFrom(RELEASE)
                .where(and(
                        RELEASE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())),
                        RELEASE.STATE.in(states)
                ))
                .fetch(record -> mapper(record));
    }

    @Override
    public List<ReleaseObject> getReleases(LibraryObject library) {
        return dslContext.selectFrom(RELEASE)
                .where(RELEASE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())))
                .fetch(record -> mapper(record));
    }

    @Override
    public ReleaseObject getTheLatestRelease(LibraryObject library) {
        ULong maxReleaseId = dslContext.select(DSL.max(RELEASE.RELEASE_ID))
                .from(RELEASE)
                .where(RELEASE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())))
                .fetchOneInto(ULong.class);
        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(maxReleaseId))
                .fetchOptional().orElse(null);
        return mapper(release);
    }

    @Override
    public List<String> getAllReleasesBeforeRelease(LibraryObject library, ReleaseObject releaseNumber) {
        List<String> earlierReleases = new ArrayList<>();
        earlierReleases = dslContext.selectFrom(RELEASE)
                .where(and(
                        RELEASE.LIBRARY_ID.eq(ULong.valueOf(library.getLibraryId())),
                        RELEASE.CREATION_TIMESTAMP.lessThan(releaseNumber.getCreationTimestamp())
                ))
                .fetch(RELEASE.RELEASE_NUM);
        return earlierReleases;
    }

    @Override
    public ReleaseObject createRandomRelease(AppUserObject creator, LibraryObject library, NamespaceObject namespace) {
        ReleaseObject randomRelease = ReleaseObject.createRandomRelease(creator, library, namespace);

        ReleaseRecord releaseRecord = new ReleaseRecord();
        releaseRecord.setLibraryId(ULong.valueOf(library.getLibraryId()));
        releaseRecord.setGuid(randomRelease.getGuid());
        releaseRecord.setReleaseNum(randomRelease.getReleaseNumber());
        releaseRecord.setReleaseNote(randomRelease.getReleaseNote());
        releaseRecord.setReleaseLicense(randomRelease.getReleaseLicence());
        releaseRecord.setNamespaceId(ULong.valueOf(randomRelease.getNamespaceId()));
        releaseRecord.setCreatedBy(ULong.valueOf(randomRelease.getCreatedby()));
        releaseRecord.setLastUpdatedBy(ULong.valueOf(randomRelease.getLastUpdatedBy()));
        releaseRecord.setCreationTimestamp(randomRelease.getCreationTimestamp());
        releaseRecord.setLastUpdateTimestamp(randomRelease.getLastUpdateTimestamp());
        releaseRecord.setState(randomRelease.getState());

        randomRelease.setReleaseId(
                dslContext.insertInto(RELEASE)
                        .set(releaseRecord)
                        .returning(RELEASE.RELEASE_ID)
                        .fetchOne().getReleaseId().toBigInteger()
        );

        return randomRelease;
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
        release.setLibraryId(releaseRecord.getLibraryId().toBigInteger());
        if (releaseRecord.getNamespaceId() != null) {
            release.setNamespaceId(releaseRecord.getNamespaceId().toBigInteger());
        }
        release.setCreatedby(releaseRecord.getCreatedBy().toBigInteger());
        release.setLastUpdatedBy(releaseRecord.getLastUpdatedBy().toBigInteger());
        release.setCreationTimestamp(releaseRecord.getCreationTimestamp());
        release.setLastUpdateTimestamp(releaseRecord.getLastUpdateTimestamp());
        release.setState(releaseRecord.getState());
        return release;
    }
}
