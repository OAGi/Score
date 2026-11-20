package org.oagi.score.gateway.http.api.code_list_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.CcQueryService;
import org.oagi.score.gateway.http.api.cc_management.service.ValueListCandidateRule;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
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
        DtSummaryRecord dt = repositoryFactory.dtQueryRepository(requester).getDtSummary(dtManifestId);
        return dt == null ? Collections.emptyList() : availableCodeLists(
                requester, dt.release().releaseId(), query(requester).getNamedCodeListManifestIdsByDtManifestId(dtManifestId));
    }

    public List<CodeListSummaryRecord> availableCodeListListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null");
        }

        // The same candidates as for a data type.
        var dtSc = repositoryFactory.dtQueryRepository(requester).getDtScSummary(dtScManifestId);
        return dtSc == null ? Collections.emptyList() : availableCodeLists(
                requester, dtSc.release().releaseId(), query(requester).getNamedCodeListManifestIdsByDtScManifestId(dtScManifestId));
    }

    /**
     * The Code Lists of the release that {@link ValueListCandidateRule} offers for a node that approves the lists named.
     * The lists that are named come ordered by name; when none is named the whole release is offered, in its own order.
     */
    private List<CodeListSummaryRecord> availableCodeLists(
            ScoreUser requester, ReleaseId releaseId, Set<CodeListManifestId> named) {
        var query = query(requester);
        Set<CodeListManifestId> candidates = ValueListCandidateRule.resolve(
                query.getCodeListCandidateList(releaseId), named, requester.isDeveloper(),
                ValueListCandidateRule.Expansion.BASES_AND_THEIR_DERIVED, false);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }
        Map<CodeListManifestId, CodeListSummaryRecord> details = query.getCodeListSummaryListByManifestIds(candidates).stream()
                .collect(Collectors.toMap(CodeListSummaryRecord::codeListManifestId, Function.identity(), (a, b) -> a));
        var codeLists = candidates.stream().map(details::get).filter(Objects::nonNull);
        return (named.isEmpty() ? codeLists : codeLists.sorted(Comparator.comparing(CodeListSummaryRecord::name)))
                .collect(Collectors.toList());
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
