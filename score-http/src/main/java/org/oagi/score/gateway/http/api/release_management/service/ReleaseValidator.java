package org.oagi.score.gateway.http.api.release_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageCode.*;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Error;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Warning;

public class ReleaseValidator {

    private final ScoreUser requester;
    private final ReleaseId releaseId;
    private final RepositoryFactory repositoryFactory;

    private List<AccManifestId> assignedAccComponentManifestIds = Collections.emptyList();
    private List<AsccpManifestId> assignedAsccpComponentManifestIds = Collections.emptyList();
    private List<BccpManifestId> assignedBccpComponentManifestIds = Collections.emptyList();
    private List<CodeListManifestId> assignedCodeListComponentManifestIds = Collections.emptyList();
    private List<AgencyIdListManifestId> assignedAgencyIdListComponentManifestIds = Collections.emptyList();
    private List<DtManifestId> assignedDtComponentManifestIds = Collections.emptyList();

    private List<AccSummaryRecord> accList;
    private Map<AccManifestId, AccSummaryRecord> accMap;

    private List<AsccSummaryRecord> asccList;
    private List<BccSummaryRecord> bccList;

    private List<AsccpSummaryRecord> asccpList;
    private Map<AsccpManifestId, AsccpSummaryRecord> asccpMap;

    private List<BccpSummaryRecord> bccpList;
    private Map<BccpManifestId, BccpSummaryRecord> bccpMap;

    private List<CodeListSummaryRecord> codeListList;

    private List<AgencyIdListSummaryRecord> agencyIdListList;

    private List<DtSummaryRecord> dtList;
    private Map<DtManifestId, DtSummaryRecord> dtMap;

    public ReleaseValidator(ScoreUser requester, ReleaseId releaseId,
                            RepositoryFactory repositoryFactory) {
        this.requester = requester;
        this.releaseId = releaseId;
        this.repositoryFactory = repositoryFactory;
    }

    public void setAssignedAccComponentManifestIds(List<AccManifestId> assignedAccComponentManifestIds) {
        this.assignedAccComponentManifestIds = assignedAccComponentManifestIds;
    }

    public void setAssignedAsccpComponentManifestIds(List<AsccpManifestId> assignedAsccpComponentManifestIds) {
        this.assignedAsccpComponentManifestIds = assignedAsccpComponentManifestIds;
    }

    public void setAssignedBccpComponentManifestIds(List<BccpManifestId> assignedBccpComponentManifestIds) {
        this.assignedBccpComponentManifestIds = assignedBccpComponentManifestIds;
    }

    public void setAssignedCodeListComponentManifestIds(List<CodeListManifestId> assignedCodeListComponentManifestIds) {
        this.assignedCodeListComponentManifestIds = assignedCodeListComponentManifestIds;
    }

    public void setAssignedAgencyIdListComponentManifestIds(List<AgencyIdListManifestId> assignedAgencyIdListComponentManifestIds) {
        this.assignedAgencyIdListComponentManifestIds = assignedAgencyIdListComponentManifestIds;
    }

    public void setAssignedDtComponentManifestIds(List<DtManifestId> assignedDtComponentManifestIds) {
        this.assignedDtComponentManifestIds = assignedDtComponentManifestIds;
    }

    public ReleaseValidationResponse validate() {
        loadManifests();

        ReleaseValidationResponse response = new ReleaseValidationResponse();

        validateAcc(response);
        validateAsccp(response);
        validateBccp(response);
        validateCodeList(response);
        validateAgencyIdList(response);
        validateDt(response);
        validateDependencies(response);

        return response;
    }

    /**
     * What the components depend on, as far as it is not only about the namespace. Some of it is in a release the library
     * depends on, so the components of those releases are read as well.
     */
    private void validateDependencies(ReleaseValidationResponse response) {
        var releaseQuery = repositoryFactory.releaseQueryRepository(requester);
        ReleaseSummaryRecord workingRelease = releaseQuery.getReleaseSummary(
                releaseQuery.getReleaseSummary(releaseId).libraryId(), "Working");
        ReleaseId workingReleaseId = workingRelease.releaseId();
        List<ReleaseSummaryRecord> dependencyReleases = releaseQuery.getIncludedReleaseSummaryList(workingReleaseId).stream()
                .filter(e -> !e.releaseId().equals(workingReleaseId))
                .toList();
        List<ReleaseId> dependencyReleaseIds = dependencyReleases.stream().map(ReleaseSummaryRecord::releaseId).toList();
        List<ReleaseId> workingAndDependencyReleaseIds = new ArrayList<>(dependencyReleaseIds);
        workingAndDependencyReleaseIds.add(workingReleaseId);

        var accQuery = repositoryFactory.accQueryRepository(requester);
        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);
        var xbtQuery = repositoryFactory.xbtQueryRepository(requester);

        var dependencies = ReleaseDependencyValidator.Dependencies.none();
        if (!dependencyReleaseIds.isEmpty()) {
            // A release is stable when it is published: the 'Working' release, or one that is not published yet, can change.
            Map<ReleaseId, Boolean> stable = dependencyReleases.stream().collect(Collectors.toMap(
                    ReleaseSummaryRecord::releaseId,
                    e -> e.state() == ReleaseState.Published && !"Working".equals(e.releaseNum())));
            dependencies = new ReleaseDependencyValidator.Dependencies(
                    locate(accQuery.getAccSummaryList(dependencyReleaseIds), AccSummaryRecord::accManifestId,
                            e -> stable.get(e.release().releaseId())),
                    locate(asccpQuery.getAsccpSummaryList(dependencyReleaseIds), AsccpSummaryRecord::asccpManifestId,
                            e -> stable.get(e.release().releaseId())),
                    locate(bccpQuery.getBccpSummaryList(dependencyReleaseIds), BccpSummaryRecord::bccpManifestId,
                            e -> stable.get(e.release().releaseId())),
                    locate(dtQuery.getDtSummaryList(dependencyReleaseIds), DtSummaryRecord::dtManifestId,
                            e -> stable.get(e.release().releaseId())),
                    locateByRelease(dependencyReleaseIds, stable, codeListQuery::getCodeListSummaryList,
                            CodeListSummaryRecord::codeListManifestId),
                    locateByRelease(dependencyReleaseIds, stable, agencyIdListQuery::getAgencyIdListSummaryList,
                            AgencyIdListSummaryRecord::agencyIdListManifestId));
        }

        var data = new ReleaseDependencyValidator.Data(
                accList, asccList, bccList, asccpList, bccpList, dtList,
                dtQuery.getDtAwdPriSummaryList(workingReleaseId),
                dtQuery.getDtScSummaryList(List.of(workingReleaseId)),
                dtQuery.getDtScAwdPriSummaryList(workingReleaseId),
                codeListList, agencyIdListList,
                xbtQuery.getXbtSummaryList(workingAndDependencyReleaseIds).stream()
                        .collect(Collectors.toMap(XbtSummaryRecord::xbtManifestId, Function.identity())),
                xbtQuery.getXbtSummaryList(workingReleaseId).stream()
                        .map(XbtSummaryRecord::xbtId).collect(Collectors.toSet()),
                dependencies,
                new ReleaseDependencyValidator.Assigned(
                        assignedAccComponentManifestIds, assignedAsccpComponentManifestIds,
                        assignedBccpComponentManifestIds, assignedDtComponentManifestIds,
                        assignedCodeListComponentManifestIds, assignedAgencyIdListComponentManifestIds));
        new ReleaseDependencyValidator(data, response).validate();
    }

    private static <I, T> Map<I, ReleaseDependencyValidator.Located<T>> locate(
            List<T> components, Function<T, I> id, Function<T, Boolean> stable) {
        return components.stream().collect(Collectors.toMap(id,
                e -> new ReleaseDependencyValidator.Located<>(e, stable.apply(e))));
    }

    /** For the components that do not say which release they are in: ask release by release. */
    private static <I, T> Map<I, ReleaseDependencyValidator.Located<T>> locateByRelease(
            List<ReleaseId> releaseIds, Map<ReleaseId, Boolean> stable,
            Function<ReleaseId, List<T>> query, Function<T, I> id) {
        Map<I, ReleaseDependencyValidator.Located<T>> result = new java.util.HashMap<>();
        for (ReleaseId releaseId : releaseIds) {
            for (T component : query.apply(releaseId)) {
                result.put(id.apply(component), new ReleaseDependencyValidator.Located<>(component, stable.get(releaseId)));
            }
        }
        return result;
    }

    private void loadManifests() {
        ReleaseSummaryRecord release = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(releaseId);

        var accQuery = repositoryFactory.accQueryRepository(requester);
        accList = accQuery.getAccSummaryList(release.libraryId(), "Working", null);
        accMap = accList.stream()
                .collect(Collectors.toMap(e -> e.accManifestId(), Function.identity()));

        asccList = accQuery.getAsccSummaryList(release.libraryId(), "Working", null);
        bccList = accQuery.getBccSummaryList(release.libraryId(), "Working", null);

        asccpList = repositoryFactory.asccpQueryRepository(requester)
                .getAsccpSummaryList(release.libraryId(), "Working", null);
        asccpMap = asccpList.stream()
                .collect(Collectors.toMap(e -> e.asccpManifestId(), Function.identity()));

        bccpList = repositoryFactory.bccpQueryRepository(requester)
                .getBccpSummaryList(release.libraryId(), "Working", null);
        bccpMap = bccpList.stream()
                .collect(Collectors.toMap(e -> e.bccpManifestId(), Function.identity()));

        codeListList = repositoryFactory.codeListQueryRepository(requester)
                .getCodeListSummaryList(release.libraryId(), "Working", null);

        agencyIdListList = repositoryFactory.agencyIdListQueryRepository(requester)
                .getAgencyIdListSummaryList(release.libraryId(), "Working", null);

        dtList = repositoryFactory.dtQueryRepository(requester)
                .getDtSummaryList(release.libraryId(), "Working", null);
        dtMap = dtList.stream()
                .collect(Collectors.toMap(e -> e.dtManifestId(), Function.identity()));
    }

    private void validateAcc(ReleaseValidationResponse response) {
        for (AccSummaryRecord acc : accList) {
            if (acc.state() == CcState.Published || assignedAccComponentManifestIds.contains(acc.accManifestId())) {
                if (acc.namespaceId() == null) {
                    response.addMessageForAcc(acc.accManifestId(),
                            Error, "Namespace is required.", NAMESPACE);
                }
            }
        }
    }

    private void validateAsccp(ReleaseValidationResponse response) {
        for (AsccpSummaryRecord asccp : asccpList) {
            if (asccp.state() == CcState.Published || assignedAsccpComponentManifestIds.contains(asccp.asccpManifestId())) {
                if (asccp.namespaceId() == null) {
                    response.addMessageForAsccp(asccp.asccpManifestId(),
                            Error, "Namespace is required.", NAMESPACE);
                }
            }
        }
    }

    private void validateBccp(ReleaseValidationResponse response) {
        for (BccpSummaryRecord bccp : bccpList) {
            if (bccp.state() == CcState.Published || assignedBccpComponentManifestIds.contains(bccp.bccpManifestId())) {
                if (bccp.namespaceId() == null) {
                    response.addMessageForBccp(bccp.bccpManifestId(),
                            Error, "Namespace is required.", NAMESPACE);
                }
            }
        }
    }

    private void validateCodeList(ReleaseValidationResponse response) {
        for (CodeListSummaryRecord codeList : codeListList) {
            CcState state = codeList.state();
            if (state != CcState.Published && !assignedCodeListComponentManifestIds.contains(
                    codeList.codeListManifestId())) {
                continue;
            }
            if (codeList.namespaceId() == null) {
                response.addMessageForCodeList(codeList.codeListManifestId(),
                        Error, "Namespace is required.", NAMESPACE);
            }
        }
    }

    private void validateAgencyIdList(ReleaseValidationResponse response) {
        for (AgencyIdListSummaryRecord agencyIdList : agencyIdListList) {
            CcState state = agencyIdList.state();
            if (state != CcState.Published && !assignedAgencyIdListComponentManifestIds.contains(
                    agencyIdList.agencyIdListManifestId())) {
                continue;
            }
            if (agencyIdList.namespaceId() == null) {
                response.addMessageForAgencyIdList(agencyIdList.agencyIdListManifestId(),
                        Error, "Namespace is required.", NAMESPACE);
            }
        }
    }

    private void validateDt(ReleaseValidationResponse response) {
        for (DtSummaryRecord dt : dtList) {
            CcState state = dt.state();
            if (state != CcState.Published && !assignedDtComponentManifestIds.contains(
                    dt.dtManifestId())) {
                continue;
            }
            if (dt.namespaceId() == null) {
                response.addMessageForDt(dt.dtManifestId(),
                        Error, "Namespace is required.", NAMESPACE);
            }
        }
    }
}
