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
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

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

        return response;
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
            CcState state = acc.state();
            if (state != CcState.Published && !assignedAccComponentManifestIds.contains(acc.accManifestId())) {
                continue;
            }
            if (acc.namespaceId() == null) {
                response.addMessageForAcc(acc.accManifestId(),
                        Error, "Namespace is required.", NAMESPACE);
            }

            // check ACCs whose `basedACC` is this acc.
            accList.stream().filter(e -> e.accManifestId().equals(acc.basedAccManifestId()))
                    .forEach(basedAcc -> {
                        CcState basedAccState = basedAcc.state();
                        if (basedAccState != CcState.Published) {
                            if (assignedAccComponentManifestIds.contains(basedAcc.accManifestId())) {
                                if (basedAccState != CcState.Candidate) {
                                    response.addMessageForAcc(basedAcc.accManifestId(),
                                            Error, "'" + basedAcc.den() + "' should be in '" + CcState.Candidate + "'.",
                                            ACC_BasedACC);
                                }
                            } else {
                                if (basedAcc.prevAccManifestId() == null) {
                                    response.addMessageForAcc(basedAcc.accManifestId(),
                                            Error, "'" + basedAcc.den() + "' is needed in the release assignment due to '" + acc.den() + "'.",
                                            ACC_BasedACC);
                                } else {
                                    response.addMessageForAcc(basedAcc.accManifestId(),
                                            Warning, "'" + basedAcc.den() + "' has been revised but not included in the release assignment.",
                                            ACC_BasedACC);
                                }
                            }
                        }
                    });

            // check ASCCs
            asccList.stream().filter(e -> e.fromAccManifestId().equals(acc.accManifestId()))
                    .forEach(ascc -> {
                        AsccpSummaryRecord asccp = asccpMap.get(ascc.toAsccpManifestId());
                        CcState asccpState = asccp.state();
                        if (asccpState != CcState.Published) {
                            if (assignedAsccpComponentManifestIds.contains(asccp.asccpManifestId())) {
                                if (asccpState != CcState.Candidate) {
                                    response.addMessageForAsccp(asccp.asccpManifestId(),
                                            Error, "'" + asccp.den() + "' should be in '" + CcState.Candidate + "'.",
                                            ACC_Association);
                                }
                            } else {
                                if (asccp.prevAsccpManifestId() == null) {
                                    response.addMessageForAsccp(asccp.asccpManifestId(),
                                            Error, "'" + asccp.den() + "' is needed in the release assignment due to '" + acc.den() + "'.",
                                            ACC_Association);
                                } else {
                                    response.addMessageForAsccp(asccp.asccpManifestId(),
                                            Warning, "'" + asccp.den() + "' has been revised but not included in the release assignment.",
                                            ACC_Association);
                                }
                            }
                        }
                    });

            // check BCCs
            bccList.stream().filter(e -> e.fromAccManifestId().equals(acc.accManifestId()))
                    .forEach(bcc -> {
                        BccpSummaryRecord bccp = bccpMap.get(bcc.toBccpManifestId());
                        CcState bccpState = bccp.state();
                        if (bccpState != CcState.Published) {
                            if (assignedBccpComponentManifestIds.contains(bccp.bccpManifestId())) {
                                if (bccpState != CcState.Candidate) {
                                    response.addMessageForBccp(bccp.bccpManifestId(),
                                            Error, "'" + bccp.den() + "' should be in '" + CcState.Candidate + "'.",
                                            ACC_Association);
                                }
                            } else {
                                if (bccp.prevBccpManifestId() == null) {
                                    response.addMessageForBccp(bccp.bccpManifestId(),
                                            Error, "'" + bccp.den() + "' is needed in the release assignment due to '" + acc.den() + "'.",
                                            ACC_Association);
                                } else {
                                    response.addMessageForBccp(bccp.bccpManifestId(),
                                            Warning, "'" + bccp.den() + "' has been revised but not included in the release assignment.",
                                            ACC_Association);
                                }
                            }
                        }
                    });
        }
    }

    private void validateAsccp(ReleaseValidationResponse response) {
        for (AsccpSummaryRecord asccp : asccpList) {
            CcState state = asccp.state();
            if (state != CcState.Published && !assignedAsccpComponentManifestIds.contains(asccp.asccpManifestId())) {
                continue;
            }
            if (asccp.namespaceId() == null) {
                response.addMessageForAsccp(asccp.asccpManifestId(),
                        Error, "Namespace is required.", NAMESPACE);
            }

            // Check ASCCP.ROLE_OF_ACC
            AccSummaryRecord acc = accMap.get(asccp.roleOfAccManifestId());
            if (acc == null) {
                continue;
            }
            CcState accState = acc.state();
            if (accState != CcState.Published) {
                if (assignedAccComponentManifestIds.contains(acc.accManifestId())) {
                    if (accState != CcState.Candidate) {
                        response.addMessageForAcc(acc.accManifestId(),
                                Error, "'" + acc.den() + "' should be in '" + CcState.Candidate + "'.",
                                ASCCP_RoleOfAcc);
                    }
                } else {
                    if (acc.prevAccManifestId() == null) {
                        response.addMessageForAcc(acc.accManifestId(),
                                Error, "'" + acc.den() + "' is needed in the release assignment due to '" + asccp.den() + "'.",
                                ASCCP_RoleOfAcc);
                    } else {
                        response.addMessageForAcc(acc.accManifestId(),
                                Warning, "'" + acc.den() + "' has been revised but not included in the release assignment.",
                                ASCCP_RoleOfAcc);
                    }
                }
            }
        }
    }

    private void validateBccp(ReleaseValidationResponse response) {
        for (BccpSummaryRecord bccp : bccpList) {
            CcState state = bccp.state();
            if (state != CcState.Published && !assignedBccpComponentManifestIds.contains(
                    bccp.bccpManifestId())) {
                continue;
            }
            if (bccp.namespaceId() == null) {
                response.addMessageForBccp(bccp.bccpManifestId(),
                        Error, "Namespace is required.", NAMESPACE);
            }

            // Check BCCP.BDT
            DtSummaryRecord dt = dtMap.get(bccp.dtManifestId());
            if (dt == null) {
                continue;
            }
            CcState dtState = dt.state();
            if (dtState != CcState.Published) {
                if (assignedDtComponentManifestIds.contains(dt.dtManifestId())) {
                    if (dtState != CcState.Candidate) {
                        response.addMessageForDt(dt.dtManifestId(),
                                Error, "'" + dt.den() + "' should be in '" + CcState.Candidate + "'.",
                                BCCP_BDT);
                    }
                } else {
                    if (dt.prevDtManifestId() == null) {
                        response.addMessageForDt(dt.dtManifestId(),
                                Error, "'" + dt.den() + "' is needed in the release assignment due to '" + bccp.den() + "'.",
                                BCCP_BDT);
                    } else {
                        response.addMessageForDt(dt.dtManifestId(),
                                Warning, "'" + dt.den() + "' has been revised but not included in the release assignment.",
                                BCCP_BDT);
                    }
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
