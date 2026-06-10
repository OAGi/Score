package org.oagi.score.gateway.http.api.integration_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Table;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.ComponentOwnerState;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAccManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAgencyIdListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAsccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueBccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueCodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueDtManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueLinkRecord;
import org.oagi.score.gateway.http.api.integration_management.repository.GitHubIssueLinkQueryRepository;
import org.oagi.score.gateway.http.common.model.Id;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/**
 * jOOQ-based implementation of {@link GitHubIssueLinkQueryRepository} (issue #1533).
 * <p>
 * The per-type public methods each name their own {@code github_issue_<type>_manifest} table and fields
 * and delegate to a small set of private helpers parameterized by those jOOQ fields, so the shared SQL
 * shape stays DRY without any {@code CcType} discriminator or runtime table lookup.
 */
public class JooqGitHubIssueLinkQueryRepository extends JooqBaseRepository
        implements GitHubIssueLinkQueryRepository {

    public JooqGitHubIssueLinkQueryRepository(DSLContext dslContext, ScoreUser requester,
                                              RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    // ----- Linked issues for a component (by manifest id) -----

    @Override
    public List<GitHubIssueLinkRecord> getLinkedIssues(AccManifestId manifestId) {
        return getLinkedIssues(GITHUB_ISSUE_ACC_MANIFEST,
                GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ACC_MANIFEST_ID,
                GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID,
                GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ID,
                valueOf(manifestId), GitHubIssueAccManifestId::from);
    }

    @Override
    public List<GitHubIssueLinkRecord> getLinkedIssues(AsccpManifestId manifestId) {
        return getLinkedIssues(GITHUB_ISSUE_ASCCP_MANIFEST,
                GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ASCCP_MANIFEST_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ID,
                valueOf(manifestId), GitHubIssueAsccpManifestId::from);
    }

    @Override
    public List<GitHubIssueLinkRecord> getLinkedIssues(BccpManifestId manifestId) {
        return getLinkedIssues(GITHUB_ISSUE_BCCP_MANIFEST,
                GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_BCCP_MANIFEST_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_ID,
                valueOf(manifestId), GitHubIssueBccpManifestId::from);
    }

    @Override
    public List<GitHubIssueLinkRecord> getLinkedIssues(DtManifestId manifestId) {
        return getLinkedIssues(GITHUB_ISSUE_DT_MANIFEST,
                GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_DT_MANIFEST_ID,
                GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID,
                GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_ID,
                valueOf(manifestId), GitHubIssueDtManifestId::from);
    }

    @Override
    public List<GitHubIssueLinkRecord> getLinkedIssues(CodeListManifestId manifestId) {
        return getLinkedIssues(GITHUB_ISSUE_CODE_LIST_MANIFEST,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_CODE_LIST_MANIFEST_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_ID,
                valueOf(manifestId), GitHubIssueCodeListManifestId::from);
    }

    @Override
    public List<GitHubIssueLinkRecord> getLinkedIssues(AgencyIdListManifestId manifestId) {
        return getLinkedIssues(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_ID,
                valueOf(manifestId), GitHubIssueAgencyIdListManifestId::from);
    }

    @Override
    public GitHubIssueId findIssueId(String repoOwner, String repoName, int issueNumber) {
        ULong issueId = dslContext().select(GITHUB_ISSUE.GITHUB_ISSUE_ID)
                .from(GITHUB_ISSUE)
                .where(GITHUB_ISSUE.REPO_OWNER.eq(repoOwner))
                .and(GITHUB_ISSUE.REPO_NAME.eq(repoName))
                .and(GITHUB_ISSUE.ISSUE_NUMBER.eq(issueNumber))
                .fetchOne(GITHUB_ISSUE.GITHUB_ISSUE_ID);
        return (issueId == null) ? null : new GitHubIssueId(issueId.toBigInteger());
    }

    // ----- Existing-link check (by manifest id + issue id) -----

    @Override
    public boolean isLinked(AccManifestId manifestId, GitHubIssueId issueId) {
        return isLinked(GITHUB_ISSUE_ACC_MANIFEST, GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID,
                GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ID, valueOf(manifestId), valueOf(issueId));
    }

    @Override
    public boolean isLinked(AsccpManifestId manifestId, GitHubIssueId issueId) {
        return isLinked(GITHUB_ISSUE_ASCCP_MANIFEST, GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ID, valueOf(manifestId), valueOf(issueId));
    }

    @Override
    public boolean isLinked(BccpManifestId manifestId, GitHubIssueId issueId) {
        return isLinked(GITHUB_ISSUE_BCCP_MANIFEST, GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_ID, valueOf(manifestId), valueOf(issueId));
    }

    @Override
    public boolean isLinked(DtManifestId manifestId, GitHubIssueId issueId) {
        return isLinked(GITHUB_ISSUE_DT_MANIFEST, GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID,
                GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_ID, valueOf(manifestId), valueOf(issueId));
    }

    @Override
    public boolean isLinked(CodeListManifestId manifestId, GitHubIssueId issueId) {
        return isLinked(GITHUB_ISSUE_CODE_LIST_MANIFEST, GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_ID, valueOf(manifestId), valueOf(issueId));
    }

    @Override
    public boolean isLinked(AgencyIdListManifestId manifestId, GitHubIssueId issueId) {
        return isLinked(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST, GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_ID, valueOf(manifestId), valueOf(issueId));
    }

    // ----- Owner/state of a component (by manifest id) -----

    @Override
    public ComponentOwnerState getComponentOwnerState(AccManifestId manifestId) {
        return accOwnerState(valueOf(manifestId));
    }

    @Override
    public ComponentOwnerState getComponentOwnerState(AsccpManifestId manifestId) {
        return asccpOwnerState(valueOf(manifestId));
    }

    @Override
    public ComponentOwnerState getComponentOwnerState(BccpManifestId manifestId) {
        return bccpOwnerState(valueOf(manifestId));
    }

    @Override
    public ComponentOwnerState getComponentOwnerState(DtManifestId manifestId) {
        return dtOwnerState(valueOf(manifestId));
    }

    @Override
    public ComponentOwnerState getComponentOwnerState(CodeListManifestId manifestId) {
        return codeListOwnerState(valueOf(manifestId));
    }

    @Override
    public ComponentOwnerState getComponentOwnerState(AgencyIdListManifestId manifestId) {
        return agencyIdListOwnerState(valueOf(manifestId));
    }

    // ----- Owner/state of the component a link points to (by link id) -----

    @Override
    public ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueAccManifestId linkId) {
        return ownerStateByLink(GITHUB_ISSUE_ACC_MANIFEST, GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ACC_MANIFEST_ID,
                GITHUB_ISSUE_ACC_MANIFEST.ACC_MANIFEST_ID, valueOf(linkId), this::accOwnerState);
    }

    @Override
    public ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueAsccpManifestId linkId) {
        return ownerStateByLink(GITHUB_ISSUE_ASCCP_MANIFEST, GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ASCCP_MANIFEST_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID, valueOf(linkId), this::asccpOwnerState);
    }

    @Override
    public ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueBccpManifestId linkId) {
        return ownerStateByLink(GITHUB_ISSUE_BCCP_MANIFEST, GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_BCCP_MANIFEST_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.BCCP_MANIFEST_ID, valueOf(linkId), this::bccpOwnerState);
    }

    @Override
    public ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueDtManifestId linkId) {
        return ownerStateByLink(GITHUB_ISSUE_DT_MANIFEST, GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_DT_MANIFEST_ID,
                GITHUB_ISSUE_DT_MANIFEST.DT_MANIFEST_ID, valueOf(linkId), this::dtOwnerState);
    }

    @Override
    public ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueCodeListManifestId linkId) {
        return ownerStateByLink(GITHUB_ISSUE_CODE_LIST_MANIFEST, GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_CODE_LIST_MANIFEST_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, valueOf(linkId), this::codeListOwnerState);
    }

    @Override
    public ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueAgencyIdListManifestId linkId) {
        return ownerStateByLink(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST, GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, valueOf(linkId), this::agencyIdListOwnerState);
    }

    // ----- Link row joined with its registry entry (by link id) -----

    @Override
    public GitHubIssueLinkRecord getLinkedIssue(GitHubIssueAccManifestId linkId) {
        return getLinkedIssue(GITHUB_ISSUE_ACC_MANIFEST, GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ACC_MANIFEST_ID,
                GITHUB_ISSUE_ACC_MANIFEST.GITHUB_ISSUE_ID, valueOf(linkId), GitHubIssueAccManifestId::from);
    }

    @Override
    public GitHubIssueLinkRecord getLinkedIssue(GitHubIssueAsccpManifestId linkId) {
        return getLinkedIssue(GITHUB_ISSUE_ASCCP_MANIFEST, GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ASCCP_MANIFEST_ID,
                GITHUB_ISSUE_ASCCP_MANIFEST.GITHUB_ISSUE_ID, valueOf(linkId), GitHubIssueAsccpManifestId::from);
    }

    @Override
    public GitHubIssueLinkRecord getLinkedIssue(GitHubIssueBccpManifestId linkId) {
        return getLinkedIssue(GITHUB_ISSUE_BCCP_MANIFEST, GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_BCCP_MANIFEST_ID,
                GITHUB_ISSUE_BCCP_MANIFEST.GITHUB_ISSUE_ID, valueOf(linkId), GitHubIssueBccpManifestId::from);
    }

    @Override
    public GitHubIssueLinkRecord getLinkedIssue(GitHubIssueDtManifestId linkId) {
        return getLinkedIssue(GITHUB_ISSUE_DT_MANIFEST, GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_DT_MANIFEST_ID,
                GITHUB_ISSUE_DT_MANIFEST.GITHUB_ISSUE_ID, valueOf(linkId), GitHubIssueDtManifestId::from);
    }

    @Override
    public GitHubIssueLinkRecord getLinkedIssue(GitHubIssueCodeListManifestId linkId) {
        return getLinkedIssue(GITHUB_ISSUE_CODE_LIST_MANIFEST, GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_CODE_LIST_MANIFEST_ID,
                GITHUB_ISSUE_CODE_LIST_MANIFEST.GITHUB_ISSUE_ID, valueOf(linkId), GitHubIssueCodeListManifestId::from);
    }

    @Override
    public GitHubIssueLinkRecord getLinkedIssue(GitHubIssueAgencyIdListManifestId linkId) {
        return getLinkedIssue(GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST, GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST_ID,
                GITHUB_ISSUE_AGENCY_ID_LIST_MANIFEST.GITHUB_ISSUE_ID, valueOf(linkId), GitHubIssueAgencyIdListManifestId::from);
    }

    // ----- Shared, jOOQ-field-parameterized helpers (no CcType) -----

    private List<GitHubIssueLinkRecord> getLinkedIssues(Table<?> linkTable, Field<ULong> linkPk,
                                                        Field<ULong> manifestIdField, Field<ULong> issueIdField,
                                                        ULong manifestId, Function<BigInteger, ? extends Id> linkIdFactory) {
        return dslContext().select(linkPk, issueIdField,
                        GITHUB_ISSUE.REPO_OWNER, GITHUB_ISSUE.REPO_NAME, GITHUB_ISSUE.ISSUE_NUMBER,
                        GITHUB_ISSUE.CACHED_METADATA)
                .from(linkTable)
                .join(GITHUB_ISSUE).on(GITHUB_ISSUE.GITHUB_ISSUE_ID.eq(issueIdField))
                .where(manifestIdField.eq(manifestId))
                .orderBy(GITHUB_ISSUE.REPO_OWNER.asc(), GITHUB_ISSUE.REPO_NAME.asc(), GITHUB_ISSUE.ISSUE_NUMBER.asc())
                .fetch(record -> toLinkRecord(record, linkPk, issueIdField, linkIdFactory));
    }

    private GitHubIssueLinkRecord getLinkedIssue(Table<?> linkTable, Field<ULong> linkPk, Field<ULong> issueIdField,
                                                 ULong linkId, Function<BigInteger, ? extends Id> linkIdFactory) {
        return dslContext().select(linkPk, issueIdField,
                        GITHUB_ISSUE.REPO_OWNER, GITHUB_ISSUE.REPO_NAME, GITHUB_ISSUE.ISSUE_NUMBER,
                        GITHUB_ISSUE.CACHED_METADATA)
                .from(linkTable)
                .join(GITHUB_ISSUE).on(GITHUB_ISSUE.GITHUB_ISSUE_ID.eq(issueIdField))
                .where(linkPk.eq(linkId))
                .fetchOne(record -> toLinkRecord(record, linkPk, issueIdField, linkIdFactory));
    }

    private GitHubIssueLinkRecord toLinkRecord(Record record, Field<ULong> linkPk, Field<ULong> issueIdField,
                                               Function<BigInteger, ? extends Id> linkIdFactory) {
        return new GitHubIssueLinkRecord(
                linkIdFactory.apply(record.get(linkPk).toBigInteger()),
                new GitHubIssueId(record.get(issueIdField).toBigInteger()),
                record.get(GITHUB_ISSUE.REPO_OWNER),
                record.get(GITHUB_ISSUE.REPO_NAME),
                record.get(GITHUB_ISSUE.ISSUE_NUMBER),
                record.get(GITHUB_ISSUE.CACHED_METADATA));
    }

    private boolean isLinked(Table<?> linkTable, Field<ULong> manifestIdField, Field<ULong> issueIdField,
                             ULong manifestId, ULong issueId) {
        return dslContext().fetchExists(dslContext().selectOne().from(linkTable)
                .where(manifestIdField.eq(manifestId))
                .and(issueIdField.eq(issueId)));
    }

    /** Resolves owner/state for the manifest the given link row points to, or {@code null} if the link is absent. */
    private ComponentOwnerState ownerStateByLink(Table<?> linkTable, Field<ULong> linkPk, Field<ULong> manifestIdField,
                                                 ULong linkId, Function<ULong, ComponentOwnerState> resolver) {
        ULong manifestId = dslContext().select(manifestIdField)
                .from(linkTable)
                .where(linkPk.eq(linkId))
                .fetchOne(manifestIdField);
        return (manifestId == null) ? null : resolver.apply(manifestId);
    }

    // ----- Per-entity owner/state resolution (distinct join per component table) -----

    private ComponentOwnerState accOwnerState(ULong manifestId) {
        return toOwnerState(dslContext().select(ACC.OWNER_USER_ID, ACC.STATE)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(manifestId))
                .fetchOne());
    }

    private ComponentOwnerState asccpOwnerState(ULong manifestId) {
        return toOwnerState(dslContext().select(ASCCP.OWNER_USER_ID, ASCCP.STATE)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(manifestId))
                .fetchOne());
    }

    private ComponentOwnerState bccpOwnerState(ULong manifestId) {
        return toOwnerState(dslContext().select(BCCP.OWNER_USER_ID, BCCP.STATE)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(manifestId))
                .fetchOne());
    }

    private ComponentOwnerState dtOwnerState(ULong manifestId) {
        return toOwnerState(dslContext().select(DT.OWNER_USER_ID, DT.STATE)
                .from(DT_MANIFEST)
                .join(DT).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(manifestId))
                .fetchOne());
    }

    private ComponentOwnerState codeListOwnerState(ULong manifestId) {
        return toOwnerState(dslContext().select(CODE_LIST.OWNER_USER_ID, CODE_LIST.STATE)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(manifestId))
                .fetchOne());
    }

    private ComponentOwnerState agencyIdListOwnerState(ULong manifestId) {
        return toOwnerState(dslContext().select(AGENCY_ID_LIST.OWNER_USER_ID, AGENCY_ID_LIST.STATE)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(manifestId))
                .fetchOne());
    }

    private ComponentOwnerState toOwnerState(Record2<ULong, String> record) {
        if (record == null) {
            return null;
        }
        UserId ownerUserId = (record.value1() == null) ? null : new UserId(record.value1().toBigInteger());
        return new ComponentOwnerState(ownerUserId, record.value2());
    }
}
