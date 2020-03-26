package org.oagi.srt.gateway.http.api.cc_management.repository;

import org.oagi.srt.data.*;
import org.oagi.srt.gateway.http.api.cc_management.data.CcList;
import org.oagi.srt.gateway.http.api.cc_management.data.CcListRequest;
import org.oagi.srt.gateway.http.api.cc_management.data.CcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.srt.gateway.http.api.cc_management.helper.CcUtility.getLatestEntity;
import static org.oagi.srt.gateway.http.api.cc_management.helper.CcUtility.getRevision;
import static org.oagi.srt.gateway.http.helper.filter.ContainsFilterBuilder.contains;

@Repository
public class CcListRepository {

    @Autowired
    private CoreComponentRepository coreComponentRepository;

    public List<CcList> getAccList(CcListRequest request) {
        if (!request.getTypes().isAcc()) {
            return Collections.emptyList();
        }

        Map<String, List<ACC>> accList = coreComponentRepository.getAccList()
                .stream().collect(groupingBy(CoreComponent::getGuid));

        long releaseId = request.getReleaseId();
        Map<Long, String> usernameMap = request.getUsernameMap();
        return accList.entrySet().stream()
                .map(entry -> getLatestEntity(releaseId, entry.getValue()))
                .filter(item -> item != null)
                .filter(e -> releaseId > 0 || !e.getOagisComponentType().equals(OagisComponentType.UserExtensionGroup.getValue()))
                .filter(e -> request.getDeprecated() == null || request.getDeprecated() == e.isDeprecated())
                .filter(e -> request.getStates().isEmpty() || request.getStates().contains(CcState.valueOf(e.getState())))
                .filter(e -> request.getOwnerLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getOwnerUserId())))
                .filter(e -> request.getUpdaterLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getLastUpdatedBy())))
                .filter(contains(request.getDen(), ACC::getDen))
                .filter(contains(request.getDefinition(), ACC::getDefinition))
                .filter(contains(request.getModule(), ACC::getModule))
                .filter(e -> {
                    Date start = request.getUpdateStartDate();
                    if (start != null) {
                        if (e.getLastUpdateTimestamp().getTime() < start.getTime()) {
                            return false;
                        }
                    }

                    Date end = request.getUpdateEndDate();
                    if (end != null) {
                        return e.getLastUpdateTimestamp().getTime() <= end.getTime();
                    }

                    return true;
                })
                .map(acc -> {
                    OagisComponentType oagisComponentType = OagisComponentType.valueOf(acc.getOagisComponentType());
                    CcList ccList = new CcList();
                    ccList.setType("ACC");
                    ccList.setId(acc.getAccId());
                    ccList.setGuid(acc.getGuid());
                    ccList.setDen(acc.getDen());
                    ccList.setDefinition(acc.getDefinition());
                    ccList.setDefinitionSource(acc.getDefinitionSource());
                    ccList.setModule(acc.getModule());
                    ccList.setOagisComponentType(oagisComponentType);
                    ccList.setState(CcState.valueOf(acc.getState()));
                    ccList.setDeprecated(acc.isDeprecated());
                    ccList.setCurrentId(acc.getCurrentAccId());
                    ccList.setLastUpdateTimestamp(acc.getLastUpdateTimestamp());
                    ccList.setRevision(getRevision(releaseId, accList.getOrDefault(acc.getGuid(), Collections.emptyList())));
                    ccList.setOwner(usernameMap.get(acc.getOwnerUserId()));
                    ccList.setLastUpdateUser(usernameMap.get(acc.getLastUpdatedBy()));

                    return ccList;
                })
                .collect(Collectors.toList());
    }

    public List<CcList> getAsccList(CcListRequest request) {
        if (!request.getTypes().isAscc() || !StringUtils.isEmpty(request.getModule())) {
            return Collections.emptyList();
        }

        Map<String, List<ASCC>> asccList = coreComponentRepository.getAsccList()
                .stream().collect(groupingBy(CoreComponent::getGuid));

        long releaseId = request.getReleaseId();
        Map<Long, String> usernameMap = request.getUsernameMap();
        return asccList.entrySet().stream()
                .map(entry -> getLatestEntity(releaseId, entry.getValue()))
                .filter(e -> e != null)
                .filter(e -> !e.getDen().contains("User Extension Group"))
                .filter(e -> request.getDeprecated() == null || request.getDeprecated() == e.isDeprecated())
                .filter(e -> request.getStates().isEmpty() || request.getStates().contains(CcState.valueOf(e.getState())))
                .filter(e -> request.getOwnerLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getOwnerUserId())))
                .filter(e -> request.getUpdaterLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getLastUpdatedBy())))
                .filter(contains(request.getDen(), ASCC::getDen))
                .filter(contains(request.getDefinition(), ASCC::getDefinition))
                .filter(e -> {
                    Date start = request.getUpdateStartDate();
                    if (start != null) {
                        if (e.getLastUpdateTimestamp().getTime() < start.getTime()) {
                            return false;
                        }
                    }

                    Date end = request.getUpdateEndDate();
                    if (end != null) {
                        return e.getLastUpdateTimestamp().getTime() <= end.getTime();
                    }

                    return true;
                })
                .map(ascc -> {
                    CcList ccList = new CcList();
                    ccList.setType("ASCC");
                    ccList.setId(ascc.getAsccId());
                    ccList.setGuid(ascc.getGuid());
                    ccList.setDen(ascc.getDen());
                    ccList.setDefinition(ascc.getDefinition());
                    ccList.setDefinitionSource(ascc.getDefinitionSource());
                    ccList.setState(CcState.valueOf(ascc.getState()));
                    ccList.setDeprecated(ascc.isDeprecated());
                    ccList.setCurrentId(ascc.getCurrentAsccId());
                    ccList.setLastUpdateTimestamp(ascc.getLastUpdateTimestamp());
                    ccList.setRevision(getRevision(releaseId, asccList.getOrDefault(ascc.getGuid(), Collections.emptyList())));
                    ccList.setOwner(usernameMap.get(ascc.getOwnerUserId()));
                    ccList.setLastUpdateUser(usernameMap.get(ascc.getLastUpdatedBy()));

                    return ccList;
                })
                .collect(Collectors.toList());
    }

    public List<CcList> getBccList(CcListRequest request) {
        if (!request.getTypes().isBcc() || !StringUtils.isEmpty(request.getModule())) {
            return Collections.emptyList();
        }

        Map<String, List<BCC>> bccList = coreComponentRepository.getBccList()
                .stream().collect(groupingBy(CoreComponent::getGuid));

        long releaseId = request.getReleaseId();
        Map<Long, String> usernameMap = request.getUsernameMap();
        return bccList.entrySet().stream()
                .map(entry -> getLatestEntity(releaseId, entry.getValue()))
                .filter(e -> e != null)
                .filter(e -> !e.getDen().contains("User Extension Group"))
                .filter(e -> request.getDeprecated() == null || request.getDeprecated() == e.isDeprecated())
                .filter(e -> request.getStates().isEmpty() || request.getStates().contains(CcState.valueOf(e.getState())))
                .filter(e -> request.getOwnerLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getOwnerUserId())))
                .filter(e -> request.getUpdaterLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getLastUpdatedBy())))
                .filter(contains(request.getDen(), BCC::getDen))
                .filter(contains(request.getDefinition(), BCC::getDefinition))
                .filter(e -> {
                    Date start = request.getUpdateStartDate();
                    if (start != null) {
                        if (e.getLastUpdateTimestamp().getTime() < start.getTime()) {
                            return false;
                        }
                    }

                    Date end = request.getUpdateEndDate();
                    if (end != null) {
                        return e.getLastUpdateTimestamp().getTime() <= end.getTime();
                    }

                    return true;
                })
                .map(bcc -> {
                    CcList ccList = new CcList();
                    ccList.setType("BCC");
                    ccList.setId(bcc.getBccId());
                    ccList.setGuid(bcc.getGuid());
                    ccList.setDen(bcc.getDen());
                    ccList.setDefinition(bcc.getDefinition());
                    ccList.setDefinitionSource(bcc.getDefinitionSource());
                    ccList.setState(CcState.valueOf(bcc.getState()));
                    ccList.setDeprecated(bcc.isDeprecated());
                    ccList.setCurrentId(bcc.getCurrentBccId());
                    ccList.setLastUpdateTimestamp(bcc.getLastUpdateTimestamp());
                    ccList.setRevision(getRevision(releaseId, bccList.getOrDefault(bcc.getGuid(), Collections.emptyList())));
                    ccList.setOwner(usernameMap.get(bcc.getOwnerUserId()));
                    ccList.setLastUpdateUser(usernameMap.get(bcc.getLastUpdatedBy()));

                    return ccList;
                })
                .collect(Collectors.toList());
    }

    public List<CcList> getAsccpList(CcListRequest request) {
        if (!request.getTypes().isAsccp()) {
            return Collections.emptyList();
        }

        Map<String, List<ASCCP>> asccpList = coreComponentRepository.getAsccpList()
                .stream().collect(groupingBy(CoreComponent::getGuid));

        long releaseId = request.getReleaseId();
        Map<Long, String> usernameMap = request.getUsernameMap();
        return asccpList.entrySet().stream()
                .map(entry -> getLatestEntity(releaseId, entry.getValue()))
                .filter(e -> e != null)
                .filter(e -> !e.getDen().contains("User Extension Group"))
                .filter(e -> request.getDeprecated() == null || request.getDeprecated() == e.isDeprecated())
                .filter(e -> request.getStates().isEmpty() || request.getStates().contains(CcState.valueOf(e.getState())))
                .filter(e -> request.getOwnerLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getOwnerUserId())))
                .filter(e -> request.getUpdaterLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getLastUpdatedBy())))
                .filter(contains(request.getDen(), ASCCP::getDen))
                .filter(contains(request.getDefinition(), ASCCP::getDefinition))
                .filter(contains(request.getModule(), ASCCP::getModule))
                .filter(e -> {
                    Date start = request.getUpdateStartDate();
                    if (start != null) {
                        if (e.getLastUpdateTimestamp().getTime() < start.getTime()) {
                            return false;
                        }
                    }

                    Date end = request.getUpdateEndDate();
                    if (end != null) {
                        return e.getLastUpdateTimestamp().getTime() <= end.getTime();
                    }

                    return true;
                })
                .map(asccp -> {
                    CcList ccList = new CcList();
                    ccList.setType("ASCCP");
                    ccList.setId(asccp.getAsccpId());
                    ccList.setGuid(asccp.getGuid());
                    ccList.setDen(asccp.getDen());
                    ccList.setDefinition(asccp.getDefinition());
                    ccList.setDefinitionSource(asccp.getDefinitionSource());
                    ccList.setModule(asccp.getModule());
                    ccList.setState(CcState.valueOf(asccp.getState()));
                    ccList.setDeprecated(asccp.isDeprecated());
                    ccList.setCurrentId(asccp.getCurrentAsccpId());
                    ccList.setLastUpdateTimestamp(asccp.getLastUpdateTimestamp());
                    ccList.setRevision(getRevision(releaseId, asccpList.getOrDefault(asccp.getGuid(), Collections.emptyList())));
                    ccList.setOwner(usernameMap.get(asccp.getOwnerUserId()));
                    ccList.setLastUpdateUser(usernameMap.get(asccp.getLastUpdatedBy()));

                    return ccList;
                })
                .collect(Collectors.toList());
    }

    public List<CcList> getBccpList(CcListRequest request) {
        if (!request.getTypes().isBccp()) {
            return Collections.emptyList();
        }

        Map<String, List<BCCP>> bccpList = coreComponentRepository.getBccpList()
                .stream().collect(groupingBy(CoreComponent::getGuid));

        long releaseId = request.getReleaseId();
        Map<Long, String> usernameMap = request.getUsernameMap();
        return bccpList.entrySet().stream()
                .map(entry -> getLatestEntity(releaseId, entry.getValue()))
                .filter(item -> item != null)
                .filter(e -> !e.getDen().contains("User Extension Group"))
                .filter(e -> request.getDeprecated() == null || request.getDeprecated() == e.isDeprecated())
                .filter(e -> request.getStates().isEmpty() || request.getStates().contains(CcState.valueOf(e.getState())))
                .filter(e -> request.getOwnerLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getOwnerUserId())))
                .filter(e -> request.getUpdaterLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getLastUpdatedBy())))
                .filter(contains(request.getDen(), BCCP::getDen))
                .filter(contains(request.getDefinition(), BCCP::getDefinition))
                .filter(contains(request.getModule(), BCCP::getModule))
                .filter(e -> {
                    Date start = request.getUpdateStartDate();
                    if (start != null) {
                        if (e.getLastUpdateTimestamp().getTime() < start.getTime()) {
                            return false;
                        }
                    }

                    Date end = request.getUpdateEndDate();
                    if (end != null) {
                        return e.getLastUpdateTimestamp().getTime() <= end.getTime();
                    }

                    return true;
                })
                .map(bccp -> {
                    CcList ccList = new CcList();
                    ccList.setType("BCCP");
                    ccList.setId(bccp.getBccpId());
                    ccList.setGuid(bccp.getGuid());
                    ccList.setDen(bccp.getDen());
                    ccList.setDefinition(bccp.getDefinition());
                    ccList.setDefinitionSource(bccp.getDefinitionSource());
                    ccList.setModule(bccp.getModule());
                    ccList.setState(CcState.valueOf(bccp.getState()));
                    ccList.setDeprecated(bccp.isDeprecated());
                    ccList.setCurrentId(bccp.getCurrentBccpId());
                    ccList.setLastUpdateTimestamp(bccp.getLastUpdateTimestamp());
                    ccList.setRevision(getRevision(releaseId, bccpList.getOrDefault(bccp.getGuid(), Collections.emptyList())));
                    ccList.setOwner(usernameMap.get(bccp.getOwnerUserId()));
                    ccList.setLastUpdateUser(usernameMap.get(bccp.getLastUpdatedBy()));

                    return ccList;
                })
                .collect(Collectors.toList());
    }

    public List<CcList> getBdtList(CcListRequest request) {
        if (!request.getTypes().isBdt()) {
            return Collections.emptyList();
        }
        Map<String, List<DT>> bdtList = coreComponentRepository.getBdtList()
                .stream().collect(groupingBy(CoreComponent::getGuid));

        long releaseId = request.getReleaseId();
        Map<Long, String> usernameMap = request.getUsernameMap();

        return bdtList.entrySet().stream()
                .map(entry -> getLatestEntity(releaseId, entry.getValue()))
                .filter(item -> item != null)
                .filter(e -> request.getDeprecated() == null || request.getDeprecated() == e.isDeprecated())
                .filter(e -> request.getStates().isEmpty() || request.getStates().contains(CcState.valueOf(e.getState())))
                .filter(e -> request.getOwnerLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getOwnerUserId())))
                .filter(e -> request.getUpdaterLoginIds().isEmpty() || request.getOwnerLoginIds().contains(usernameMap.get(e.getLastUpdatedBy())))
                .filter(contains(request.getDen(), DT::getDen))
                .filter(contains(request.getDefinition(), DT::getDefinition))
                .filter(e -> {
                    Date start = request.getUpdateStartDate();
                    if (start != null) {
                        if (e.getLastUpdateTimestamp().getTime() < start.getTime()) {
                            return false;
                        }
                    }

                    Date end = request.getUpdateEndDate();
                    if (end != null) {
                        return e.getLastUpdateTimestamp().getTime() <= end.getTime();
                    }

                    return true;
                })
                .map(bdt -> {
                    CcList ccList = new CcList();
                    ccList.setType("BDT");
                    ccList.setId(bdt.getDtId());
                    ccList.setGuid(bdt.getGuid());
                    ccList.setDen(bdt.getDen());
                    ccList.setDefinition(bdt.getDefinition());
                    ccList.setDefinitionSource(bdt.getDefinitionSource());
                    ccList.setState(CcState.valueOf(bdt.getState()));
                    ccList.setDeprecated(bdt.isDeprecated());
                    ccList.setLastUpdateTimestamp(bdt.getLastUpdateTimestamp());
                    ccList.setRevision(getRevision(releaseId, bdtList.getOrDefault(bdt.getGuid(), Collections.emptyList())));
                    ccList.setOwner(usernameMap.get(bdt.getOwnerUserId()));
                    ccList.setLastUpdateUser(usernameMap.get(bdt.getLastUpdatedBy()));
                    return ccList;
                })
                .collect(Collectors.toList());
    }
}
