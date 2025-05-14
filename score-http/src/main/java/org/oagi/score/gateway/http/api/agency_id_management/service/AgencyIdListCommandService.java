package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.CreateAgencyIdListRequest;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.UpdateAgencyIdListRequest;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.UpdateAgencyIdListValueRequest;
import org.oagi.score.gateway.http.api.agency_id_management.model.*;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.log_management.model.LogAction;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Deleted;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.WIP;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.*;

@Service
@Transactional
public class AgencyIdListCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private AgencyIdListCommandRepository command(ScoreUser requester) {
        return repositoryFactory.agencyIdListCommandRepository(requester);
    }

    private AgencyIdListQueryRepository query(ScoreUser requester) {
        return repositoryFactory.agencyIdListQueryRepository(requester);
    }

    private LogCommandRepository logCommand(ScoreUser requester) {
        return repositoryFactory.logCommandRepository(requester);
    }

    public AgencyIdListManifestId create(ScoreUser requester, CreateAgencyIdListRequest request) {

        var command = command(requester);

        AgencyIdListManifestId agencyIdListManifestId =
                command.create(request.releaseId(), request.basedAgencyIdListManifestId());

        LogId logId = logCommand(requester).create(
                query(requester).getAgencyIdListDetails(agencyIdListManifestId), Added);
        command.updateLogId(agencyIdListManifestId, logId);

        return agencyIdListManifestId;
    }

    public boolean update(ScoreUser requester, UpdateAgencyIdListRequest request) {

        var query = query(requester);
        AgencyIdListManifestId agencyIdListManifestId = request.agencyIdListManifestId();

        AgencyIdListDetailsRecord agencyIdListDetails =
                query.getAgencyIdListDetails(agencyIdListManifestId);

        if (WIP != agencyIdListDetails.state()) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!agencyIdListDetails.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = command(requester);

        // Update the main agency ID list details
        boolean updated = command.update(
                agencyIdListManifestId,
                request.name(),
                request.versionId(), request.listId(),
                request.agencyIdListValueManifestId(),
                request.definition(), request.remark(),
                request.namespaceId(),
                request.deprecated()
        );

        if (updated) {

            // Fetch existing values as a map
            Map<AgencyIdListValueManifestId, AgencyIdListValueDetailsRecord> existingValues = query
                    .getAgencyIdListValueDetailsList(agencyIdListManifestId)
                    .stream()
                    .collect(Collectors.toMap(AgencyIdListValueDetailsRecord::agencyIdListValueManifestId, Function.identity()));


            // Issue #1647
            // Only one 'developer default' and one 'user default' are allowed.
            UpdateAgencyIdListValueRequest developerDefaultValueRequest =
                    request.valueList().stream()
                            .filter(e -> e.developerDefault() != null && e.developerDefault())
                            .findAny().orElse(null);
            UpdateAgencyIdListValueRequest userDefaultValueRequest =
                    request.valueList().stream()
                            .filter(e -> e.userDefault() != null && e.userDefault())
                            .findAny().orElse(null);

            // Process updates and additions of agency ID list values
            request.valueList().forEach(value -> {
                AgencyIdListValueManifestId valueId = value.agencyIdListValueManifestId();
                if (existingValues.containsKey(valueId)) {
                    command.updateValue(valueId,
                            value.value(), value.name(),
                            value.definition(),
                            value.deprecated(),
                            (developerDefaultValueRequest != null && developerDefaultValueRequest.agencyIdListValueManifestId().equals(value.agencyIdListValueManifestId())) ? value.developerDefault() : null,
                            (userDefaultValueRequest != null && userDefaultValueRequest.agencyIdListValueManifestId().equals(value.agencyIdListValueManifestId())) ? value.userDefault() : null
                    );
                    existingValues.remove(valueId);
                } else {
                    command.createValue(agencyIdListManifestId,
                            agencyIdListDetails.agencyIdListId(),
                            agencyIdListDetails.release().releaseId(),
                            value.value(), value.name(),
                            value.definition());
                }
            });

            // Remove any remaining (deleted) values
            existingValues.keySet().forEach(command::deleteValue);
        }

        LogId logId = logCommand(requester).create(
                query.getAgencyIdListDetails(agencyIdListManifestId), Modified);
        command.updateLogId(agencyIdListManifestId, logId);

        return updated;
    }

    public boolean updateState(ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId, CcState nextState) {

        var query = query(requester);

        AgencyIdListSummaryRecord agencyIdListSummary =
                query.getAgencyIdListSummary(agencyIdListManifestId);

        CcState prevState = agencyIdListSummary.state();
        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        if (prevState == Deleted) {
            boolean isOwnerDeveloper = agencyIdListSummary.owner().isDeveloper();
            boolean isRequesterDeveloper = requester.isDeveloper();

            if (isOwnerDeveloper != isRequesterDeveloper) {
                if (isOwnerDeveloper) {
                    throw new IllegalArgumentException("Only developers can restore this component.");
                } else {
                    throw new IllegalArgumentException("Only end-users can restore this component.");
                }
            }

            // Issue #1647
            if (isOwnerDeveloper) {
                int countActiveAgencyIdLists = query.countActiveAgencyIdLists(
                        agencyIdListSummary.agencyIdListManifestId());
                if (countActiveAgencyIdLists > 0) {
                    throw new IllegalArgumentException("Another active Agency ID List has been found. Only one Agency ID List for developers is allowed.");
                }
            }
        } else {
            if (!agencyIdListSummary.owner().userId().equals(requester.userId()) && !prevState.isImplicitMove(nextState)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }
        }

        var command = command(requester);

        boolean result = command.updateState(agencyIdListManifestId, nextState);

        LogAction logAction = (Deleted == prevState && WIP == nextState) ? LogAction.Restored :
                ((Deleted == nextState) ? LogAction.Deleted : LogAction.Modified);
        LogId logId = logCommand(requester).create(
                query.getAgencyIdListDetails(agencyIdListManifestId), logAction);
        command.updateLogId(agencyIdListManifestId, logId);

        return result;
    }

    public void revise(ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId) {

        var query = query(requester);

        AgencyIdListDetailsRecord prevAgencyIdListDetails =
                query.getAgencyIdListDetails(agencyIdListManifestId);

        if (requester.isDeveloper()) {
            if (CcState.Published != prevAgencyIdListDetails.state()) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
            if (!prevAgencyIdListDetails.release().isWorkingRelease()) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (CcState.Production != prevAgencyIdListDetails.state()) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
            if (prevAgencyIdListDetails.release().isWorkingRelease()) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        if (!Objects.equals(prevAgencyIdListDetails.owner().isDeveloper(), requester.isDeveloper())) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        var command = command(requester);

        command.revise(agencyIdListManifestId);

        LogId logId = logCommand(requester).create(
                query.getAgencyIdListDetails(agencyIdListManifestId), Revised);
        command.updateLogId(agencyIdListManifestId, logId);
    }

    public void cancel(ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId) {

        AgencyIdListDetailsRecord agencyIdListDetails =
                query(requester).getAgencyIdListDetails(agencyIdListManifestId);

        if (agencyIdListDetails == null) {
            throw new IllegalArgumentException("Not found a target Agency Id List");
        }
        if (agencyIdListDetails.log().revisionNum() == 1) {
            throw new IllegalArgumentException("Not found previous revision");
        }

        var command = command(requester);

        command.cancel(agencyIdListManifestId);

        LogId logId = logCommand(requester).revertToStableStateByReference(agencyIdListDetails.guid(), CcType.AGENCY_ID_LIST);
        command.updateLogId(agencyIdListManifestId, logId);
    }

    public boolean markAsDeleted(ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId) {

        return updateState(requester, agencyIdListManifestId, Deleted);
    }

    public boolean purge(ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId) {

        AgencyIdListSummaryRecord agencyIdListSummary =
                query(requester).getAgencyIdListSummary(agencyIdListManifestId);

        if (!CcState.Deleted.equals(agencyIdListSummary.state())) {
            throw new IllegalArgumentException("Only the Code List in 'Deleted' state can be deleted.");
        }

        boolean result = command(requester).delete(agencyIdListManifestId);
        if (result) {
            logCommand(requester).deleteByReference(agencyIdListSummary.guid());
        }
        return result;
    }

    public boolean restore(ScoreUser requester, AgencyIdListManifestId agencyIdListManifestId) {

        return updateState(requester, agencyIdListManifestId, WIP);
    }

    public void transferOwnership(
            ScoreUser requester, ScoreUser targetUser, AgencyIdListManifestId agencyIdListManifestId) {

        if (targetUser == null) {
            throw new IllegalArgumentException("You must specify a target user.");
        }

        var query = query(requester);

        AgencyIdListSummaryRecord agencyIdListSummary =
                query.getAgencyIdListSummary(agencyIdListManifestId);

        if (!requester.isAdministrator()) {
            if (!WIP.equals(agencyIdListSummary.state())) {
                throw new IllegalArgumentException("Only the agency ID list in 'WIP' state can be modified.");
            }

            if (!agencyIdListSummary.owner().userId().equals(requester.userId())) {
                throw new IllegalArgumentException("It only allows to modify the agency ID list by the owner.");
            }
        }

        if (agencyIdListSummary.owner().userId().equals(targetUser.userId())) {
            throw new IllegalArgumentException("You already own this agency ID list.");
        }

        var command = command(requester);

        // Perform ownership transfer
        boolean success = command.updateOwnership(targetUser, agencyIdListManifestId);

        if (!success) {
            throw new AccessDeniedException("Ownership transfer failed due to insufficient permissions.");
        }

        LogId logId = logCommand(requester).create(
                query.getAgencyIdListDetails(agencyIdListManifestId), Modified);
        command.updateLogId(agencyIdListManifestId, logId);
    }

}
