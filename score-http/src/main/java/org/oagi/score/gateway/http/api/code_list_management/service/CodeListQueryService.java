package org.oagi.score.gateway.http.api.code_list_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.service.CcQueryService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListListEntryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.criteria.CodeListListFilterCriteria;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Production;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Published;

@Service
@Transactional(readOnly = true)
public class CodeListQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private CodeListQueryRepository query(ScoreUser requester) {
        return repositoryFactory.codeListQueryRepository(requester);
    }

    @Autowired
    private CcQueryService ccQueryService;

    public List<CodeListSummaryRecord> getCodeListSummaryList(
            ScoreUser requester, ReleaseId releaseId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null");
        }

        return query(requester).getCodeListSummaryList(releaseId);
    }

    public List<CodeListSummaryRecord> availableCodeListListByDtManifestId(
            ScoreUser requester, DtManifestId dtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` must not be null");
        }

        // Issue #1723: developers see both developer-owned (Published) and end-user-owned
        // (Production) code lists so an already-assigned end-user code list is visible in the UI.
        // Assigning an end-user (Production) code list is blocked separately:
        //   - UI renders Production options read-only (disabled), and
        //   - the BIE update path rejects a developer assigning a Production/end-user code list.
        List<CcState> states = availableStates(requester);
        var query = query(requester);
        List<CodeListSummaryRecord> availableCodeLists =
                query.availableCodeListByDtManifestId(dtManifestId, states);
        if (!availableCodeLists.isEmpty() || query.hasCodeListAvailabilityByDtManifestId(dtManifestId)) {
            return availableCodeLists;
        }

        DtSummaryRecord dt = repositoryFactory.dtQueryRepository(requester).getDtSummary(dtManifestId);
        return dt == null ? Collections.emptyList() : filterByStates(
                query.getCodeListSummaryList(dt.release().releaseId()), states);
    }

    /**
     * Filters a caller-provided release-wide list without querying the database.
     * A null allowed-id set means unrestricted; an empty set means restricted
     * with no code-list option.
     */
    public List<CodeListSummaryRecord> availableCodeListListByDtManifestId(
            ScoreUser requester, DtManifestId dtManifestId,
            List<CodeListSummaryRecord> codeListList,
            Set<CodeListManifestId> allowedCodeListManifestIds) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` must not be null");
        }
        return codeListList == null
                ? availableCodeListListByDtManifestId(requester, dtManifestId)
                : filterByAllowedManifestIds(codeListList, allowedCodeListManifestIds);
    }

    public List<CodeListSummaryRecord> availableCodeListListByDtManifestId(
            ScoreUser requester, DtManifestId dtManifestId,
            List<CodeListSummaryRecord> codeListList,
            List<DtAwdPriSummaryRecord> approvedPrimitives) {
        return availableCodeListListByDtManifestId(requester, dtManifestId, codeListList,
                extractManifestIds(approvedPrimitives, DtAwdPriSummaryRecord::codeListManifestId));
    }

    public List<CodeListSummaryRecord> availableCodeListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null");
        }

        // Issue #1723: developers see both developer-owned (Published) and end-user-owned
        // (Production) code lists so an already-assigned end-user code list is visible in the UI.
        // Assigning an end-user (Production) code list is blocked separately:
        //   - UI renders Production options read-only (disabled), and
        //   - the BIE update path rejects a developer assigning a Production/end-user code list.
        List<CcState> states = availableStates(requester);
        var query = query(requester);
        List<CodeListSummaryRecord> availableCodeLists =
                query.availableCodeListByDtScManifestId(dtScManifestId, states);
        if (!availableCodeLists.isEmpty() || query.hasCodeListAvailabilityByDtScManifestId(dtScManifestId)) {
            return availableCodeLists;
        }

        var dtSc = repositoryFactory.dtQueryRepository(requester).getDtScSummary(dtScManifestId);
        return dtSc == null ? Collections.emptyList() : filterByStates(
                query.getCodeListSummaryList(dtSc.release().releaseId()), states);
    }

    /** Database-free counterpart for a preloaded target release list. */
    public List<CodeListSummaryRecord> availableCodeListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId,
            List<CodeListSummaryRecord> codeListList,
            Set<CodeListManifestId> allowedCodeListManifestIds) {
        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null");
        }
        return codeListList == null
                ? availableCodeListListByDtScManifestId(requester, dtScManifestId)
                : filterByAllowedManifestIds(codeListList, allowedCodeListManifestIds);
    }

    public List<CodeListSummaryRecord> availableCodeListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId,
            List<CodeListSummaryRecord> codeListList,
            List<DtScAwdPriSummaryRecord> approvedPrimitives) {
        return availableCodeListListByDtScManifestId(requester, dtScManifestId, codeListList,
                extractManifestIds(approvedPrimitives, DtScAwdPriSummaryRecord::codeListManifestId));
    }

    private List<CcState> availableStates(ScoreUser requester) {
        return requester.isDeveloper()
                ? Arrays.asList(Published, Production)
                : Collections.emptyList();
    }

    private List<CodeListSummaryRecord> filterByStates(
            List<CodeListSummaryRecord> codeLists, List<CcState> states) {
        if (states.isEmpty()) {
            return codeLists;
        }
        return codeLists.stream()
                .filter(codeList -> codeList != null && states.contains(codeList.state()))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<CodeListSummaryRecord> filterByAllowedManifestIds(
            Collection<CodeListSummaryRecord> codeLists,
            Set<CodeListManifestId> allowedManifestIds) {
        Set<CodeListManifestId> expandedAllowedManifestIds = expandDerivedCodeLists(
                codeLists, allowedManifestIds);
        return codeLists.stream()
                .filter(Objects::nonNull)
                .filter(codeList -> expandedAllowedManifestIds == null
                        || expandedAllowedManifestIds.contains(codeList.codeListManifestId()))
                .collect(java.util.stream.Collectors.toList());
    }

    private Set<CodeListManifestId> expandDerivedCodeLists(
            Collection<CodeListSummaryRecord> codeLists,
            Set<CodeListManifestId> allowedManifestIds) {
        if (allowedManifestIds == null) {
            return null;
        }
        Set<CodeListManifestId> expanded = new LinkedHashSet<>(allowedManifestIds);
        boolean changed;
        do {
            changed = false;
            for (CodeListSummaryRecord codeList : codeLists) {
                if (codeList != null && codeList.basedCodeListManifestId() != null
                        && expanded.contains(codeList.basedCodeListManifestId())) {
                    changed |= expanded.add(codeList.codeListManifestId());
                }
            }
        } while (changed);
        return expanded;
    }

    private <P> Set<CodeListManifestId> extractManifestIds(
            Collection<P> approvedPrimitives,
            Function<P, CodeListManifestId> manifestIdExtractor) {
        if (approvedPrimitives == null || approvedPrimitives.isEmpty()) {
            return null;
        }
        Set<CodeListManifestId> manifestIds = approvedPrimitives.stream()
                .filter(Objects::nonNull)
                .map(manifestIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return manifestIds.isEmpty() ? null : manifestIds;
    }

    /**
     * @param requester
     * @param codeListManifestId
     * @return
     * @throws IllegalArgumentException
     * @throws EmptyResultDataAccessException
     */
    public CodeListDetailsRecord getCodeListDetails(
            ScoreUser requester, CodeListManifestId codeListManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (codeListManifestId == null) {
            throw new IllegalArgumentException("`codeListManifestId` must not be null");
        }

        CodeListDetailsRecord codeListDetails =
                query(requester).getCodeListDetails(codeListManifestId);
        if (codeListDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return codeListDetails;
    }

    public CodeListDetailsRecord getPrevCodeListDetails(
            ScoreUser requester, CodeListManifestId codeListManifestId) {

        CodeListDetailsRecord prevCodeListDetails = query(requester).getPrevCodeListDetails(codeListManifestId);
        if (prevCodeListDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return prevCodeListDetails;
    }

    public ResultAndCount<CodeListListEntryRecord> getCodeListList(
            ScoreUser requester, CodeListListFilterCriteria filterCriteria, PageRequest pageRequest) {

        return query(requester).getCodeListList(filterCriteria, pageRequest);
    }

    public boolean hasSameCodeList(
            ScoreUser requester, ReleaseId releaseId,
            CodeListManifestId codeListManifestId,
            AgencyIdListValueManifestId agencyIdListValueManifestId,
            String listId, String versionId) {

        return query(requester).hasSameCodeList(
                releaseId, codeListManifestId, agencyIdListValueManifestId, listId, versionId);
    }

    public boolean hasSameNameCodeList(
            ScoreUser requester, ReleaseId releaseId,
            CodeListManifestId codeListManifestId,
            String codeListName) {

        return query(requester).hasSameNameCodeList(
                releaseId, codeListManifestId, codeListName);
    }
}
