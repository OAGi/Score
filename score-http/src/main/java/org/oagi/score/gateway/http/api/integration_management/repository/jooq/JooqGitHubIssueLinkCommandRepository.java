package org.oagi.score.gateway.http.api.integration_management.repository.jooq;

import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAccManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAgencyIdListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAsccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueBccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueCodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueDtManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueId;
import org.oagi.score.gateway.http.api.integration_management.repository.GitHubIssueLinkCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.notExists;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/**
 * jOOQ-based implementation of {@link GitHubIssueLinkCommandRepository} (issue #1533).
 * <p>
 * The per-type link writes each name their own {@code github_issue_<type>_manifest} table and fields and
 * delegate to a small set of private helpers parameterized by those jOOQ fields, so the shared SQL shape
 * stays DRY without any {@code CcType} discriminator or runtime table lookup.
 */
public class JooqGitHubIssueLinkCommandRepository extends JooqBaseRepository
        implements GitHubIssueLinkCommandRepository {

    public JooqGitHubIssueLinkCommandRepository(DSLContext dslContext, ScoreUser requester,
                                                RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public GitHubIssueId createIssue(String repoOwner, String repoName, int issueNumber,
                                     @Nullable String cachedMetadata) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime synced = (cachedMetadata == null) ? null : now;
        // Plain insert returning the generated id (the convention used by the other command repositories).
        // The service registers an issue at most once per (owner, repo, number) by checking findIssueId in
        // the same transaction, so this does not need an upsert; a truly concurrent first-time insert of the
        // same coordinates hits the unique key and rolls the transaction back rather than corrupting state.
        return new GitHubIssueId(
                dslContext().insertInto(GITHUB_ISSUE)
                        .set(GITHUB_ISSUE.REPO_OWNER, repoOwner)
                        .set(GITHUB_ISSUE.REPO_NAME, repoName)
                        .set(GITHUB_ISSUE.ISSUE_NUMBER, issueNumber)
                        .set(GITHUB_ISSUE.CACHED_METADATA, cachedMetadata)
                        .set(GITHUB_ISSUE.CACHED_SYNCED_TIMESTAMP, synced)
                        .set(GITHUB_ISSUE.CREATION_TIMESTAMP, now)
                        .set(GITHUB_ISSUE.LAST_UPDATE_TIMESTAMP, now)
                        .returning(GITHUB_ISSUE.GITHUB_ISSUE_ID)
                        .fetchOne().getGithubIssueId().toBigInteger());
    }

    @Override
    public boolean updateIssueCache(GitHubIssueId issueId, String cachedMetadata) {
        LocalDateTime now = LocalDateTime.now();
        return dslContext().update(GITHUB_ISSUE)
                .set(GITHUB_ISSUE.CACHED_METADATA, cachedMetadata)
                .set(GITHUB_ISSUE.CACHED_SYNCED_TIMESTAMP, now)
                .set(GITHUB_ISSUE.LAST_UPDATE_TIMESTAMP, now)
                .where(GITHUB_ISSUE.GITHUB_ISSUE_ID.eq(valueOf(issueId)))
                .execute() == 1;
    }

    @Override
    public boolean updateIssueCache(String repoOwner, String repoName, int issueNumber, String cachedMetadata) {
        LocalDateTime now = LocalDateTime.now();
        return dslContext().update(GITHUB_ISSUE)
                .set(GITHUB_ISSUE.CACHED_METADATA, cachedMetadata)
                .set(GITHUB_ISSUE.CACHED_SYNCED_TIMESTAMP, now)
                .set(GITHUB_ISSUE.LAST_UPDATE_TIMESTAMP, now)
                .where(GITHUB_ISSUE.REPO_OWNER.eq(repoOwner))
                .and(GITHUB_ISSUE.REPO_NAME.eq(repoName))
                .and(GITHUB_ISSUE.ISSUE_NUMBER.eq(issueNumber))
                .execute() > 0;
    }

    // ----- Link a component to a GitHub issue (per type) -----

    @Override
    public GitHubIssueAccManifestId createIssueLink(AccManifestId manifestId, GitHubIssueId issueId) {
        return new GitHubIssueAccManifestId(insertLink(GITHUB_ISSUE_ACC_MANIFEST,
                GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ACC_MANIFEST_ID,
                GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID,
                GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ID,
                GITHUB_ISSUE_ACC_MANIFEST.CREATED_BY,
                GITHUB_ISSUE_ACC_MANIFEST.LAST_UPDATED_BY,
                GITHUB_ISSUE_ACC_MANIFEST.CREATION_TIMESTAMP,
                GITHUB_ISSUE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP,
                valueOf(manifestId), valueOf(issueId)).toBigInteger());
    }

    @Override
    public GitHubIssueAsccpManifestId createIssueLink(AsccpManifestId manifestId, GitHubIssueId issueId) {
        return new GitHubIssueAsccpManifestId(insertLink(GITHUB_ISSUE_ASCCP_MANIFEST,
                GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ASCCP_MANIFEST_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.CREATED_BY,
                GITHUB_ISSUE_ASCCP_MANIFEST.LAST_UPDATED_BY,
                GITHUB_ISSUE_ASCCP_MANIFEST.CREATION_TIMESTAMP,
                GITHUB_ISSUE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP,
                valueOf(manifestId), valueOf(issueId)).toBigInteger());
    }

    @Override
    public GitHubIssueBccpManifestId createIssueLink(BccpManifestId manifestId, GitHubIssueId issueId) {
        return new GitHubIssueBccpManifestId(insertLink(GITHUB_ISSUE_BCCP_MANIFEST,
                GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_BCCP_MANIFEST_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.CREATED_BY,
                GITHUB_ISSUE_BCCP_MANIFEST.LAST_UPDATED_BY,
                GITHUB_ISSUE_BCCP_MANIFEST.CREATION_TIMESTAMP,
                GITHUB_ISSUE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP,
                valueOf(manifestId), valueOf(issueId)).toBigInteger());
    }

    @Override
    public GitHubIssueDtManifestId createIssueLink(DtManifestId manifestId, GitHubIssueId issueId) {
        return new GitHubIssueDtManifestId(insertLink(GITHUB_ISSUE_DT_MANIFEST,
                GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_DT_MANIFEST_ID,
                GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID,
                GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_ID,
                GITHUB_ISSUE_DT_MANIFEST.CREATED_BY,
                GITHUB_ISSUE_DT_MANIFEST.LAST_UPDATED_BY,
                GITHUB_ISSUE_DT_MANIFEST.CREATION_TIMESTAMP,
                GITHUB_ISSUE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP,
                valueOf(manifestId), valueOf(issueId)).toBigInteger());
    }

    @Override
    public GitHubIssueCodeListManifestId createIssueLink(CodeListManifestId manifestId, GitHubIssueId issueId) {
        return new GitHubIssueCodeListManifestId(insertLink(GITHUB_ISSUE_CODE_LIST_MANIFEST,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_CODE_LIST_MANIFEST_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.CREATED_BY,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.LAST_UPDATED_BY,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP,
                valueOf(manifestId), valueOf(issueId)).toBigInteger());
    }

    @Override
    public GitHubIssueAgencyIdListManifestId createIssueLink(AgencyIdListManifestId manifestId, GitHubIssueId issueId) {
        return new GitHubIssueAgencyIdListManifestId(insertLink(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.CREATED_BY,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP,
                valueOf(manifestId), valueOf(issueId)).toBigInteger());
    }

    // ----- Delete a GitHub issue link row (per type, by link id) -----

    @Override
    public boolean deleteIssueLink(GitHubIssueAccManifestId linkId) {
        return deleteLink(GITHUB_ISSUE_ACC_MANIFEST, GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ACC_MANIFEST_ID,
                valueOf(linkId));
    }

    @Override
    public boolean deleteIssueLink(GitHubIssueAsccpManifestId linkId) {
        return deleteLink(GITHUB_ISSUE_ASCCP_MANIFEST, GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ASCCP_MANIFEST_ID,
                valueOf(linkId));
    }

    @Override
    public boolean deleteIssueLink(GitHubIssueBccpManifestId linkId) {
        return deleteLink(GITHUB_ISSUE_BCCP_MANIFEST, GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_BCCP_MANIFEST_ID,
                valueOf(linkId));
    }

    @Override
    public boolean deleteIssueLink(GitHubIssueDtManifestId linkId) {
        return deleteLink(GITHUB_ISSUE_DT_MANIFEST, GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_DT_MANIFEST_ID,
                valueOf(linkId));
    }

    @Override
    public boolean deleteIssueLink(GitHubIssueCodeListManifestId linkId) {
        return deleteLink(GITHUB_ISSUE_CODE_LIST_MANIFEST, GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_CODE_LIST_MANIFEST_ID,
                valueOf(linkId));
    }

    @Override
    public boolean deleteIssueLink(GitHubIssueAgencyIdListManifestId linkId) {
        return deleteLink(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST, GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST_ID,
                valueOf(linkId));
    }

    @Override
    public boolean deleteIssueIfOrphaned(GitHubIssueId issueId) {
        ULong id = valueOf(issueId);
        // Delete the registry row only if no per-type link table references it. The NOT EXISTS guards are
        // evaluated atomically with the DELETE, so a link added concurrently is seen and the FK is never
        // violated.
        return dslContext().deleteFrom(GITHUB_ISSUE)
                .where(GITHUB_ISSUE.GITHUB_ISSUE_ID.eq(id))
                .and(notExists(dslContext().selectOne().from(GITHUB_ISSUE_ACC_MANIFEST)
                        .where(GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ID.eq(id))))
                .and(notExists(dslContext().selectOne().from(GITHUB_ISSUE_ASCCP_MANIFEST)
                        .where(GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ID.eq(id))))
                .and(notExists(dslContext().selectOne().from(GITHUB_ISSUE_BCCP_MANIFEST)
                        .where(GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_ID.eq(id))))
                .and(notExists(dslContext().selectOne().from(GITHUB_ISSUE_DT_MANIFEST)
                        .where(GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_ID.eq(id))))
                .and(notExists(dslContext().selectOne().from(GITHUB_ISSUE_CODE_LIST_MANIFEST)
                        .where(GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_ID.eq(id))))
                .and(notExists(dslContext().selectOne().from(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST)
                        .where(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_ID.eq(id))))
                .execute() == 1;
    }

    // ----- Shared, jOOQ-field-parameterized helpers (no CcType) -----

    /** Inserts a link row and returns its generated primary key (plain insert + returning). */
    private ULong insertLink(Table<?> linkTable, Field<ULong> linkPk,
                             Field<ULong> manifestIdField, Field<ULong> issueIdField,
                             Field<ULong> createdByField, Field<ULong> lastUpdatedByField,
                             Field<LocalDateTime> creationTimestampField, Field<LocalDateTime> lastUpdateTimestampField,
                             ULong manifestId, ULong issueId) {
        LocalDateTime now = LocalDateTime.now();
        ULong me = valueOf(requester().userId());
        return dslContext().insertInto(linkTable)
                .set(manifestIdField, manifestId)
                .set(issueIdField, issueId)
                .set(createdByField, me)
                .set(lastUpdatedByField, me)
                .set(creationTimestampField, now)
                .set(lastUpdateTimestampField, now)
                .returning(linkPk)
                .fetchOne()
                .get(linkPk);
    }

    private boolean deleteLink(Table<?> linkTable, Field<ULong> linkPk, ULong linkId) {
        return dslContext().deleteFrom(linkTable)
                .where(linkPk.eq(linkId))
                .execute() == 1;
    }

    // ----- Release carry-over (issue #1533) -----
    //
    // A Draft-release manifest records the Working manifest it was copied from in its NEXT_*_MANIFEST_ID
    // column, so the links attached to the Working manifest are carried onto the target manifest by joining
    // target -> its NEXT (Working) manifest -> the Working manifest's links. This mirrors how component
    // tags are carried over in JooqCcCommandRepository#copyWorkingManifests.

    /** Resolves the {@code Working} release id of the library that owns {@code releaseId}, or {@code null}. */
    private ReleaseId workingReleaseIdOf(ReleaseId releaseId) {
        var releaseQueryRepository = repositoryFactory().releaseQueryRepository(requester());
        ReleaseSummaryRecord releaseSummaryRecord =
                releaseQueryRepository.getReleaseSummary(releaseId);
        if (releaseSummaryRecord == null) {
            return null;
        }
        ReleaseSummaryRecord workingReleaseRecord =
                releaseQueryRepository.getReleaseSummary(releaseSummaryRecord.libraryId(), "Working");
        return (workingReleaseRecord != null) ? workingReleaseRecord.releaseId() : null;
    }

    @Override
    public void copyLinksFromWorking(ReleaseId targetReleaseId) {
        ReleaseId workingReleaseId = workingReleaseIdOf(targetReleaseId);
        if (workingReleaseId == null) {
            return;
        }

        dslContext().insertInto(GITHUB_ISSUE_ACC_MANIFEST,
                        GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID,
                        GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ID,
                        GITHUB_ISSUE_ACC_MANIFEST.CREATED_BY,
                        GITHUB_ISSUE_ACC_MANIFEST.LAST_UPDATED_BY,
                        GITHUB_ISSUE_ACC_MANIFEST.CREATION_TIMESTAMP,
                        GITHUB_ISSUE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                ACC_MANIFEST.ACC_MANIFEST_ID,
                                GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ID,
                                GITHUB_ISSUE_ACC_MANIFEST.CREATED_BY,
                                GITHUB_ISSUE_ACC_MANIFEST.LAST_UPDATED_BY,
                                GITHUB_ISSUE_ACC_MANIFEST.CREATION_TIMESTAMP,
                                GITHUB_ISSUE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP)
                        .from(ACC_MANIFEST)
                        .join(ACC_MANIFEST.as("next")).on(and(
                                ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("next").ACC_MANIFEST_ID),
                                ACC_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(GITHUB_ISSUE_ACC_MANIFEST).on(
                                ACC_MANIFEST.as("next").ACC_MANIFEST_ID.eq(GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID))
                        .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(targetReleaseId))))
                .execute();

        dslContext().insertInto(GITHUB_ISSUE_ASCCP_MANIFEST,
                        GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ID,
                        GITHUB_ISSUE_ASCCP_MANIFEST.CREATED_BY,
                        GITHUB_ISSUE_ASCCP_MANIFEST.LAST_UPDATED_BY,
                        GITHUB_ISSUE_ASCCP_MANIFEST.CREATION_TIMESTAMP,
                        GITHUB_ISSUE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                                GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ID,
                                GITHUB_ISSUE_ASCCP_MANIFEST.CREATED_BY,
                                GITHUB_ISSUE_ASCCP_MANIFEST.LAST_UPDATED_BY,
                                GITHUB_ISSUE_ASCCP_MANIFEST.CREATION_TIMESTAMP,
                                GITHUB_ISSUE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP_MANIFEST.as("next")).on(and(
                                ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID),
                                ASCCP_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(GITHUB_ISSUE_ASCCP_MANIFEST).on(
                                ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID.eq(GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(targetReleaseId))))
                .execute();

        dslContext().insertInto(GITHUB_ISSUE_BCCP_MANIFEST,
                        GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID,
                        GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_ID,
                        GITHUB_ISSUE_BCCP_MANIFEST.CREATED_BY,
                        GITHUB_ISSUE_BCCP_MANIFEST.LAST_UPDATED_BY,
                        GITHUB_ISSUE_BCCP_MANIFEST.CREATION_TIMESTAMP,
                        GITHUB_ISSUE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                BCCP_MANIFEST.BCCP_MANIFEST_ID,
                                GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_ID,
                                GITHUB_ISSUE_BCCP_MANIFEST.CREATED_BY,
                                GITHUB_ISSUE_BCCP_MANIFEST.LAST_UPDATED_BY,
                                GITHUB_ISSUE_BCCP_MANIFEST.CREATION_TIMESTAMP,
                                GITHUB_ISSUE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP)
                        .from(BCCP_MANIFEST)
                        .join(BCCP_MANIFEST.as("next")).on(and(
                                BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID),
                                BCCP_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(GITHUB_ISSUE_BCCP_MANIFEST).on(
                                BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID.eq(GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID))
                        .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(targetReleaseId))))
                .execute();

        dslContext().insertInto(GITHUB_ISSUE_DT_MANIFEST,
                        GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID,
                        GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_ID,
                        GITHUB_ISSUE_DT_MANIFEST.CREATED_BY,
                        GITHUB_ISSUE_DT_MANIFEST.LAST_UPDATED_BY,
                        GITHUB_ISSUE_DT_MANIFEST.CREATION_TIMESTAMP,
                        GITHUB_ISSUE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                DT_MANIFEST.DT_MANIFEST_ID,
                                GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_ID,
                                GITHUB_ISSUE_DT_MANIFEST.CREATED_BY,
                                GITHUB_ISSUE_DT_MANIFEST.LAST_UPDATED_BY,
                                GITHUB_ISSUE_DT_MANIFEST.CREATION_TIMESTAMP,
                                GITHUB_ISSUE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP)
                        .from(DT_MANIFEST)
                        .join(DT_MANIFEST.as("next")).on(and(
                                DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("next").DT_MANIFEST_ID),
                                DT_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(GITHUB_ISSUE_DT_MANIFEST).on(
                                DT_MANIFEST.as("next").DT_MANIFEST_ID.eq(GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID))
                        .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(targetReleaseId))))
                .execute();

        dslContext().insertInto(GITHUB_ISSUE_CODE_LIST_MANIFEST,
                        GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                        GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_ID,
                        GITHUB_ISSUE_CODE_LIST_MANIFEST.CREATED_BY,
                        GITHUB_ISSUE_CODE_LIST_MANIFEST.LAST_UPDATED_BY,
                        GITHUB_ISSUE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP,
                        GITHUB_ISSUE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                                GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_ID,
                                GITHUB_ISSUE_CODE_LIST_MANIFEST.CREATED_BY,
                                GITHUB_ISSUE_CODE_LIST_MANIFEST.LAST_UPDATED_BY,
                                GITHUB_ISSUE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP,
                                GITHUB_ISSUE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP)
                        .from(CODE_LIST_MANIFEST)
                        .join(CODE_LIST_MANIFEST.as("next")).on(and(
                                CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("next").CODE_LIST_MANIFEST_ID),
                                CODE_LIST_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(GITHUB_ISSUE_CODE_LIST_MANIFEST).on(
                                CODE_LIST_MANIFEST.as("next").CODE_LIST_MANIFEST_ID.eq(GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                        .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(targetReleaseId))))
                .execute();

        dslContext().insertInto(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST,
                        GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_ID,
                        GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.CREATED_BY,
                        GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY,
                        GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP,
                        GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_ID,
                                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.CREATED_BY,
                                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY,
                                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP,
                                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP)
                        .from(AGENCY_ID_LIST_MANIFEST)
                        .join(AGENCY_ID_LIST_MANIFEST.as("next")).on(and(
                                AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("next").AGENCY_ID_LIST_MANIFEST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST).on(
                                AGENCY_ID_LIST_MANIFEST.as("next").AGENCY_ID_LIST_MANIFEST_ID.eq(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                        .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(targetReleaseId))))
                .execute();
    }

    @Override
    public void deleteLinksByRelease(ReleaseId releaseId) {
        ULong release = valueOf(releaseId);

        dslContext().deleteFrom(GITHUB_ISSUE_ACC_MANIFEST)
                .where(GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID.in(
                        dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID).from(ACC_MANIFEST)
                                .where(ACC_MANIFEST.RELEASE_ID.eq(release))))
                .execute();
        dslContext().deleteFrom(GITHUB_ISSUE_ASCCP_MANIFEST)
                .where(GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(
                        dslContext().select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).from(ASCCP_MANIFEST)
                                .where(ASCCP_MANIFEST.RELEASE_ID.eq(release))))
                .execute();
        dslContext().deleteFrom(GITHUB_ISSUE_BCCP_MANIFEST)
                .where(GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID.in(
                        dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID).from(BCCP_MANIFEST)
                                .where(BCCP_MANIFEST.RELEASE_ID.eq(release))))
                .execute();
        dslContext().deleteFrom(GITHUB_ISSUE_DT_MANIFEST)
                .where(GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID.in(
                        dslContext().select(DT_MANIFEST.DT_MANIFEST_ID).from(DT_MANIFEST)
                                .where(DT_MANIFEST.RELEASE_ID.eq(release))))
                .execute();
        dslContext().deleteFrom(GITHUB_ISSUE_CODE_LIST_MANIFEST)
                .where(GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(
                        dslContext().select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).from(CODE_LIST_MANIFEST)
                                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(release))))
                .execute();
        dslContext().deleteFrom(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST)
                .where(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(
                        dslContext().select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).from(AGENCY_ID_LIST_MANIFEST)
                                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(release))))
                .execute();
    }

    @Override
    public void deleteWorkingLinksIncludedInRelease(ReleaseId releaseId) {
        // The Working manifests included in the published release are those its manifests point to via
        // NEXT_<type>_MANIFEST_ID (the inverse of copyLinksFromWorking); cleanUp preserves that pointer.
        // The Working release is resolved from the given release's library (one Working release per library).
        ReleaseId workingReleaseId = workingReleaseIdOf(releaseId);
        if (workingReleaseId == null) {
            return;
        }

        dslContext().deleteFrom(GITHUB_ISSUE_ACC_MANIFEST)
                .where(GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID.in(
                        dslContext().select(ACC_MANIFEST.as("next").ACC_MANIFEST_ID)
                                .from(ACC_MANIFEST)
                                .join(ACC_MANIFEST.as("next")).on(and(
                                        ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("next").ACC_MANIFEST_ID),
                                        ACC_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();

        dslContext().deleteFrom(GITHUB_ISSUE_ASCCP_MANIFEST)
                .where(GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(
                        dslContext().select(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID)
                                .from(ASCCP_MANIFEST)
                                .join(ASCCP_MANIFEST.as("next")).on(and(
                                        ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID),
                                        ASCCP_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                                .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();

        dslContext().deleteFrom(GITHUB_ISSUE_BCCP_MANIFEST)
                .where(GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID.in(
                        dslContext().select(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID)
                                .from(BCCP_MANIFEST)
                                .join(BCCP_MANIFEST.as("next")).on(and(
                                        BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID),
                                        BCCP_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();

        dslContext().deleteFrom(GITHUB_ISSUE_DT_MANIFEST)
                .where(GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID.in(
                        dslContext().select(DT_MANIFEST.as("next").DT_MANIFEST_ID)
                                .from(DT_MANIFEST)
                                .join(DT_MANIFEST.as("next")).on(and(
                                        DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("next").DT_MANIFEST_ID),
                                        DT_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();

        dslContext().deleteFrom(GITHUB_ISSUE_CODE_LIST_MANIFEST)
                .where(GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(
                        dslContext().select(CODE_LIST_MANIFEST.as("next").CODE_LIST_MANIFEST_ID)
                                .from(CODE_LIST_MANIFEST)
                                .join(CODE_LIST_MANIFEST.as("next")).on(and(
                                        CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("next").CODE_LIST_MANIFEST_ID),
                                        CODE_LIST_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();

        dslContext().deleteFrom(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST)
                .where(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(
                        dslContext().select(AGENCY_ID_LIST_MANIFEST.as("next").AGENCY_ID_LIST_MANIFEST_ID)
                                .from(AGENCY_ID_LIST_MANIFEST)
                                .join(AGENCY_ID_LIST_MANIFEST.as("next")).on(and(
                                        AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("next").AGENCY_ID_LIST_MANIFEST_ID),
                                        AGENCY_ID_LIST_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();
    }
}
