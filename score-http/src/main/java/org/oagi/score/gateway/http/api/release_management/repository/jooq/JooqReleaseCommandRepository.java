package org.oagi.score.gateway.http.api.release_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ReleaseRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import static org.jooq.impl.DSL.inline;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.RELEASE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.RELEASE_DEP;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

/**
 * Jooq implementation of {@link ReleaseCommandRepository} for handling release management operations.
 */
public class JooqReleaseCommandRepository extends JooqBaseRepository implements ReleaseCommandRepository {

    /**
     * Constructs a new {@code JooqReleaseCommandRepository} with the given {@code DSLContext}.
     *
     * @param dslContext The {@code DSLContext} used to interact with the database.
     */
    public JooqReleaseCommandRepository(DSLContext dslContext, ScoreUser requester,
                                        RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public ReleaseId create(LibraryId libraryId,
                            NamespaceId namespaceId,
                            String releaseNum,
                            String releaseNote,
                            String releaseLicense) {

        ReleaseState initialReleaseState = ReleaseState.Initialized;

        LocalDateTime timestamp = LocalDateTime.now();
        ReleaseRecord releaseRecord = new ReleaseRecord();
        releaseRecord.setGuid(randomGuid());
        releaseRecord.setReleaseNum(releaseNum);
        releaseRecord.setReleaseNote(releaseNote);
        releaseRecord.setReleaseLicense(releaseLicense);
        releaseRecord.setLibraryId(valueOf(libraryId));
        if (namespaceId != null) {
            releaseRecord.setNamespaceId(valueOf(namespaceId));
        }
        releaseRecord.setCreatedBy(valueOf(requester().userId()));
        releaseRecord.setLastUpdatedBy(valueOf(requester().userId()));
        releaseRecord.setCreationTimestamp(timestamp);
        releaseRecord.setLastUpdateTimestamp(timestamp);

        return new ReleaseId(
                dslContext().insertInto(RELEASE)
                        .set(releaseRecord)
                        .returning(RELEASE.RELEASE_ID)
                        .fetchOne().getReleaseId().toBigInteger()
        );
    }

    @Override
    public boolean update(ReleaseId releaseId,
                          NamespaceId namespaceId,
                          String releaseNum,
                          String releaseNote,
                          String releaseLicense) {

        LocalDateTime timestamp = LocalDateTime.now();
        int num = dslContext().update(RELEASE)
                .set(RELEASE.RELEASE_NUM, releaseNum)
                .set(RELEASE.RELEASE_NOTE, releaseNote)
                .set(RELEASE.RELEASE_LICENSE, releaseLicense)
                .set(RELEASE.NAMESPACE_ID, (namespaceId != null) ? valueOf(namespaceId) : null)
                .set(RELEASE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        return num == 1;
    }

    @Override
    public boolean updateState(ReleaseId releaseId, ReleaseState releaseState) {
        LocalDateTime timestamp = LocalDateTime.now();
        int num = dslContext().update(RELEASE)
                .set(RELEASE.STATE, releaseState.name())
                .set(RELEASE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();

        if (releaseState == ReleaseState.Published) {

            // update relations in between releases.
            var query = repositoryFactory().releaseQueryRepository(requester());
            ReleaseDetailsRecord release = query.getReleaseDetails(releaseId);
            ReleaseDetailsRecord workingRelease = query.getReleaseDetails(release.libraryId(), "Working");

            dslContext().update(RELEASE)
                    .set(RELEASE.PREV_RELEASE_ID, valueOf(workingRelease.prev().releaseId()))
                    .set(RELEASE.NEXT_RELEASE_ID, valueOf(workingRelease.releaseId()))
                    .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                    .execute();

            dslContext().update(RELEASE)
                    .set(RELEASE.NEXT_RELEASE_ID, valueOf(releaseId))
                    .where(RELEASE.RELEASE_ID.eq(valueOf(workingRelease.prev().releaseId())))
                    .execute();

            dslContext().update(RELEASE)
                    .set(RELEASE.PREV_RELEASE_ID, valueOf(releaseId))
                    .where(RELEASE.RELEASE_ID.eq(valueOf(workingRelease.releaseId())))
                    .execute();
        }
        return num == 1;
    }

    @Override
    public boolean delete(ReleaseId releaseId) {
        return delete(Arrays.asList(releaseId)) == 1;
    }

    @Override
    public int delete(Collection<ReleaseId> releaseIdList) {
        if (releaseIdList == null || releaseIdList.isEmpty()) {
            return 0;
        }

        int res = dslContext().deleteFrom(RELEASE)
                .where(
                        releaseIdList.size() == 1 ?
                                RELEASE.RELEASE_ID.eq(valueOf(releaseIdList.iterator().next())) :
                                RELEASE.RELEASE_ID.in(valueOf(releaseIdList)))
                .execute();
        return res;
    }

    @Override
    public void copyDepsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId) {
        // Add `release_dep`
        dslContext().insertInto(RELEASE_DEP,
                        RELEASE_DEP.RELEASE_ID,
                        RELEASE_DEP.DEPEND_ON_RELEASE_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                RELEASE_DEP.DEPEND_ON_RELEASE_ID)
                        .from(RELEASE_DEP)
                        .where(RELEASE_DEP.RELEASE_ID.eq(valueOf(workingReleaseId)))).execute();
    }

    @Override
    public void deleteDeps(ReleaseId releaseId) {
        dslContext().deleteFrom(RELEASE_DEP)
                .where(RELEASE_DEP.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
    }

}
