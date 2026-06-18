package org.oagi.score.gateway.http.api.code_list_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.service.ComponentStateChangeEventPublisher;
import org.oagi.score.gateway.http.api.bie_management.model.CodeListBieReferenceRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.controller.payload.CodeListUpliftingResponse;
import org.oagi.score.gateway.http.api.code_list_management.controller.payload.CreateCodeListRequest;
import org.oagi.score.gateway.http.api.code_list_management.controller.payload.UpdateCodeListRequest;
import org.oagi.score.gateway.http.api.code_list_management.model.*;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.log_management.model.LogAction;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.cc_management.model.CcState.*;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Deleted;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.*;
import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional
public class CodeListCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ComponentStateChangeEventPublisher stateChangeEventPublisher;

    private CodeListCommandRepository command(ScoreUser requester) {
        return repositoryFactory.codeListCommandRepository(requester);
    }

    private CodeListQueryRepository query(ScoreUser requester) {
        return repositoryFactory.codeListQueryRepository(requester);
    }

    private LogCommandRepository logCommand(ScoreUser requester) {
        return repositoryFactory.logCommandRepository(requester);
    }

    public CodeListManifestId create(ScoreUser requester, CreateCodeListRequest request) {

        var command = command(requester);

        CodeListManifestId codeListManifestId =
                command.create(request.releaseId(), request.basedCodeListManifestId());

        LogId logId = logCommand(requester).create(
                query(requester).getCodeListDetails(codeListManifestId), Added);
        command.updateLogId(codeListManifestId, logId);

        return codeListManifestId;
    }

    public boolean update(ScoreUser requester, UpdateCodeListRequest request) {

        var query = query(requester);
        CodeListManifestId codeListManifestId = request.codeListManifestId();

        CodeListDetailsRecord codeListDetails =
                query.getCodeListDetails(codeListManifestId);

        if (WIP != codeListDetails.state()) {
            throw new IllegalArgumentException("Only the code list in 'WIP' state can be modified.");
        }

        if (!codeListDetails.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the code list by the owner.");
        }

        var command = command(requester);

        // Update the main code list details
        boolean updated = command.update(
                codeListManifestId,
                request.name(),
                request.versionId(), request.listId(),
                request.agencyIdListValueManifestId(),
                request.definition(), request.remark(),
                request.namespaceId(),
                request.deprecated(), request.extensible()
        );

        if (updated) {

            // Fetch existing values as a map
            Map<CodeListValueManifestId, CodeListValueDetailsRecord> existingValues = query
                    .getCodeListValueDetailsList(codeListManifestId)
                    .stream()
                    .collect(Collectors.toMap(CodeListValueDetailsRecord::codeListValueManifestId, Function.identity()));

            // Process updates and additions of code list values
            request.valueList().forEach(value -> {
                CodeListValueManifestId valueId = value.codeListValueManifestId();
                if (existingValues.containsKey(valueId)) {
                    command.updateValue(valueId,
                            value.value(), value.meaning(),
                            value.definition(),
                            value.deprecated()
                    );
                    existingValues.remove(valueId);
                } else {
                    command.createValue(codeListManifestId,
                            codeListDetails.codeListId(),
                            codeListDetails.release().releaseId(),
                            value.value(), value.meaning(),
                            value.definition());
                }
            });

            // Remove any remaining (deleted) values
            existingValues.keySet().forEach(command::deleteValue);
        }

        LogId logId = logCommand(requester).create(
                query.getCodeListDetails(codeListManifestId), Modified);
        command.updateLogId(codeListManifestId, logId);

        return updated;
    }

    public boolean updateState(ScoreUser requester, CodeListManifestId codeListManifestId, CcState nextState) {
        return updateState(requester, codeListManifestId, nextState, null, null);
    }

    public boolean updateState(ScoreUser requester, CodeListManifestId codeListManifestId, CcState nextState, String comment) {
        return updateState(requester, codeListManifestId, nextState, comment, null);
    }

    public boolean updateState(ScoreUser requester, CodeListManifestId codeListManifestId, CcState nextState,
                               String comment, String projectFieldOptionOverride) {

        var query = query(requester);

        CodeListSummaryRecord codeListSummary =
                query.getCodeListSummary(codeListManifestId);

        CcState prevState = codeListSummary.state();
        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The code list in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        if (prevState == Deleted) {
            boolean isOwnerDeveloper = codeListSummary.owner().isDeveloper();
            boolean isRequesterDeveloper = requester.isDeveloper();

            if (isOwnerDeveloper != isRequesterDeveloper) {
                if (isOwnerDeveloper) {
                    throw new IllegalArgumentException("Only developers can restore this component.");
                } else {
                    throw new IllegalArgumentException("Only end-users can restore this component.");
                }
            }
        } else {
            if (!codeListSummary.owner().userId().equals(requester.userId()) && !prevState.isImplicitMove(nextState)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }
        }

        ensureBieDependenciesForChangingState(requester, codeListSummary, nextState);

        var command = command(requester);

        boolean result = command.updateState(codeListManifestId, nextState);

        LogAction logAction = (Deleted == prevState && WIP == nextState) ? LogAction.Restored :
                ((Deleted == nextState) ? LogAction.Deleted : LogAction.Modified);
        LogId logId = logCommand(requester).create(
                query.getCodeListDetails(codeListManifestId), logAction);
        command.updateLogId(codeListManifestId, logId);

        if (result) {
            stateChangeEventPublisher.publish(
                    CcType.CODE_LIST, codeListManifestId, prevState, nextState, requester.userId(), comment, projectFieldOptionOverride);
        }

        return result;
    }

    /**
     * Rejects a code-list state change when any assigned BIE would become
     * incompatible with the requested next code-list state.
     */
    private void ensureBieDependenciesForChangingState(ScoreUser requester,
                                                       CodeListSummaryRecord codeListSummary,
                                                       CcState nextState) {
        if (codeListSummary == null || nextState == null) {
            return;
        }

        List<CodeListBieReferenceRecord> blockingBies = query(requester)
                .getAssignedBieSummaryList(codeListSummary.codeListManifestId()).stream()
                .filter(reference -> reference.topLevelAsbiep() != null)
                .filter(reference -> reference.topLevelAsbiep().state() != null)
                .filter(reference -> !CodeListStateLevel.isCompatible(
                        reference.topLevelAsbiep().state(),
                        nextState,
                        codeListSummary.owner()))
                .sorted(Comparator
                        .comparing((CodeListBieReferenceRecord reference) -> toName(reference.topLevelAsbiep()),
                                Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(reference -> reference.topLevelAsbiep().guid().value()))
                .toList();
        if (blockingBies.isEmpty()) {
            return;
        }

        StringBuilder failureMessageBody = new StringBuilder()
                .append("\n---\n[**")
                .append(codeListSummary.name())
                .append("**](")
                .append("/code_list/").append(codeListSummary.codeListManifestId())
                .append(") cannot move to ")
                .append(nextState)
                .append(" because ")
                .append(toBlockingBieStateMessage(nextState, codeListSummary))
                .append("\n\n");
        for (CodeListBieReferenceRecord reference : blockingBies) {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = reference.topLevelAsbiep();
            failureMessageBody.append("- [")
                    .append(toListItemLabel(topLevelAsbiep))
                    .append("](")
                    .append("/profile_bie/").append(topLevelAsbiep.topLevelAsbiepId())
                    .append(") (")
                    .append(toListItemIdentity(topLevelAsbiep))
                    .append(") - ")
                    .append(topLevelAsbiep.state()).append(" state")
                    .append("\n");
        }

        throw new IllegalArgumentException(toPlainTextStateFailureMessage(
                "Failed to update code list state", failureMessageBody.toString()));
    }

    /**
     * Returns the preferred user-facing BIE name for sorting and simple
     * messages.
     */
    private String toName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (topLevelAsbiep == null) {
            return null;
        }
        return (topLevelAsbiep.displayName() != null && !topLevelAsbiep.displayName().isBlank()) ?
                topLevelAsbiep.displayName() :
                topLevelAsbiep.propertyTerm();
    }

    /**
     * Returns the rich label used in blocking-BIE bullet items.
     *
     * <p>When a display name exists, the property term is preserved as the
     * canonical BIE name and the display name is appended for disambiguation.</p>
     */
    private String toListItemLabel(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (topLevelAsbiep == null) {
            return null;
        }
        if (topLevelAsbiep.displayName() != null && !topLevelAsbiep.displayName().isBlank()) {
            return topLevelAsbiep.propertyTerm() + " (" + topLevelAsbiep.displayName() + ")";
        }
        return topLevelAsbiep.propertyTerm();
    }

    /**
     * Returns the secondary identifier block shown in blocking-BIE list items.
     *
     * <p>The GUID is always included. When the owner login ID is available, it
     * is appended so the user knows who to contact for the required follow-up
     * action.</p>
     */
    private String toListItemIdentity(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (topLevelAsbiep == null || topLevelAsbiep.guid() == null) {
            return null;
        }

        StringBuilder identity = new StringBuilder(topLevelAsbiep.guid().toString());
        if (topLevelAsbiep.owner() != null && hasLength(topLevelAsbiep.owner().loginId())) {
            identity.append(", owner: ").append(topLevelAsbiep.owner().loginId());
        }
        return identity.toString();
    }

    /**
     * Returns the summary sentence fragment used before the blocking BIE list.
     *
     * <p>The wording names the exact BIE state set required by the requested
     * future code-list state whenever that set is expressible.</p>
     */
    private String toBlockingBieStateMessage(CcState nextState, CodeListSummaryRecord codeListSummary) {
        List<org.oagi.score.gateway.http.api.bie_management.model.BieState> compatibleBieStates =
                CodeListStateLevel.compatibleBieStates(nextState, codeListSummary);
        if (compatibleBieStates.size() == 1) {
            return "the following related BIEs are not in '" + compatibleBieStates.get(0) + "' state:";
        }
        if (!compatibleBieStates.isEmpty()) {
            return "the following related BIEs are not in one of these states: " +
                    joinQuotedStates(compatibleBieStates) + ":";
        }
        return "the following related BIEs require a different compatible code list state:";
    }

    /**
     * Joins state names into a natural-language quoted list.
     */
    private String joinQuotedStates(List<?> states) {
        if (states == null || states.isEmpty()) {
            return "";
        }
        if (states.size() == 1) {
            return "'" + states.get(0) + "'";
        }
        if (states.size() == 2) {
            return "'" + states.get(0) + "' or '" + states.get(1) + "'";
        }
        return states.subList(0, states.size() - 1).stream()
                .map(state -> "'" + state + "'")
                .collect(Collectors.joining(", ")) +
                ", or '" + states.get(states.size() - 1) + "'";
    }

    /**
     * Removes markdown-style formatting from multi-line validation messages so
     * they can be safely returned through plain-text exception paths.
     */
    private String toPlainTextStateFailureMessage(String subject, String messageBody) {
        String plainTextMessageBody = messageBody
                .replace("\n---\n", "\n\n")
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\[(.*?)\\]\\([^)]*\\)", "$1");
        return subject + "\n" + plainTextMessageBody.trim();
    }

    public void revise(ScoreUser requester, CodeListManifestId codeListManifestId) {

        var query = query(requester);

        CodeListDetailsRecord prevCodeListDetails =
                query.getCodeListDetails(codeListManifestId);

        if (requester.isDeveloper()) {
            if (Published != prevCodeListDetails.state()) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
            if (!prevCodeListDetails.release().isWorkingRelease()) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (Production != prevCodeListDetails.state()) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
            if (prevCodeListDetails.release().isWorkingRelease()) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        if (!Objects.equals(prevCodeListDetails.owner().isDeveloper(), requester.isDeveloper())) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        var command = command(requester);
        command.revise(codeListManifestId);

        LogId logId = logCommand(requester).create(
                query.getCodeListDetails(codeListManifestId), Revised);
        command.updateLogId(codeListManifestId, logId);
        // Publish the revise (released -> WIP) so the GitHub project fieldOption sync moves the linked issue
        // back into the in-progress fieldOption (issue #1533).
        stateChangeEventPublisher.publish(
                CcType.CODE_LIST, codeListManifestId, prevCodeListDetails.state(), WIP, requester.userId());
    }

    public void cancel(ScoreUser requester, CodeListManifestId codeListManifestId, String comment, String projectFieldOptionOverride) {

        CodeListDetailsRecord codeListDetails =
                query(requester).getCodeListDetails(codeListManifestId);

        if (codeListDetails == null) {
            throw new IllegalArgumentException("Not found a target Agency Id List");
        }
        if (codeListDetails.log().revisionNum() == 1) {
            throw new IllegalArgumentException("Not found previous revision");
        }

        CcState prevState = codeListDetails.state();

        var command = command(requester);

        command.cancel(codeListManifestId);

        LogId logId = logCommand(requester).revertToStableStateByReference(codeListDetails.guid(), CcType.CODE_LIST);
        command.updateLogId(codeListManifestId, logId);

        // Cancelling reverts WIP to the previously released revision. Publish it like an explicit state
        // change so any linked GitHub issues get the user-edited status comment (issue #1533 follow-up).
        CcState revertedTo = requester.isDeveloper() ? Published : Production;
        stateChangeEventPublisher.publish(
                CcType.CODE_LIST, codeListManifestId, prevState, revertedTo, requester.userId(), comment, projectFieldOptionOverride);
    }

    public boolean markAsDeleted(ScoreUser requester, CodeListManifestId codeListManifestId) {

        return updateState(requester, codeListManifestId, Deleted);
    }

    public boolean purge(ScoreUser requester, CodeListManifestId codeListManifestId) {

        CodeListSummaryRecord codeListSummary =
                query(requester).getCodeListSummary(codeListManifestId);

        if (!Deleted.equals(codeListSummary.state())) {
            throw new IllegalArgumentException("Only the Code List in 'Deleted' state can be deleted.");
        }

        boolean result = command(requester).delete(codeListManifestId);
        if (result) {
            logCommand(requester).deleteByReference(codeListSummary.guid());
        }
        return result;
    }

    public boolean restore(ScoreUser requester, CodeListManifestId codeListManifestId) {

        return updateState(requester, codeListManifestId, WIP);
    }

    public void transferOwnership(ScoreUser requester, ScoreUser targetUser, CodeListManifestId codeListManifestId) {

        var query = query(requester);

        CodeListSummaryRecord codeListSummary =
                query.getCodeListSummary(codeListManifestId);

        if (!WIP.equals(codeListSummary.state())) {
            throw new IllegalArgumentException("Only the code list in 'WIP' state can be modified.");
        }

        if (!codeListSummary.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the code list by the owner.");
        }

        var command = command(requester);

        // Perform ownership transfer
        boolean success = command.updateOwnership(targetUser, codeListManifestId);

        if (!success) {
            throw new AccessDeniedException("Ownership transfer failed due to insufficient permissions.");
        }

        LogId logId = logCommand(requester).create(
                query.getCodeListDetails(codeListManifestId), Modified);
        command.updateLogId(codeListManifestId, logId);
    }

    public CodeListUpliftingResponse uplift(
            ScoreUser requester, CodeListManifestId codeListManifestId, ReleaseId targetReleaseId) {

        var command = command(requester);
        Pair<CodeListManifestId, List<String>> result =
                command.uplift(requester, codeListManifestId, targetReleaseId);

        CodeListManifestId newCodeListManifestId = result.getFirst();
        var query = query(requester);
        LogId logId = logCommand(requester).create(
                query.getCodeListDetails(newCodeListManifestId), Added);
        command.updateLogId(newCodeListManifestId, logId);

        return new CodeListUpliftingResponse(result.getFirst(), result.getSecond());
    }

}
