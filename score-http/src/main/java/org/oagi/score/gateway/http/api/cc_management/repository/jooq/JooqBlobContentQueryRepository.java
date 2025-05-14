package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.BlobContentQueryRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqBlobContentQueryRepository extends JooqBaseRepository implements BlobContentQueryRepository {

    public JooqBlobContentQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<BlobContentSummaryRecord> getBlobContentSummaryList(Collection<ReleaseId> releaseIdList) {
        if (releaseIdList == null || releaseIdList.isEmpty()) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetBlobContentSummaryQueryBuilder();
        return queryBuilder.select()
                .where(BLOB_CONTENT_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    private class GetBlobContentSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(
                            BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID,
                            BLOB_CONTENT.BLOB_CONTENT_ID,
                            BLOB_CONTENT.CONTENT,

                            LIBRARY.LIBRARY_ID,
                            LIBRARY.NAME.as("library_name"),
                            LIBRARY.STATE.as("library_state"),
                            LIBRARY.IS_READ_ONLY,

                            RELEASE.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            RELEASE.STATE.as("release_state"))
                    .from(BLOB_CONTENT_MANIFEST)
                    .join(BLOB_CONTENT).on(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_ID.eq(BLOB_CONTENT.BLOB_CONTENT_ID))
                    .join(RELEASE).on(BLOB_CONTENT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID));
        }

        private RecordMapper<Record, BlobContentSummaryRecord> mapper() {
            return record -> {
                BlobContentManifestId blobContentManifestId =
                        new BlobContentManifestId(record.get(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID).toBigInteger());
                BlobContentId blobContentId =
                        new BlobContentId(record.get(BLOB_CONTENT.BLOB_CONTENT_ID).toBigInteger());
                LibrarySummaryRecord library = new LibrarySummaryRecord(
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(LIBRARY.NAME.as("library_name")),
                        record.get(LIBRARY.STATE.as("library_state")),
                        (byte) 1 == record.get(LIBRARY.IS_READ_ONLY)
                );
                ReleaseSummaryRecord release = new ReleaseSummaryRecord(
                        new ReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger()),
                        new LibraryId(record.get(LIBRARY.LIBRARY_ID).toBigInteger()),
                        record.get(RELEASE.RELEASE_NUM),
                        ReleaseState.valueOf(record.get(RELEASE.STATE.as("release_state")))
                );
                return new BlobContentSummaryRecord(
                        library, release,

                        blobContentManifestId,
                        blobContentId,
                        record.get(BLOB_CONTENT.CONTENT)
                );
            };
        }
    }

}
