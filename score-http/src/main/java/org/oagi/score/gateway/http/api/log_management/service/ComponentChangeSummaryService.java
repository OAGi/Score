package org.oagi.score.gateway.http.api.log_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChangeSummary;
import org.oagi.score.gateway.http.api.log_management.model.LogSnapshotEntry;
import org.oagi.score.gateway.http.api.log_management.repository.LogRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import static org.oagi.score.gateway.http.api.log_management.service.ComponentChangeSummaryBuilder.revisionNum;

/**
 * Builds the per-component change summary (issue #1533, sub-task 3) — the shared capability behind
 * the GitHub status auto-post (sub-task 4) and the revert-warning dialog (sub-task 5).
 * <p>
 * A component at revision 1 yields a {@code NEW} summary of its current state; revision 2+ yields a
 * {@code REVISED} summary of what changed since the prior revision. The prior revision is resolved
 * with the existing {@code getPrev*Details} repository methods, which return the state frozen at
 * the moment Revise/Amend was invoked (the last log row of revision N−1) for both the developer
 * (cross-release manifest chain) and end-user (same-manifest log chain) paths. The diff runs over
 * {@code *DetailsRecord} pairs rather than LOG snapshots because the snapshots lack DT value
 * domains, the agency ID list remark, and several child flags.
 * <p>
 * Limitation on the end-user (same-manifest) path: the prev-details mappers resolve manifest-keyed
 * references from the <em>current</em> manifest row, so the prior ACC associations/based ACC,
 * ASCCP role-of ACC, BCCP data type and DT supplementary components are not recoverable there;
 * those comparisons are skipped (DT supplementary components fall back to the records'
 * {@code prevCardinality}) rather than silently reporting "no change". The developer path — the
 * primary #1533 scenario — diffs everything.
 */
@Service
@Transactional(readOnly = true)
public class ComponentChangeSummaryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private LogRepository logRepository;

    /**
     * Builds the change summary for a component.
     *
     * @param requester  the user reading the summary.
     * @param ccType     one of ACC, ASCCP, BCCP, DT, CODE_LIST, AGENCY_ID_LIST.
     * @param manifestId the component manifest id.
     * @return the summary; {@code NEW} when no prior revision is resolvable.
     * @throws IllegalArgumentException when the component does not exist or the type is unsupported.
     */
    public ComponentChangeSummary buildSummary(ScoreUser requester, CcType ccType, BigInteger manifestId) {
        Objects.requireNonNull(ccType, "ccType");
        Objects.requireNonNull(manifestId, "manifestId");
        return switch (ccType) {
            case ACC -> acc(requester, new AccManifestId(manifestId));
            case ASCCP -> asccp(requester, new AsccpManifestId(manifestId));
            case BCCP -> bccp(requester, new BccpManifestId(manifestId));
            case DT -> dt(requester, new DtManifestId(manifestId));
            case CODE_LIST -> codeList(requester, new CodeListManifestId(manifestId));
            case AGENCY_ID_LIST -> agencyIdList(requester, new AgencyIdListManifestId(manifestId));
            default -> throw new IllegalArgumentException(
                    "Unsupported component type for a change summary: " + ccType);
        };
    }

    private ComponentChangeSummary acc(ScoreUser requester, AccManifestId manifestId) {
        AccQueryRepository query = repositoryFactory.accQueryRepository(requester);
        AccDetailsRecord current = query.getAccDetails(manifestId);
        assertFound(current, CcType.ACC, manifestId.value());
        AccDetailsRecord prev = (revisionNum(current.log()) > 1) ? query.getPrevAccDetails(manifestId) : null;
        return ComponentChangeSummaryBuilder.acc(current, prev,
                prev != null && manifestId.equals(prev.accManifestId()));
    }

    private ComponentChangeSummary asccp(ScoreUser requester, AsccpManifestId manifestId) {
        AsccpQueryRepository query = repositoryFactory.asccpQueryRepository(requester);
        AsccpDetailsRecord current = query.getAsccpDetails(manifestId);
        assertFound(current, CcType.ASCCP, manifestId.value());
        AsccpDetailsRecord prev = (revisionNum(current.log()) > 1) ? query.getPrevAsccpDetails(manifestId) : null;
        return ComponentChangeSummaryBuilder.asccp(current, prev,
                prev != null && manifestId.equals(prev.asccpManifestId()));
    }

    private ComponentChangeSummary bccp(ScoreUser requester, BccpManifestId manifestId) {
        BccpQueryRepository query = repositoryFactory.bccpQueryRepository(requester);
        BccpDetailsRecord current = query.getBccpDetails(manifestId);
        assertFound(current, CcType.BCCP, manifestId.value());
        BccpDetailsRecord prev = (revisionNum(current.log()) > 1) ? query.getPrevBccpDetails(manifestId) : null;
        return ComponentChangeSummaryBuilder.bccp(current, prev,
                prev != null && manifestId.equals(prev.bccpManifestId()));
    }

    private ComponentChangeSummary dt(ScoreUser requester, DtManifestId manifestId) {
        DtQueryRepository query = repositoryFactory.dtQueryRepository(requester);
        DtDetailsRecord current = query.getDtDetails(manifestId);
        assertFound(current, CcType.DT, manifestId.value());
        List<DtScDetailsRecord> currentScList = query.getDtScDetailsList(manifestId);

        DtDetailsRecord prev = (revisionNum(current.log()) > 1) ? query.getPrevDtDetails(manifestId) : null;
        // The prior SC list is resolvable only when the prior revision lives on its own manifest
        // (developer cross-release revision). An end-user amendment revises in place — same
        // manifest — so the SC diff falls back to the prevCardinality carried on the records.
        List<DtScDetailsRecord> prevScList = null;
        if (prev != null && prev.dtManifestId() != null && !prev.dtManifestId().equals(current.dtManifestId())) {
            prevScList = query.getDtScDetailsList(prev.dtManifestId());
        }
        return ComponentChangeSummaryBuilder.dt(current, currentScList, prev, prevScList);
    }

    private ComponentChangeSummary codeList(ScoreUser requester, CodeListManifestId manifestId) {
        CodeListQueryRepository query = repositoryFactory.codeListQueryRepository(requester);
        CodeListDetailsRecord current = query.getCodeListDetails(manifestId);
        assertFound(current, CcType.CODE_LIST, manifestId.value());
        CodeListDetailsRecord prev = (revisionNum(current.log()) > 1) ? query.getPrevCodeListDetails(manifestId) : null;
        return ComponentChangeSummaryBuilder.codeList(current, prev);
    }

    private ComponentChangeSummary agencyIdList(ScoreUser requester, AgencyIdListManifestId manifestId) {
        AgencyIdListQueryRepository query = repositoryFactory.agencyIdListQueryRepository(requester);
        AgencyIdListDetailsRecord current = query.getAgencyIdListDetails(manifestId);
        assertFound(current, CcType.AGENCY_ID_LIST, manifestId.value());
        AgencyIdListDetailsRecord prev = (revisionNum(current.log()) > 1) ? query.getPrevAgencyIdListDetails(manifestId) : null;
        return ComponentChangeSummaryBuilder.agencyIdList(current, prev);
    }

    private static void assertFound(Object details, CcType ccType, BigInteger manifestId) {
        if (details == null) {
            // EmptyResultDataAccessException maps to 404, matching the other detail endpoints.
            throw new EmptyResultDataAccessException(
                    "Component not found: " + ccType + " " + manifestId, 1);
        }
    }

    /**
     * Builds the change summary between two user-selected LOG entries of the same component —
     * the summary counterpart of the log compare view, diffing the stored snapshots so the
     * baseline is exactly the selected pair (e.g. the last commit of the previous revision vs
     * the latest state). The lower log id is treated as the older side.
     *
     * @throws IllegalArgumentException when the entries belong to different components.
     */
    public ComponentChangeSummary buildSummary(ScoreUser requester, BigInteger beforeLogId, BigInteger afterLogId) {
        LogSnapshotEntry before = logRepository.getSnapshotEntryById(requester, beforeLogId);
        LogSnapshotEntry after = logRepository.getSnapshotEntryById(requester, afterLogId);
        if (before == null || after == null) {
            throw new EmptyResultDataAccessException(
                    "Log entry not found: " + ((before == null) ? beforeLogId : afterLogId), 1);
        }
        if (!Objects.equals(before.reference(), after.reference())) {
            throw new IllegalArgumentException("The selected log entries belong to different components.");
        }
        if (before.logId().compareTo(after.logId()) > 0) {
            LogSnapshotEntry tmp = before;
            before = after;
            after = tmp;
        }
        return SnapshotChangeSummaryBuilder.diff(before.snapshot(), after.snapshot(),
                after.revisionNum(), before.revisionNum());
    }
}
