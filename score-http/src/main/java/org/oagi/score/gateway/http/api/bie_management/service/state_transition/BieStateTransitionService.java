package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieUpdateStateListRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieCodeListStateDependencyRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.BieStateLevel;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.service.BieService;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.BieEditTreeController;
import org.oagi.score.gateway.http.api.bie_management.service.edit_tree.DefaultBieEditTreeController;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule.*;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListStateLevel;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListCommandService;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASBIEP;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASCCP;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;
import static org.springframework.util.StringUtils.hasLength;

/**
 * Builds the BIE/code-list dependency graph used by the state-transition
 * dialog, projects future states from the user's current selection, and
 * converts rule results into row-level blocking issues.
 */
@Service
@Transactional(readOnly = true)
public class BieStateTransitionService {

    /**
     * The projected future states used during dependency-dialog evaluation.
     *
     * <p>{@code actualFutureStateMap} respects ownership/selectability and is
     * used for the final BIE row validation, while
     * {@code requiredFutureStateMap} ignores those ownership limits so the
     * dialog can still surface dependent code lists and downstream blockers
     * that become relevant through implied BIE movement.
     *
     * <p>{@code actualCodeListFutureStateMap} applies the current code-list
     * checkbox selection to the visible code lists so rule validation does not
     * need to recalculate selected cascade targets per edge.
     *
     * <p>{@code requiredCodeListFutureStateMap} projects the code-list cascade
     * target implied by the required BIE future-state map, regardless of the
     * current checkbox selection.</p>
     */
    private record BieStateFutureStateProjection(
            Map<TopLevelAsbiepId, BieState> actualFutureStateMap,
            Map<TopLevelAsbiepId, BieState> requiredFutureStateMap,
            Map<CodeListManifestId, CcState> actualCodeListFutureStateMap,
            Map<CodeListManifestId, CcState> requiredCodeListFutureStateMap) {
    }

    /**
     * Immutable snapshot of the dependency graph and derived metadata for a
     * requested state transition.
     */
    private record BieStateDependencyGraph(
            /**
             * Root BIE ids explicitly requested by the user.
             *
             * <p>These ids seed graph traversal, define which rows are always
             * part of the future-state map, and anchor edge-distance
             * calculation.</p>
             */
            Set<TopLevelAsbiepId> requestedTopLevelAsbiepIds,
            /**
             * Root code list ids explicitly requested by the caller.
             *
             * <p>The current UI does not send these yet, but keeping the
             * concept in the graph lets the projection model stay symmetric
             * with BIE roots.</p>
             */
            Set<CodeListManifestId> requestedCodeListManifestIds,
            /**
             * Summary records for every root and visible dependency row in the
             * graph.
             *
             * <p>This is the canonical lookup used by row projection, rule
             * evaluation, and final state update validation.</p>
             */
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            /**
             * Direct prerequisite row ids for each visible dependency target.
             *
             * <p>This is the graph structure later copied into
            * {@link BieStateDependencyTarget#getDependencyTopLevelAsbiepIds()}
             * and used to render the Dependencies column.</p>
             */
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap,
            /**
             * Whether each visible row is allowed to be updated along the
             * traversed ownership/path rules.
             *
             * <p>Row projection converts this into
             * {@link BieStateDependencyTarget#isSelectable()} and later uses it
             * when assembling ownership issues for the row.</p>
             */
            LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap,
            /**
             * UI-facing dependency relations for each visible row.
             *
             * <p>These are direct adjacencies projected into
             * {@link BieStateDependencyTarget#getDependencies()} for the dialog
             * table and tooltips.</p>
             */
            Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> dependenciesRelationMap,
            /**
             * Minimum graph distance from any requested root to each visible
             * row.
             *
             * <p>This ordering metadata keeps near dependencies ahead of deeper
             * ones in the dialog.</p>
             */
            Map<TopLevelAsbiepId, Integer> edgeDistanceMap,
            /**
             * Directed distance-1 dependency pairs used by
             * {@link BieStateTransitionRule} implementations.
             *
             * <p>These edges are separate from the dialog relations because rule
             * evaluation needs both relationship direction and source/target
             * future states.</p>
             */
            List<BieStateTransitionEdge<TopLevelAsbiepId, TopLevelAsbiepId>> stateTransitionEdges,
            /**
             * Code list summaries referenced by visible BIE rows.
             */
            Map<CodeListManifestId, CodeListSummaryRecord> codeListMap,
            /**
             * Directed BIE-to-code-list dependency edges.
             */
            List<BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId>> codeListStateTransitionEdges,
            /**
             * Whether each visible code list is allowed to be updated by the
             * requester under the current dependency rules.
             */
            Map<CodeListManifestId, Boolean> codeListDependencyUpdateAllowedMap,
            /**
             * Directed code-list-to-BIE dependency edges.
             */
            List<BieStateTransitionEdge<CodeListManifestId, TopLevelAsbiepId>> usedByBieCodeListStateTransitionEdges,
            /**
             * UI-facing dependency relations for each visible code list row.
             */
            Map<CodeListManifestId, List<BieStateDependencyRelation>> codeListDependenciesRelationMap,
            /**
             * Minimum graph distance from any requested root to each visible
             * code list row.
             */
            Map<CodeListManifestId, Integer> codeListEdgeDistanceMap,
            /**
             * Reverse child lookup used to propagate blocking messages from
             * dependent rows back toward their ancestors.
             *
             * <p>This is used only during preview/validation message
             * resolution.</p>
             */
            Map<TopLevelAsbiepId, Set<TopLevelAsbiepId>> childMap) {
    }

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BieService bieService;

    @Autowired
    private CodeListCommandService codeListCommandService;

    @Autowired(required = false)
    private List<BieStateTransitionRule> stateTransitionRules = Collections.emptyList();

    /**
     * Revalidates the user's current dependency selection against the same
     * projected graph used to build the initial dialog.
     *
     * <p>Validation may be rooted from requested BIE ids, requested code-list
     * ids, or both.</p>
     */
    public List<BieStateDependencyTarget> validateStateDependencies(
            ScoreUser requester,
            Collection<TopLevelAsbiepId> requestedTopLevelAsbiepIds,
            Collection<CodeListManifestId> requestedCodeListManifestIds,
            BieState nextState,
            Collection<TopLevelAsbiepId> selectedTopLevelAsbiepIds,
            Collection<CodeListManifestId> selectedCodeListManifestIds) {
        BieStateDependencyGraph dependencyGraph = buildStateDependencyGraph(
                requester,
                requestedTopLevelAsbiepIds,
                requestedCodeListManifestIds,
                nextState);
        List<BieStateDependencyTarget> targets = buildDependencyTargets(
                requester,
                dependencyGraph,
                nextState,
                selectedTopLevelAsbiepIds,
                (selectedCodeListManifestIds != null) ? new LinkedHashSet<>(selectedCodeListManifestIds) : Collections.emptySet());
        return targets;
    }

    /**
     * Validates the requested state transition against cross-owner reuse and
     * inheritance rules before any write is attempted.
     */
    public void validateStateChange(ScoreUser requester,
                                    TopLevelAsbiepId topLevelAsbiepId,
                                    BieState state,
                                    Collection<CodeListManifestId> dependencyCodeListManifestIds) {
        if (state == BieState.Discard) {
            throw new IllegalArgumentException("Use the discard API for 'Discard' transitions.");
        }
        ensureCodeListDependenciesForChangingState(requester, List.of(topLevelAsbiepId), state, dependencyCodeListManifestIds);
        ensureBieRelationshipsForChangingState(requester, topLevelAsbiepId, state);
    }

    /**
     * Applies a state transition to the root BIE and any dependency rows the
     * user selected in the dialog.
     */
    @Transactional
    public void updateState(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, BieState state) {
        updateState(requester, topLevelAsbiepId, state, null, null);
    }

    /**
     * Applies a validated state transition to the root BIE and the approved
     * dependency rows.
     */
    @Transactional
    public void updateState(ScoreUser requester,
                            TopLevelAsbiepId topLevelAsbiepId,
                            BieState state,
                            Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
                            Collection<CodeListManifestId> dependencyCodeListManifestIds) {
        if (state == BieState.Discard) {
            throw new IllegalArgumentException("Use the discard API for 'Discard' transitions.");
        }
        validateStateChange(requester, topLevelAsbiepId, state, dependencyCodeListManifestIds);
        ensureDependencySelectionStateChange(
                requester,
                List.of(topLevelAsbiepId),
                state,
                dependencyTopLevelAsbiepIds,
                dependencyCodeListManifestIds);

        BieEditTreeController treeController = getTreeController(requester, topLevelAsbiepId);
        treeController.updateState(requester, state, dependencyTopLevelAsbiepIds);
        updateSelectedCodeLists(requester, state, dependencyCodeListManifestIds);
    }

    /**
     * Applies the same validated state transition to every BIE selected in the
     * list view request.
     */
    @Transactional
    public void updateStateBieList(ScoreUser requester, BieUpdateStateListRequest request) {
        if (request.getTopLevelAsbiepIds() == null || request.getTopLevelAsbiepIds().isEmpty()) {
            return;
        }
        if (request.getToState() == BieState.Discard) {
            throw new IllegalArgumentException("Use the discard API for 'Discard' transitions.");
        }

        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId ->
                validateStateChange(requester, topLevelAsbiepId, request.getToState(), request.getDependencyCodeListManifestIds()));
        ensureDependencySelectionStateChange(
                requester,
                request.getTopLevelAsbiepIds(),
                request.getToState(),
                request.getDependencyTopLevelAsbiepIds(),
                request.getDependencyCodeListManifestIds());

        request.getTopLevelAsbiepIds().forEach(topLevelAsbiepId -> {
            BieEditTreeController treeController = getTreeController(requester, topLevelAsbiepId);
            treeController.updateState(requester, request.getToState(), request.getDependencyTopLevelAsbiepIds());
        });
        updateSelectedCodeLists(requester, request.getToState(), request.getDependencyCodeListManifestIds());
    }

    /**
     * Builds the dependency graph once so preview rendering and checkbox
     * validation use the same graph snapshot.
     */
    private BieStateDependencyGraph buildStateDependencyGraph(
            ScoreUser requester,
            Collection<TopLevelAsbiepId> requestedTopLevelAsbiepIds,
            Collection<CodeListManifestId> requestedCodeListManifestIds,
            BieState nextState) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        Set<TopLevelAsbiepId> requestedTopLevelAsbiepIdSet = (requestedTopLevelAsbiepIds != null)
                ? new LinkedHashSet<>(requestedTopLevelAsbiepIds)
                : new LinkedHashSet<>();
        Set<CodeListManifestId> requestedCodeListIdSet = (requestedCodeListManifestIds != null)
                ? new LinkedHashSet<>(requestedCodeListManifestIds)
                : new LinkedHashSet<>();
        Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap = new LinkedHashMap<>();
        LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap = new LinkedHashMap<>();
        LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap = new LinkedHashMap<>();
        Set<String> visitedStates = new HashSet<>();

        for (TopLevelAsbiepId requestedTopLevelAsbiepId : requestedTopLevelAsbiepIdSet) {
            TopLevelAsbiepSummaryRecord rootTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(requestedTopLevelAsbiepId);
            if (rootTopLevelAsbiep != null) {
                collectDependencyTargets(requestedTopLevelAsbiepIdSet, rootTopLevelAsbiep, nextState,
                        new LinkedHashSet<>(Collections.singleton(requestedTopLevelAsbiepId)),
                        true, visitedStates, requester, topLevelAsbiepMap, dependencyMap,
                        dependencyUpdateAllowedMap, topLevelAsbiepQuery);
            }
        }

        Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds = new LinkedHashSet<>(requestedTopLevelAsbiepIdSet);
        associatedTopLevelAsbiepIds.addAll(dependencyMap.keySet());
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> dependenciesRelationMap =
                buildDependenciesRelationMap(topLevelAsbiepQuery, topLevelAsbiepMap, associatedTopLevelAsbiepIds);
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> connectedRelationMap =
                buildConnectedRelationMap(topLevelAsbiepQuery, topLevelAsbiepMap, associatedTopLevelAsbiepIds);
        Map<TopLevelAsbiepId, Integer> edgeDistanceMap =
                buildEdgeDistanceMap(requestedTopLevelAsbiepIdSet, connectedRelationMap);
        List<BieStateTransitionEdge<TopLevelAsbiepId, TopLevelAsbiepId>> stateTransitionEdges =
                buildStateTransitionEdges(topLevelAsbiepQuery, topLevelAsbiepMap, associatedTopLevelAsbiepIds);
        Map<CodeListManifestId, CodeListSummaryRecord> codeListMap =
                buildCodeListMap(requester, associatedTopLevelAsbiepIds, requestedCodeListIdSet);
        List<BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId>> codeListStateTransitionEdges =
                buildCodeListStateTransitionEdges(requester, associatedTopLevelAsbiepIds, codeListMap.keySet());
        Map<CodeListManifestId, Boolean> codeListDependencyUpdateAllowedMap =
                buildCodeListDependencyUpdateAllowedMap(requester, codeListMap.values(), nextState);
        List<BieStateTransitionEdge<CodeListManifestId, TopLevelAsbiepId>> usedByBieCodeListStateTransitionEdges =
                buildUsedByBieCodeListStateTransitionEdges(codeListStateTransitionEdges);
        Map<CodeListManifestId, List<BieStateDependencyRelation>> codeListDependenciesRelationMap =
                buildCodeListDependenciesRelationMap(topLevelAsbiepMap, codeListStateTransitionEdges);
        Map<CodeListManifestId, Integer> codeListEdgeDistanceMap =
                buildCodeListEdgeDistanceMap(requestedCodeListIdSet, edgeDistanceMap, codeListStateTransitionEdges);

        Map<TopLevelAsbiepId, Set<TopLevelAsbiepId>> childMap = buildDependentIssueChildMap(
                nextState,
                requestedTopLevelAsbiepIdSet,
                dependencyMap.keySet(),
                stateTransitionEdges);

        return new BieStateDependencyGraph(
                requestedTopLevelAsbiepIdSet,
                requestedCodeListIdSet,
                topLevelAsbiepMap,
                dependencyMap,
                dependencyUpdateAllowedMap,
                dependenciesRelationMap,
                edgeDistanceMap,
                stateTransitionEdges,
                codeListMap,
                codeListStateTransitionEdges,
                codeListDependencyUpdateAllowedMap,
                usedByBieCodeListStateTransitionEdges,
                codeListDependenciesRelationMap,
                codeListEdgeDistanceMap,
                childMap
        );
    }

    /**
     * Builds the directed adjacency used for propagated BIE dependency
     * conflicts.
     *
     * <p>The direction is driven by the requested transition:
     * forward transitions propagate through {@code REUSES}/{@code INHERITS_FROM}
     * edges, while backward transitions propagate through
     * {@code REUSED_BY}/{@code IS_A_BASED_OF} edges.</p>
     */
    private Map<TopLevelAsbiepId, Set<TopLevelAsbiepId>> buildDependentIssueChildMap(
            BieState nextState,
            Set<TopLevelAsbiepId> requestedTopLevelAsbiepIds,
            Set<TopLevelAsbiepId> visibleDependencyTopLevelAsbiepIds,
            List<BieStateTransitionEdge<TopLevelAsbiepId, TopLevelAsbiepId>> stateTransitionEdges) {
        Map<TopLevelAsbiepId, Set<TopLevelAsbiepId>> childMap = new LinkedHashMap<>();

        for (BieStateTransitionEdge<TopLevelAsbiepId, TopLevelAsbiepId> edge : stateTransitionEdges) {
            if (!isDependencyConflictPropagationEdge(nextState, edge.dependency())) {
                continue;
            }
            if (requestedTopLevelAsbiepIds.contains(edge.source()) ||
                    !visibleDependencyTopLevelAsbiepIds.contains(edge.source()) ||
                    !visibleDependencyTopLevelAsbiepIds.contains(edge.target())) {
                continue;
            }

            childMap.computeIfAbsent(edge.source(), key -> new LinkedHashSet<>())
                    .add(edge.target());
        }

        return childMap;
    }

    private boolean isDependencyConflictPropagationEdge(BieState nextState,
                                                        BieStateTransitionDependency dependency) {
        if (dependency == null || nextState == null) {
            return false;
        }

        if (isBackwardStateTransition(nextState)) {
            return dependency == BieStateTransitionDependency.REUSED_BY ||
                    dependency == BieStateTransitionDependency.IS_A_BASED_OF;
        }

        return dependency == BieStateTransitionDependency.REUSES ||
                dependency == BieStateTransitionDependency.INHERITS_FROM;
    }

    /**
     * Converts one dependency-graph snapshot into the transport rows used by
     * the dialog.
     *
     * <p>The method keeps all preview logic in one place: it projects BIE rows
     * first, applies the current checkbox state, computes future-state maps,
     * then evaluates BIE rules and code-list rules against that projection.</p>
     */
    private List<BieStateDependencyTarget> buildDependencyTargets(
            ScoreUser requester,
            BieStateDependencyGraph dependencyGraph,
            BieState nextState,
            Collection<TopLevelAsbiepId> selectedTopLevelAsbiepIds,
            Collection<CodeListManifestId> selectedCodeListManifestIds) {
        List<BieStateDependencyTarget> targets = buildBieDependencyTargets(requester, dependencyGraph, nextState);
        applyCheckedState(
                targets,
                selectedTopLevelAsbiepIds,
                selectedCodeListManifestIds);

        BieStateFutureStateProjection futureStateProjection = buildFutureStateProjection(
                requester,
                dependencyGraph,
                nextState,
                getSelectedTopLevelAsbiepIdSet(targets),
                (selectedCodeListManifestIds != null)
                        ? new LinkedHashSet<>(selectedCodeListManifestIds)
                        : Collections.emptySet());
        applyBieStateChangeAvailability(
                requester,
                targets,
                dependencyGraph,
                nextState,
                futureStateProjection.requiredFutureStateMap());
        applyStateTransitionRules(
                requester,
                targets,
                dependencyGraph,
                nextState,
                futureStateProjection,
                selectedCodeListManifestIds);
        return targets;
    }

    /**
     * Projects the visible BIE vertices in the graph into dialog rows before
     * any checkbox-specific future-state evaluation runs.
     */
    private List<BieStateDependencyTarget> buildBieDependencyTargets(
            ScoreUser requester,
            BieStateDependencyGraph dependencyGraph,
            BieState nextState) {
        var asbiepQuery = repositoryFactory.asbiepQueryRepository(requester);
        var businessContextQuery = repositoryFactory.businessContextQueryRepository(requester);

        return dependencyGraph.dependencyMap().entrySet().stream()
                .map(entry -> {
                    TopLevelAsbiepSummaryRecord target = dependencyGraph.topLevelAsbiepMap().get(entry.getKey());
                    return toBieStateDependencyTarget(
                            requester,
                            target,
                            asbiepQuery.getAsbiepSummary(target.asbiepId()),
                            businessContextQuery.getBusinessContextSummaryList(target.topLevelAsbiepId()),
                            dependencyGraph.dependenciesRelationMap().getOrDefault(entry.getKey(), Collections.emptyList()),
                            dependencyGraph.edgeDistanceMap().getOrDefault(entry.getKey(), Integer.MAX_VALUE),
                            new ArrayList<>(entry.getValue()),
                            dependencyGraph.dependencyUpdateAllowedMap().getOrDefault(entry.getKey(), false),
                            nextState);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void applyBieStateChangeAvailability(ScoreUser requester,
                                                 List<BieStateDependencyTarget> targets,
                                                 BieStateDependencyGraph dependencyGraph,
                                                 BieState nextState,
                                                 Map<TopLevelAsbiepId, BieState> requiredFutureStateMap) {
        for (BieStateDependencyTarget target : targets) {
            if (target.getNodeType() != BieStateDependencyNodeType.BIE || target.getTopLevelAsbiepId() == null) {
                continue;
            }

            TopLevelAsbiepSummaryRecord topLevelAsbiep = dependencyGraph.topLevelAsbiepMap().get(target.getTopLevelAsbiepId());
            BieState requiredFutureState = requiredFutureStateMap.get(target.getTopLevelAsbiepId());
            boolean stateChangeAvailable = topLevelAsbiep != null &&
                    topLevelAsbiep.state() != null &&
                    requiredFutureState != null &&
                    !Objects.equals(topLevelAsbiep.state(), requiredFutureState) &&
                    shouldApplyDependencyStateChange(requester, topLevelAsbiep.state(), nextState) &&
                    target.isSelectable();
            target.setStateChangeAvailable(stateChangeAvailable);
        }
    }

    private Set<TopLevelAsbiepId> getSelectedTopLevelAsbiepIdSet(List<BieStateDependencyTarget> targets) {
        return targets.stream()
                .filter(target -> target.getNodeType() == BieStateDependencyNodeType.BIE)
                .filter(BieStateDependencyTarget::isChecked)
                .map(BieStateDependencyTarget::getTopLevelAsbiepId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Applies the current checkbox selection to the row model without adding
     * any validation issues.
     *
     * <p>The subsequent future-state projection uses this normalized checked
     * state as its only selection input.</p>
     */
    private void applyCheckedState(
            List<BieStateDependencyTarget> targets,
            Collection<TopLevelAsbiepId> selectedTopLevelAsbiepIds,
            Collection<CodeListManifestId> selectedCodeListManifestIds) {
        Set<TopLevelAsbiepId> selectedIdSet = new HashSet<>();
        if (selectedTopLevelAsbiepIds != null) {
            selectedIdSet.addAll(selectedTopLevelAsbiepIds);
        } else {
            targets.stream()
                    .filter(target -> target.getNodeType() == BieStateDependencyNodeType.BIE)
                    .filter(BieStateDependencyTarget::isSelectable)
                    .map(BieStateDependencyTarget::getTopLevelAsbiepId)
                    .forEach(selectedIdSet::add);
        }
        Set<CodeListManifestId> selectedCodeListIdSet = (selectedCodeListManifestIds != null)
                ? new HashSet<>(selectedCodeListManifestIds) : new HashSet<>();

        targets.forEach(target -> {
            if (target.getNodeType() == BieStateDependencyNodeType.CODE_LIST) {
                boolean checked = target.isSelectable() &&
                        target.getCodeListManifestId() != null &&
                        selectedCodeListIdSet.contains(target.getCodeListManifestId());
                target.setChecked(checked);
                return;
            }
            boolean selectable = target.isSelectable();
            boolean checked = selectable && selectedIdSet.contains(target.getTopLevelAsbiepId());
            target.setChecked(checked);
        });
    }

    /**
     * Converts repository records into the dialog row transport model.
     */
    private BieStateDependencyTarget toBieStateDependencyTarget(
            ScoreUser requester,
            TopLevelAsbiepSummaryRecord topLevelAsbiep,
            AsbiepSummaryRecord asbiep,
            List<BusinessContextSummaryRecord> businessContexts,
            List<BieStateDependencyRelation> dependencies,
            int edgeDistance,
            List<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
            boolean dependencyUpdateAllowed,
            BieState nextState) {
        return new BieStateDependencyTarget(
                toNodeKey(topLevelAsbiep.topLevelAsbiepId()),
                BieStateDependencyNodeType.BIE,
                topLevelAsbiep.topLevelAsbiepId(),
                null,
                dependencyTopLevelAsbiepIds,
                dependencies,
                edgeDistance,
                topLevelAsbiep.den(),
                topLevelAsbiep.propertyTerm(),
                topLevelAsbiep.displayName(),
                null,
                topLevelAsbiep.guid().value(),
                ownerLoginId(topLevelAsbiep),
                null,
                null,
                businessContexts.stream().map(BusinessContextSummaryRecord::name).toList(),
                topLevelAsbiep.version(),
                topLevelAsbiep.status(),
                (asbiep != null) ? asbiep.remark() : null,
                (topLevelAsbiep.state() != null) ? topLevelAsbiep.state().name() : null,
                dependencyUpdateAllowed,
                dependencyUpdateAllowed && shouldApplyDependencyStateChange(requester, topLevelAsbiep.state(), nextState),
                false,
                new ArrayList<>()
        );
    }

    /**
     * Traverses the dependency graph in the direction required by the
     * transition and records the rows that should be visible in the dialog.
     */
    private void collectDependencyTargets(
            Set<TopLevelAsbiepId> requestedTopLevelAsbiepIdSet,
            TopLevelAsbiepSummaryRecord currentTopLevelAsbiep,
            BieState nextState,
            LinkedHashSet<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
            boolean dependencyPathAllowed,
            Set<String> visitedStates,
            ScoreUser requester,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap,
            LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery) {
        String visitedStateKey = currentTopLevelAsbiep.topLevelAsbiepId() + "|" +
                dependencyTopLevelAsbiepIds.stream().map(TopLevelAsbiepId::toString).sorted()
                        .reduce((left, right) -> left + "," + right).orElse("") +
                "|" + dependencyPathAllowed;
        if (!visitedStates.add(visitedStateKey)) {
            return;
        }

        topLevelAsbiepMap.putIfAbsent(currentTopLevelAsbiep.topLevelAsbiepId(), currentTopLevelAsbiep);

        if (isDiscardStateTransition(nextState)) {
            topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                            true, true, visitedStates, requester, topLevelAsbiepMap,
                            dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));

            topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                            true, true, visitedStates, requester, topLevelAsbiepMap,
                            dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));
            return;
        }

        if (isBackwardStateTransition(nextState)) {
            topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                            true, true, visitedStates, requester, topLevelAsbiepMap,
                            dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));

            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                            true, false, visitedStates, requester, topLevelAsbiepMap,
                            dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));

            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(currentTopLevelAsbiep.basedTopLevelAsbiepId());
            if (basedTopLevelAsbiep != null) {
                collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, basedTopLevelAsbiep, nextState,
                        dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                        true, false, visitedStates, requester, topLevelAsbiepMap,
                        dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery);
            }

            topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                    .forEach(target -> collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, target, nextState,
                            dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                            true, false, visitedStates, requester, topLevelAsbiepMap,
                            dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));
            return;
        }

        topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                .forEach(target -> collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, target, nextState,
                        dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                        true, true, visitedStates, requester, topLevelAsbiepMap,
                        dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));

        TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(currentTopLevelAsbiep.basedTopLevelAsbiepId());
        if (basedTopLevelAsbiep != null) {
            collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, basedTopLevelAsbiep, nextState,
                    dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                    true, true, visitedStates, requester, topLevelAsbiepMap,
                    dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery);
        }

        topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(currentTopLevelAsbiep.topLevelAsbiepId()).stream()
                .forEach(target -> collectDependencyTarget(requestedTopLevelAsbiepIdSet, currentTopLevelAsbiep, target, nextState,
                        dependencyTopLevelAsbiepIds, dependencyPathAllowed,
                        true, false, visitedStates, requester, topLevelAsbiepMap,
                        dependencyMap, dependencyUpdateAllowedMap, topLevelAsbiepQuery));
    }

    /**
     * Adds one dependency vertex/edge to the graph and continues traversal
     * through that row while preserving the ownership-aware update flags for
     * the current path.
     */
    private void collectDependencyTarget(
            Set<TopLevelAsbiepId> requestedTopLevelAsbiepIdSet,
            TopLevelAsbiepSummaryRecord currentTopLevelAsbiep,
            TopLevelAsbiepSummaryRecord target,
            BieState nextState,
            LinkedHashSet<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
            boolean dependencyPathAllowed,
            boolean dependencyUpdateAllowedOnPath,
            boolean continueTraversal,
            Set<String> visitedStates,
            ScoreUser requester,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            LinkedHashMap<TopLevelAsbiepId, LinkedHashSet<TopLevelAsbiepId>> dependencyMap,
            LinkedHashMap<TopLevelAsbiepId, Boolean> dependencyUpdateAllowedMap,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery) {
        topLevelAsbiepMap.putIfAbsent(target.topLevelAsbiepId(), target);

        boolean sameOwner = currentTopLevelAsbiep.owner().userId().equals(target.owner().userId());
        boolean stateChangeNeeded = shouldApplyDependencyStateChange(requester, target.state(), nextState);
        boolean visible = !requestedTopLevelAsbiepIdSet.contains(target.topLevelAsbiepId());
        boolean nextDependencyPathAllowed = dependencyPathAllowed &&
                (sameOwner || isAdminDiscardTransition(requester, nextState)) &&
                dependencyUpdateAllowedOnPath;

        if (visible) {
            dependencyMap.computeIfAbsent(target.topLevelAsbiepId(), key -> new LinkedHashSet<>())
                    .addAll(dependencyTopLevelAsbiepIds);
            dependencyUpdateAllowedMap.merge(
                    target.topLevelAsbiepId(),
                    nextDependencyPathAllowed && stateChangeNeeded,
                    Boolean::logicalOr);
        }

        if (!continueTraversal) {
            return;
        }

        LinkedHashSet<TopLevelAsbiepId> nextDependencies = visible
                ? new LinkedHashSet<>(Collections.singleton(target.topLevelAsbiepId()))
                : new LinkedHashSet<>(dependencyTopLevelAsbiepIds);
        collectDependencyTargets(requestedTopLevelAsbiepIdSet, target, nextState, nextDependencies,
                nextDependencyPathAllowed, visitedStates, requester,
                topLevelAsbiepMap, dependencyMap,
                dependencyUpdateAllowedMap, topLevelAsbiepQuery);
    }

    private BieStateDependencyRelation toBieStateDependencyRelation(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return toBieStateDependencyRelation(topLevelAsbiep, null);
    }

    private BieStateDependencyRelation toBieStateDependencyRelation(
            TopLevelAsbiepSummaryRecord topLevelAsbiep,
            BieStateTransitionDependency dependency) {
        return new BieStateDependencyRelation(
                toNodeKey(topLevelAsbiep.topLevelAsbiepId()),
                BieStateDependencyNodeType.BIE,
                topLevelAsbiep.topLevelAsbiepId(),
                null,
                dependency,
                toName(topLevelAsbiep),
                topLevelAsbiep.guid().value()
        );
    }

    private boolean isBackwardStateTransition(BieState nextState) {
        return nextState == BieState.WIP || nextState == BieState.Discard;
    }

    private boolean isDiscardStateTransition(BieState nextState) {
        return nextState == BieState.Discard;
    }

    /**
     * Resolves the first blocking BIE-side issue for one dependency row and
     * memoizes the result so repeated traversals of the same graph stay cheap.
     *
     * <p>The result can be a direct issue on the row itself or a propagated
     * dependency conflict from a downstream child row.</p>
     */
    private String buildBieOwnershipMessage(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                            BieState nextState) {
        if (isDiscardStateTransition(nextState)) {
            return "This BIE is owned by " + toOwnerName(topLevelAsbiep) + " and cannot be discarded.";
        }
        return "This BIE is owned by " + toOwnerName(topLevelAsbiep) + " and cannot be updated.";
    }

    private String buildCodeListOwnershipMessage(CodeListSummaryRecord codeList) {
        return "This code list is owned by " + toOwnerName(codeList) + " and cannot be updated.";
    }

    private String buildBieComparableStateMessage(BieState nextState) {
        if (isDiscardStateTransition(nextState)) {
            return "This BIE must be discarded.";
        }
        return "This BIE must be in '" + nextState + "'.";
    }

    private String buildBieDependencyConflictMessage(BieState nextState) {
        if (isDiscardStateTransition(nextState)) {
            return "A dependent BIE for this record must be discarded.";
        }
        return "A dependent BIE for this record must be in '" + nextState + "'.";
    }

    private String buildCodeListComparableStateMessage(BieState bieState, CodeListSummaryRecord codeList) {
        CcState nextCodeListState = nextCodeListState(bieState, codeList);
        if (nextCodeListState != null) {
            return "This code list must be in '" + nextCodeListState + "'.";
        }
        return "This code list must be in " + toQuotedStateList(CodeListStateLevel.compatibleStates(bieState, codeList)) + ".";
    }

    private String buildAssignedCodeListDependencyConflictMessage(BieState bieState,
                                                                  CodeListSummaryRecord codeList,
                                                                  BieStateDependencyTarget codeListTarget) {
        if (hasIssueType(codeListTarget.getIssues(), BieStateDependencyIssueType.OWNERSHIP)) {
            return "An assigned code list for this BIE is owned by " + toOwnerName(codeList) + " and cannot be updated.";
        }
        CcState nextCodeListState = nextCodeListState(bieState, codeList);
        if (nextCodeListState != null) {
            return "An assigned code list for this BIE must be in '" + nextCodeListState + "'.";
        }
        return "An assigned code list for this BIE must be in " +
                toQuotedStateList(CodeListStateLevel.compatibleStates(bieState, codeList)) + ".";
    }

    /**
     * Assembles the issues shown on one code-list row.
     *
     * <p>Ownership/selectability is handled first because a cross-owner code
     * list must surface that blocker even if its current state is otherwise
     * incompatible.</p>
     */
    private List<BieStateDependencyIssue> buildCodeListIssues(ScoreUser requester,
                                                              CodeListSummaryRecord codeList,
                                                              BieState nextState,
                                                              CcState futureState,
                                                              String stateTransitionMessage) {
        if (isDiscardStateTransition(nextState)) {
            return new ArrayList<>();
        }
        if (codeList == null || nextState == null || codeList.state() == null || futureState == null) {
            return new ArrayList<>();
        }

        boolean compatible = CodeListStateLevel.compatibleStates(nextState, codeList).contains(futureState);
        boolean sameOwner = codeList.owner() != null &&
                requester != null &&
                requester.userId().equals(codeList.owner().userId());

        if (compatible && stateTransitionMessage == null) {
            return new ArrayList<>();
        }

        if (!compatible && !sameOwner) {
            return new ArrayList<>(List.of(
                    new BieStateDependencyIssue(
                            BieStateDependencyIssueType.OWNERSHIP,
                            buildCodeListOwnershipMessage(codeList))));
        }

        String message = (stateTransitionMessage != null)
                ? stateTransitionMessage
                : buildCodeListComparableStateMessage(nextState, codeList);
        return new ArrayList<>(List.of(
                new BieStateDependencyIssue(
                        BieStateDependencyIssueType.STATE_COMPATIBILITY,
                        message)));
    }

    private boolean hasIssueType(List<BieStateDependencyIssue> issues, BieStateDependencyIssueType issueType) {
        return issues != null && issues.stream().anyMatch(issue -> issue.getType() == issueType);
    }

    private List<BieStateDependencyIssue> removeIssuesOfType(List<BieStateDependencyIssue> issues,
                                                             BieStateDependencyIssueType issueType) {
        if (issues == null || issues.isEmpty()) {
            return new ArrayList<>();
        }
        return issues.stream()
                .filter(issue -> issue.getType() != issueType)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<BieStateDependencyIssue> addIssue(List<BieStateDependencyIssue> issues,
                                                   BieStateDependencyIssue issue) {
        List<BieStateDependencyIssue> nextIssues = removeIssuesOfType(
                issues, issue.getType());
        if (nextIssues.stream().anyMatch(existing -> Objects.equals(existing.getMessage(), issue.getMessage()))) {
            return nextIssues;
        }
        nextIssues.add(issue);
        return nextIssues;
    }

    private List<BieStateDependencyIssue> mergeIssues(List<BieStateDependencyIssue> left,
                                                      List<BieStateDependencyIssue> right) {
        List<BieStateDependencyIssue> merged = new ArrayList<>();
        if (left != null) {
            merged.addAll(left);
        }
        if (right != null) {
            for (BieStateDependencyIssue issue : right) {
                merged = addIssue(merged, issue);
            }
        }
        return merged;
    }

    private List<BieState> requiredBieStatesForTransition(BieState nextState) {
        if (nextState == null) {
            return List.of();
        }
        return isBackwardStateTransition(nextState) ? List.of(nextState) : BieStateLevel.compatibleStates(nextState);
    }

    private String toOwnerName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (topLevelAsbiep == null || topLevelAsbiep.owner() == null) {
            return "another user";
        }
        if (hasLength(topLevelAsbiep.owner().loginId())) {
            return "'" + topLevelAsbiep.owner().loginId() + "'";
        }
        if (hasLength(topLevelAsbiep.owner().username())) {
            return "'" + topLevelAsbiep.owner().username() + "'";
        }
        return "another user";
    }

    private String toOwnerName(CodeListSummaryRecord codeList) {
        if (codeList == null || codeList.owner() == null) {
            return "another user";
        }
        if (hasLength(codeList.owner().loginId())) {
            return "'" + codeList.owner().loginId() + "'";
        }
        if (hasLength(codeList.owner().username())) {
            return "'" + codeList.owner().username() + "'";
        }
        return "another user";
    }

    /**
     * Builds the direct dependency relations displayed in the dialog's
     * Dependencies column.
     */
    private Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> buildDependenciesRelationMap(
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds) {
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> dependenciesRelationMap = new LinkedHashMap<>();

        for (TopLevelAsbiepId topLevelAsbiepId : associatedTopLevelAsbiepIds) {
            LinkedHashMap<TopLevelAsbiepId, BieStateDependencyRelation> relationMap = new LinkedHashMap<>();

            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(
                            target.topLevelAsbiepId(),
                            toBieStateDependencyRelation(target, BieStateTransitionDependency.REUSES)));

            topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(
                            target.topLevelAsbiepId(),
                            toBieStateDependencyRelation(target, BieStateTransitionDependency.REUSED_BY)));

            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepMap.get(topLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    (topLevelAsbiep != null)
                            ? topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiep.basedTopLevelAsbiepId())
                            : null;
            if (basedTopLevelAsbiep != null && associatedTopLevelAsbiepIds.contains(basedTopLevelAsbiep.topLevelAsbiepId())) {
                relationMap.putIfAbsent(
                        basedTopLevelAsbiep.topLevelAsbiepId(),
                        toBieStateDependencyRelation(basedTopLevelAsbiep, BieStateTransitionDependency.INHERITS_FROM));
            }

            topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(
                            target.topLevelAsbiepId(),
                            toBieStateDependencyRelation(target, BieStateTransitionDependency.IS_A_BASED_OF)));

            dependenciesRelationMap.put(topLevelAsbiepId, new ArrayList<>(relationMap.values()));
        }

        return dependenciesRelationMap;
    }

    /**
     * Builds the undirected connected relation map used for edge distance
     * calculation across the visible dependency graph.
     */
    private Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> buildConnectedRelationMap(
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds) {
        Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> connectedRelationMap = new LinkedHashMap<>();

        for (TopLevelAsbiepId topLevelAsbiepId : associatedTopLevelAsbiepIds) {
            LinkedHashMap<TopLevelAsbiepId, BieStateDependencyRelation> relationMap = new LinkedHashMap<>();

            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(target.topLevelAsbiepId(), toBieStateDependencyRelation(target)));

            topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(target.topLevelAsbiepId(), toBieStateDependencyRelation(target)));

            topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> relationMap.putIfAbsent(target.topLevelAsbiepId(), toBieStateDependencyRelation(target)));

            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepMap.get(topLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    (topLevelAsbiep != null)
                            ? topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiep.basedTopLevelAsbiepId())
                            : null;
            if (basedTopLevelAsbiep != null && associatedTopLevelAsbiepIds.contains(basedTopLevelAsbiep.topLevelAsbiepId())) {
                relationMap.putIfAbsent(basedTopLevelAsbiep.topLevelAsbiepId(), toBieStateDependencyRelation(basedTopLevelAsbiep));
            }

            connectedRelationMap.put(topLevelAsbiepId, new ArrayList<>(relationMap.values()));
        }

        return connectedRelationMap;
    }

    private Map<TopLevelAsbiepId, Integer> buildEdgeDistanceMap(
            Set<TopLevelAsbiepId> requestedTopLevelAsbiepIds,
            Map<TopLevelAsbiepId, List<BieStateDependencyRelation>> connectedRelationMap) {
        Map<TopLevelAsbiepId, Integer> edgeDistanceMap = new HashMap<>();
        Queue<TopLevelAsbiepId> queue = new LinkedList<>();

        for (TopLevelAsbiepId requestedTopLevelAsbiepId : requestedTopLevelAsbiepIds) {
            edgeDistanceMap.put(requestedTopLevelAsbiepId, 0);
            queue.offer(requestedTopLevelAsbiepId);
        }

        while (!queue.isEmpty()) {
            TopLevelAsbiepId currentTopLevelAsbiepId = queue.poll();
            int currentDistance = edgeDistanceMap.getOrDefault(currentTopLevelAsbiepId, 0);
            for (BieStateDependencyRelation relation :
                    connectedRelationMap.getOrDefault(currentTopLevelAsbiepId, Collections.emptyList())) {
                if (edgeDistanceMap.containsKey(relation.getTopLevelAsbiepId())) {
                    continue;
                }
                edgeDistanceMap.put(relation.getTopLevelAsbiepId(), currentDistance + 1);
                queue.offer(relation.getTopLevelAsbiepId());
            }
        }

        return edgeDistanceMap;
    }

    /**
     * Computes the two future-state maps used by dialog validation.
     *
     * <p>{@code actualFutureStateMap} contains only the rows that the current
     * selection can truly move, while {@code requiredFutureStateMap} keeps
     * expanding through BIE rules so downstream blockers such as assigned code
     * lists remain visible even when an intermediate BIE cannot be selected.
     *
     * <p>The same split applies to code lists: the actual map reflects only
     * selected code-list cascade updates, while the required map reflects both
     * explicitly selected code-list updates and the preferred cascade target
     * implied by the mixed BIE/code-list projection.</p>
     */
    private BieStateFutureStateProjection buildFutureStateProjection(
            ScoreUser requester,
            BieStateDependencyGraph dependencyGraph,
            BieState nextState,
            Set<TopLevelAsbiepId> selectedTopLevelAsbiepIds,
            Set<CodeListManifestId> selectedCodeListManifestIds) {
        Map<TopLevelAsbiepId, BieState> actualFutureStateMap = new LinkedHashMap<>();
        Map<TopLevelAsbiepId, BieState> requiredFutureStateMap = new LinkedHashMap<>();
        Map<CodeListManifestId, CcState> actualCodeListFutureStateMap = new LinkedHashMap<>();
        Map<CodeListManifestId, CcState> requiredCodeListFutureStateMap = new LinkedHashMap<>();
        Map<CodeListManifestId, CcState> nextCodeListStateMap =
                buildNextCodeListStateMap(dependencyGraph.codeListMap(), nextState);

        for (Map.Entry<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> entry : dependencyGraph.topLevelAsbiepMap().entrySet()) {
            actualFutureStateMap.put(entry.getKey(), entry.getValue().state());
            requiredFutureStateMap.put(entry.getKey(), entry.getValue().state());
        }
        for (Map.Entry<CodeListManifestId, CodeListSummaryRecord> entry : dependencyGraph.codeListMap().entrySet()) {
            actualCodeListFutureStateMap.put(entry.getKey(), entry.getValue().state());
            requiredCodeListFutureStateMap.put(entry.getKey(), entry.getValue().state());
        }

        Set<TopLevelAsbiepId> requestedAndSelectedTopLevelAsbiepIds =
                new LinkedHashSet<>(dependencyGraph.requestedTopLevelAsbiepIds());
        requestedAndSelectedTopLevelAsbiepIds.addAll(normalizeSelectedTopLevelAsbiepIds(
                requester,
                dependencyGraph,
                nextState,
                selectedTopLevelAsbiepIds));
        for (TopLevelAsbiepId requestedOrSelectedTopLevelAsbiepId : requestedAndSelectedTopLevelAsbiepIds) {
            actualFutureStateMap.put(requestedOrSelectedTopLevelAsbiepId, nextState);
            requiredFutureStateMap.put(requestedOrSelectedTopLevelAsbiepId, nextState);
        }
        Set<CodeListManifestId> requestedAndSelectedCodeListManifestIds =
                new LinkedHashSet<>(dependencyGraph.requestedCodeListManifestIds());
        requestedAndSelectedCodeListManifestIds.addAll(normalizeSelectedCodeListManifestIds(
                dependencyGraph,
                nextCodeListStateMap,
                selectedCodeListManifestIds));
        for (CodeListManifestId requestedOrSelectedCodeListManifestId : requestedAndSelectedCodeListManifestIds) {
            CcState nextCodeListState = nextCodeListStateMap.get(requestedOrSelectedCodeListManifestId);
            if (nextCodeListState == null) {
                continue;
            }

            actualCodeListFutureStateMap.put(requestedOrSelectedCodeListManifestId, nextCodeListState);
            requiredCodeListFutureStateMap.put(requestedOrSelectedCodeListManifestId, nextCodeListState);
        }

        if (requiredFutureStateMap.isEmpty() || stateTransitionRules.isEmpty()) {
            return new BieStateFutureStateProjection(
                    actualFutureStateMap,
                    requiredFutureStateMap,
                    actualCodeListFutureStateMap,
                    requiredCodeListFutureStateMap);
        }

        boolean changed;
        do {
            changed = false;

            for (BieStateTransitionEdge<TopLevelAsbiepId, TopLevelAsbiepId> edge : dependencyGraph.stateTransitionEdges()) {
                TopLevelAsbiepSummaryRecord source = dependencyGraph.topLevelAsbiepMap().get(edge.source());
                TopLevelAsbiepSummaryRecord target = dependencyGraph.topLevelAsbiepMap().get(edge.target());
                if (source == null || target == null) {
                    continue;
                }

                changed |= applyFutureStateRule(
                        requiredFutureStateMap,
                        requiredFutureStateMap,
                        source,
                        target,
                        edge,
                        TopLevelAsbiepSummaryRecord::state,
                        TopLevelAsbiepSummaryRecord::state,
                        BieFutureStateCarrier::new,
                        BieFutureStateCarrier::new,
                        record -> shouldApplyDependencyStateChange(requester, record.state(), nextState) ? nextState : null);
            }

            for (BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId> edge : dependencyGraph.codeListStateTransitionEdges()) {
                TopLevelAsbiepSummaryRecord source = dependencyGraph.topLevelAsbiepMap().get(edge.source());
                CodeListSummaryRecord target = dependencyGraph.codeListMap().get(edge.target());
                if (source == null || target == null) {
                    continue;
                }

                changed |= applyFutureStateRule(
                        requiredFutureStateMap,
                        requiredCodeListFutureStateMap,
                        source,
                        target,
                        edge,
                        TopLevelAsbiepSummaryRecord::state,
                        CodeListSummaryRecord::state,
                        BieFutureStateCarrier::new,
                        CodeListFutureStateCarrier::new,
                        record -> nextCodeListStateMap.get(record.codeListManifestId()));
            }

            for (BieStateTransitionEdge<CodeListManifestId, TopLevelAsbiepId> edge :
                    dependencyGraph.usedByBieCodeListStateTransitionEdges()) {
                CodeListSummaryRecord source = dependencyGraph.codeListMap().get(edge.source());
                TopLevelAsbiepSummaryRecord target = dependencyGraph.topLevelAsbiepMap().get(edge.target());
                if (source == null || target == null) {
                    continue;
                }

                changed |= applyFutureStateRule(
                        requiredCodeListFutureStateMap,
                        requiredFutureStateMap,
                        source,
                        target,
                        edge,
                        CodeListSummaryRecord::state,
                        TopLevelAsbiepSummaryRecord::state,
                        CodeListFutureStateCarrier::new,
                        BieFutureStateCarrier::new,
                        record -> shouldApplyDependencyStateChange(requester, record.state(), nextState) ? nextState : null);
            }
        } while (changed);

        return new BieStateFutureStateProjection(
                actualFutureStateMap,
                requiredFutureStateMap,
                actualCodeListFutureStateMap,
                requiredCodeListFutureStateMap);
    }

    /**
     * Derives the preferred code-list next state for the requested BIE state.
     *
     * <p>This keeps the "requested BIE next state -> code-list next state"
     * mapping explicit so projection, selection normalization, and the final
     * update flow all use the same target-state calculation.</p>
     */
    private CcState nextCodeListState(BieState nextState, CodeListSummaryRecord codeList) {
        if (nextState == null || codeList == null) {
            return null;
        }
        return CodeListStateLevel.preferredCascadeTargetState(nextState, codeList);
    }

    private Map<CodeListManifestId, CcState> buildNextCodeListStateMap(
            Map<CodeListManifestId, CodeListSummaryRecord> codeListMap,
            BieState nextState) {
        Map<CodeListManifestId, CcState> nextCodeListStateMap = new LinkedHashMap<>();
        for (Map.Entry<CodeListManifestId, CodeListSummaryRecord> entry : codeListMap.entrySet()) {
            CcState nextCodeListState = nextCodeListState(nextState, entry.getValue());
            if (nextCodeListState != null) {
                nextCodeListStateMap.put(entry.getKey(), nextCodeListState);
            }
        }
        return nextCodeListStateMap;
    }

    private Set<TopLevelAsbiepId> normalizeSelectedTopLevelAsbiepIds(
            ScoreUser requester,
            BieStateDependencyGraph dependencyGraph,
            BieState nextState,
            Set<TopLevelAsbiepId> selectedTopLevelAsbiepIds) {
        if (selectedTopLevelAsbiepIds == null || selectedTopLevelAsbiepIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<TopLevelAsbiepId> normalizedSelectedIds = new LinkedHashSet<>();
        for (TopLevelAsbiepId selectedTopLevelAsbiepId : selectedTopLevelAsbiepIds) {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = dependencyGraph.topLevelAsbiepMap().get(selectedTopLevelAsbiepId);
            if (topLevelAsbiep == null || topLevelAsbiep.state() == null) {
                continue;
            }
            if (!dependencyGraph.dependencyUpdateAllowedMap().getOrDefault(selectedTopLevelAsbiepId, false)) {
                continue;
            }
            if (!shouldApplyDependencyStateChange(requester, topLevelAsbiep.state(), nextState)) {
                continue;
            }

            normalizedSelectedIds.add(selectedTopLevelAsbiepId);
        }

        return normalizedSelectedIds;
    }

    private Set<CodeListManifestId> normalizeSelectedCodeListManifestIds(
            BieStateDependencyGraph dependencyGraph,
            Map<CodeListManifestId, CcState> nextCodeListStateMap,
            Set<CodeListManifestId> selectedCodeListManifestIds) {
        if (selectedCodeListManifestIds == null || selectedCodeListManifestIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<CodeListManifestId> normalizedSelectedIds = new LinkedHashSet<>();
        for (CodeListManifestId selectedCodeListManifestId : selectedCodeListManifestIds) {
            CodeListSummaryRecord codeList = dependencyGraph.codeListMap().get(selectedCodeListManifestId);
            if (codeList == null || codeList.state() == null) {
                continue;
            }
            if (!dependencyGraph.codeListDependencyUpdateAllowedMap()
                    .getOrDefault(selectedCodeListManifestId, false)) {
                continue;
            }

            CcState nextCodeListState = nextCodeListStateMap.get(selectedCodeListManifestId);
            if (nextCodeListState == null ||
                    Objects.equals(codeList.state(), nextCodeListState)) {
                continue;
            }

            normalizedSelectedIds.add(selectedCodeListManifestId);
        }

        return normalizedSelectedIds;
    }

    private <SI, TI, SR, TR, SS, TS> boolean applyFutureStateRule(
            Map<SI, SS> sourceFutureStateMap,
            Map<TI, TS> targetFutureStateMap,
            SR source,
            TR target,
            BieStateTransitionEdge<SI, TI> edge,
            Function<SR, SS> sourceStateAccessor,
            Function<TR, TS> targetStateAccessor,
            BiFunction<SR, SS, FutureStateCarrier<?, ?>> sourceCarrierFactory,
            BiFunction<TR, TS, FutureStateCarrier<?, ?>> targetCarrierFactory,
            Function<TR, TS> desiredTargetFutureStateProvider) {
        SS sourceFutureState = sourceFutureStateMap.get(edge.source());
        if (sourceFutureState == null || Objects.equals(sourceStateAccessor.apply(source), sourceFutureState)) {
            return false;
        }

        TS desiredTargetFutureState = desiredTargetFutureStateProvider.apply(target);
        if (desiredTargetFutureState == null) {
            return false;
        }

        TS targetFutureState = targetFutureStateMap.get(edge.target());
        if (targetFutureState == null || !Objects.equals(targetStateAccessor.apply(target), targetFutureState)) {
            return false;
        }

        for (BieStateTransitionRule stateTransitionRule : stateTransitionRules) {
            try {
                stateTransitionRule.validate(
                        sourceCarrierFactory.apply(source, sourceFutureState),
                        targetCarrierFactory.apply(target, targetFutureState),
                        edge.dependency());
            } catch (BieStateTransitionRuleViolationException e) {
                targetFutureStateMap.put(edge.target(), desiredTargetFutureState);
                return !Objects.equals(targetFutureState, desiredTargetFutureState);
            }
        }
        return false;
    }

    /**
     * Builds the direct distance-1 dependency edges evaluated by transition
     * rules. Reuse and inheritance are emitted in both directions so rules can
     * describe the user action from either side of the relationship.
     */
    private List<BieStateTransitionEdge<TopLevelAsbiepId, TopLevelAsbiepId>> buildStateTransitionEdges(
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds) {
        LinkedHashMap<String, BieStateTransitionEdge<TopLevelAsbiepId, TopLevelAsbiepId>> edgeMap = new LinkedHashMap<>();

        for (TopLevelAsbiepId sourceTopLevelAsbiepId : associatedTopLevelAsbiepIds) {
            topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(sourceTopLevelAsbiepId).stream()
                    .filter(target -> associatedTopLevelAsbiepIds.contains(target.topLevelAsbiepId()))
                    .forEach(target -> {
                        String edgeKey = sourceTopLevelAsbiepId + "|REUSES|" + target.topLevelAsbiepId();
                        edgeMap.putIfAbsent(edgeKey, new BieStateTransitionEdge(
                                sourceTopLevelAsbiepId,
                                target.topLevelAsbiepId(),
                                BieStateTransitionDependency.REUSES));
                        String reverseEdgeKey = target.topLevelAsbiepId() + "|REUSED_BY|" + sourceTopLevelAsbiepId;
                        edgeMap.putIfAbsent(reverseEdgeKey, new BieStateTransitionEdge(
                                target.topLevelAsbiepId(),
                                sourceTopLevelAsbiepId,
                                BieStateTransitionDependency.REUSED_BY));
                    });

            TopLevelAsbiepSummaryRecord sourceTopLevelAsbiep = topLevelAsbiepMap.get(sourceTopLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    (sourceTopLevelAsbiep != null)
                            ? topLevelAsbiepQuery.getTopLevelAsbiepSummary(sourceTopLevelAsbiep.basedTopLevelAsbiepId())
                            : null;
            if (basedTopLevelAsbiep != null && associatedTopLevelAsbiepIds.contains(basedTopLevelAsbiep.topLevelAsbiepId())) {
                String edgeKey = sourceTopLevelAsbiepId + "|INHERITS_FROM|" + basedTopLevelAsbiep.topLevelAsbiepId();
                edgeMap.putIfAbsent(edgeKey, new BieStateTransitionEdge(
                        sourceTopLevelAsbiepId,
                        basedTopLevelAsbiep.topLevelAsbiepId(),
                        BieStateTransitionDependency.INHERITS_FROM));
                String reverseEdgeKey = basedTopLevelAsbiep.topLevelAsbiepId() + "|IS_A_BASED_OF|" + sourceTopLevelAsbiepId;
                edgeMap.putIfAbsent(reverseEdgeKey, new BieStateTransitionEdge(
                        basedTopLevelAsbiep.topLevelAsbiepId(),
                        sourceTopLevelAsbiepId,
                        BieStateTransitionDependency.IS_A_BASED_OF));
            }
        }

        return new ArrayList<>(edgeMap.values());
    }

    /**
     * Loads the distinct code lists assigned anywhere inside the visible BIE
     * graph.
     */
    private Map<CodeListManifestId, CodeListSummaryRecord> buildCodeListMap(
            ScoreUser requester,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds,
            Set<CodeListManifestId> requestedCodeListManifestIds) {
        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);

        LinkedHashMap<CodeListManifestId, CodeListSummaryRecord> codeListMap =
                buildAssignedCodeListRecords(requester, associatedTopLevelAsbiepIds).stream()
                .map(BieCodeListStateDependencyRecord::codeListManifestId)
                .distinct()
                .map(codeListQuery::getCodeListSummary)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        CodeListSummaryRecord::codeListManifestId,
                        codeList -> codeList,
                        (left, right) -> left,
                        LinkedHashMap::new));

        for (CodeListManifestId requestedCodeListManifestId : requestedCodeListManifestIds) {
            CodeListSummaryRecord codeList = codeListQuery.getCodeListSummary(requestedCodeListManifestId);
            if (codeList != null) {
                codeListMap.putIfAbsent(requestedCodeListManifestId, codeList);
            }
        }

        return codeListMap;
    }

    /**
     * Collects assigned code-list references from BBIE and BBIE_SC nodes under
     * the visible BIE graph.
     */
    private List<BieCodeListStateDependencyRecord> buildAssignedCodeListRecords(
            ScoreUser requester,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds) {
        return associatedTopLevelAsbiepIds.stream()
                .flatMap(topLevelAsbiepId -> Stream.concat(
                        repositoryFactory.bbieQueryRepository(requester)
                                .getAssignedCodeListSummaryList(topLevelAsbiepId).stream(),
                        repositoryFactory.bbieScQueryRepository(requester)
                                .getAssignedCodeListSummaryList(topLevelAsbiepId).stream()))
                .collect(Collectors.toMap(
                        BieCodeListStateDependencyRecord::codeListManifestId,
                        record -> record,
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values().stream()
                .toList();
    }

    /**
     * Builds the directed BIE-to-code-list edges used by code-list transition
     * rules.
     */
    private List<BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId>> buildCodeListStateTransitionEdges(
            ScoreUser requester,
            Set<TopLevelAsbiepId> associatedTopLevelAsbiepIds,
            Set<CodeListManifestId> associatedCodeListManifestIds) {
        LinkedHashMap<String, BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId>> edgeMap = new LinkedHashMap<>();

        for (TopLevelAsbiepId sourceTopLevelAsbiepId : associatedTopLevelAsbiepIds) {
            Stream.concat(
                            repositoryFactory.bbieQueryRepository(requester)
                                    .getAssignedCodeListSummaryList(sourceTopLevelAsbiepId).stream(),
                            repositoryFactory.bbieScQueryRepository(requester)
                                    .getAssignedCodeListSummaryList(sourceTopLevelAsbiepId).stream())
                    .filter(record -> associatedCodeListManifestIds.contains(record.codeListManifestId()))
                    .forEach(record -> edgeMap.putIfAbsent(
                            sourceTopLevelAsbiepId + "|USES_CODE_LIST|" + record.codeListManifestId(),
                            new BieStateTransitionEdge<>(
                                    sourceTopLevelAsbiepId,
                                    record.codeListManifestId(),
                                    BieStateTransitionDependency.USES_CODE_LIST)));
        }

        return new ArrayList<>(edgeMap.values());
    }

    private List<BieStateTransitionEdge<CodeListManifestId, TopLevelAsbiepId>> buildUsedByBieCodeListStateTransitionEdges(
            List<BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId>> codeListStateTransitionEdges) {
        return codeListStateTransitionEdges.stream()
                .map(edge -> new BieStateTransitionEdge<>(
                        edge.target(),
                        edge.source(),
                        BieStateTransitionDependency.USED_BY_BIE))
                .toList();
    }

    private Map<CodeListManifestId, Boolean> buildCodeListDependencyUpdateAllowedMap(
            ScoreUser requester,
            Collection<CodeListSummaryRecord> codeLists,
            BieState nextState) {
        Map<CodeListManifestId, Boolean> updateAllowedMap = new LinkedHashMap<>();
        for (CodeListSummaryRecord codeList : codeLists) {
            if (codeList == null) {
                continue;
            }
            updateAllowedMap.put(
                    codeList.codeListManifestId(),
                    isCodeListDependencyUpdateAllowed(requester, codeList, nextState));
        }
        return updateAllowedMap;
    }

    /**
     * Builds the dialog-facing dependency labels for each visible code list.
     */
    private Map<CodeListManifestId, List<BieStateDependencyRelation>> buildCodeListDependenciesRelationMap(
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            List<BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId>> codeListStateTransitionEdges) {
        Map<CodeListManifestId, LinkedHashMap<String, BieStateDependencyRelation>> relationMap = new LinkedHashMap<>();

        for (BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId> edge : codeListStateTransitionEdges) {
            TopLevelAsbiepSummaryRecord source = topLevelAsbiepMap.get(edge.source());
            if (source == null) {
                continue;
            }

            relationMap.computeIfAbsent(edge.target(), key -> new LinkedHashMap<>())
                    .putIfAbsent(
                            toNodeKey(source.topLevelAsbiepId()),
                            toBieStateDependencyRelation(source, BieStateTransitionDependency.USED_BY_BIE));
        }

        return relationMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue().values()),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    /**
     * Derives code-list edge distances from the already-computed BIE graph
     * distances.
     */
    private Map<CodeListManifestId, Integer> buildCodeListEdgeDistanceMap(
            Set<CodeListManifestId> requestedCodeListManifestIds,
            Map<TopLevelAsbiepId, Integer> bieEdgeDistanceMap,
            List<BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId>> codeListStateTransitionEdges) {
        Map<CodeListManifestId, Integer> edgeDistanceMap = new LinkedHashMap<>();

        for (CodeListManifestId requestedCodeListManifestId : requestedCodeListManifestIds) {
            edgeDistanceMap.put(requestedCodeListManifestId, 0);
        }

        for (BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId> edge : codeListStateTransitionEdges) {
            int sourceDistance = bieEdgeDistanceMap.getOrDefault(edge.source(), Integer.MAX_VALUE);
            if (sourceDistance == Integer.MAX_VALUE) {
                continue;
            }
            edgeDistanceMap.merge(edge.target(), sourceDistance + 1, Math::min);
        }

        return edgeDistanceMap;
    }

    /**
     * Evaluates every BIE-to-BIE rule edge against the actual projected future
     * states and updates BIE rows with any direct compatibility issues.
     */
    private void applyStateTransitionRules(ScoreUser requester,
                                           List<BieStateDependencyTarget> targets,
                                           BieStateDependencyGraph dependencyGraph,
                                           BieState nextState,
                                           BieStateFutureStateProjection futureStateProjection,
                                           Collection<CodeListManifestId> selectedCodeListManifestIds) {
        if (targets == null) {
            return;
        }

        Map<TopLevelAsbiepId, BieState> actualFutureStateMap = futureStateProjection.actualFutureStateMap();
        Map<TopLevelAsbiepId, BieState> requiredFutureStateMap = futureStateProjection.requiredFutureStateMap();

        Map<TopLevelAsbiepId, BieStateDependencyTarget> targetMap = targets.stream()
                .filter(target -> target.getNodeType() == BieStateDependencyNodeType.BIE)
                .collect(LinkedHashMap::new,
                        (map, target) -> map.put(target.getTopLevelAsbiepId(), target),
                        LinkedHashMap::putAll);

        applyDirectBieIssues(requester, targetMap, dependencyGraph, nextState, actualFutureStateMap, requiredFutureStateMap);

        for (Map.Entry<TopLevelAsbiepId, BieStateDependencyTarget> entry : targetMap.entrySet()) {
            entry.getValue().setIssues(removeIssuesOfType(
                    entry.getValue().getIssues(), BieStateDependencyIssueType.DEPENDENCY_CONFLICT));
        }

        applyBieDependencyConflictIssues(targetMap, dependencyGraph, nextState, requiredFutureStateMap);
        if (isDiscardStateTransition(nextState) ||
                (dependencyGraph.codeListStateTransitionEdges().isEmpty() &&
                        dependencyGraph.requestedCodeListManifestIds().isEmpty())) {
            return;
        }

        Map<TopLevelAsbiepId, BieStateDependencyTarget> bieTargetMap = targets.stream()
                .filter(target -> target.getNodeType() == BieStateDependencyNodeType.BIE)
                .filter(target -> target.getTopLevelAsbiepId() != null)
                .collect(Collectors.toMap(
                        BieStateDependencyTarget::getTopLevelAsbiepId,
                        target -> target,
                        (left, right) -> left,
                        LinkedHashMap::new));
        LinkedHashMap<CodeListManifestId, BieStateDependencyTarget> codeListTargetMap = new LinkedHashMap<>();
        Map<CodeListManifestId, CcState> actualCodeListFutureStateMap = futureStateProjection.actualCodeListFutureStateMap();
        Map<CodeListManifestId, CcState> requiredCodeListFutureStateMap = futureStateProjection.requiredCodeListFutureStateMap();
        Set<CodeListManifestId> requestedAndSelectedCodeListIdSet =
                new LinkedHashSet<>(dependencyGraph.requestedCodeListManifestIds());
        requestedAndSelectedCodeListIdSet.addAll(normalizeSelectedCodeListManifestIds(
                dependencyGraph,
                buildNextCodeListStateMap(dependencyGraph.codeListMap(), nextState),
                (selectedCodeListManifestIds != null)
                        ? new LinkedHashSet<>(selectedCodeListManifestIds)
                        : Collections.emptySet()));

        for (BieStateTransitionEdge<TopLevelAsbiepId, CodeListManifestId> edge : dependencyGraph.codeListStateTransitionEdges()) {
            TopLevelAsbiepSummaryRecord sourceBie = dependencyGraph.topLevelAsbiepMap().get(edge.source());
            CodeListSummaryRecord codeList = dependencyGraph.codeListMap().get(edge.target());
            BieStateDependencyTarget bieTarget = bieTargetMap.get(edge.source());
            if (sourceBie == null || codeList == null || codeList.state() == null) {
                continue;
            }

            BieState sourceFutureState = futureStateProjection.requiredFutureStateMap().get(edge.source());
            boolean selected = requestedAndSelectedCodeListIdSet.contains(codeList.codeListManifestId());
            String stateTransitionMessage = null;
            if (sourceFutureState != null && !Objects.equals(sourceBie.state(), sourceFutureState)) {
                BieFutureStateCarrier source = new BieFutureStateCarrier(sourceBie, sourceFutureState);
                CcState codeListFutureState = actualCodeListFutureStateMap.getOrDefault(
                        codeList.codeListManifestId(),
                        codeList.state());
                CodeListFutureStateCarrier target = new CodeListFutureStateCarrier(codeList, codeListFutureState);
                for (BieStateTransitionRule rule : stateTransitionRules) {
                    try {
                        rule.validate(source, target, edge.dependency());
                    } catch (BieStateTransitionRuleViolationException e) {
                        stateTransitionMessage = buildStateTransitionMessage(edge.dependency(), sourceFutureState, codeList);
                        break;
                    }
                }
            }

            BieStateDependencyTarget candidate = toCodeListStateDependencyTarget(
                    requester,
                    codeList,
                    nextState,
                    selected,
                    dependencyGraph.codeListDependenciesRelationMap().getOrDefault(
                            codeList.codeListManifestId(), Collections.emptyList()),
                    dependencyGraph.codeListEdgeDistanceMap().getOrDefault(
                            codeList.codeListManifestId(), Integer.MAX_VALUE),
                    stateTransitionMessage,
                    actualCodeListFutureStateMap.getOrDefault(codeList.codeListManifestId(), codeList.state()),
                    requiredCodeListFutureStateMap.getOrDefault(codeList.codeListManifestId(), codeList.state()));
            codeListTargetMap.merge(codeList.codeListManifestId(), candidate, (current, next) -> {
                current.setChecked(current.isChecked() || next.isChecked());
                current.setIssues(mergeIssues(current.getIssues(), next.getIssues()));
                return current;
            });

            if (!candidate.getIssues().isEmpty() &&
                    bieTarget != null &&
                    sourceFutureState != null &&
                    !Objects.equals(sourceBie.state(), sourceFutureState) &&
                    !hasIssueType(bieTarget.getIssues(), BieStateDependencyIssueType.OWNERSHIP)) {
                bieTarget.setIssues(addIssue(
                        bieTarget.getIssues(),
                        new BieStateDependencyIssue(
                                BieStateDependencyIssueType.DEPENDENCY_CONFLICT,
                                buildAssignedCodeListDependencyConflictMessage(sourceFutureState, codeList, candidate))));
            }
        }

        for (CodeListManifestId selectedCodeListManifestId : requestedAndSelectedCodeListIdSet) {
            if (codeListTargetMap.containsKey(selectedCodeListManifestId)) {
                continue;
            }
            CodeListSummaryRecord codeList = dependencyGraph.codeListMap().get(selectedCodeListManifestId);
            if (codeList == null) {
                continue;
            }
            CcState codeListFutureState = actualCodeListFutureStateMap.getOrDefault(
                    selectedCodeListManifestId,
                    codeList.state());
            codeListTargetMap.put(selectedCodeListManifestId, toCodeListStateDependencyTarget(
                    requester,
                    codeList,
                    nextState,
                    true,
                    dependencyGraph.codeListDependenciesRelationMap().getOrDefault(
                            codeList.codeListManifestId(), Collections.emptyList()),
                    dependencyGraph.codeListEdgeDistanceMap().getOrDefault(
                            codeList.codeListManifestId(), Integer.MAX_VALUE),
                    null,
                    codeListFutureState,
                    requiredCodeListFutureStateMap.getOrDefault(codeList.codeListManifestId(), codeList.state())));
        }

        targets.addAll(codeListTargetMap.values());
    }

    private void applyDirectBieIssues(ScoreUser requester,
                                      Map<TopLevelAsbiepId, BieStateDependencyTarget> targetMap,
                                      BieStateDependencyGraph dependencyGraph,
                                      BieState nextState,
                                      Map<TopLevelAsbiepId, BieState> futureStateMap,
                                      Map<TopLevelAsbiepId, BieState> requiredFutureStateMap) {
        for (Map.Entry<TopLevelAsbiepId, BieStateDependencyTarget> entry : targetMap.entrySet()) {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = dependencyGraph.topLevelAsbiepMap().get(entry.getKey());
            BieStateDependencyTarget row = entry.getValue();
            if (topLevelAsbiep == null || topLevelAsbiep.state() == null || nextState == null) {
                continue;
            }

            List<BieStateDependencyIssue> issues = removeIssuesOfType(
                    removeIssuesOfType(row.getIssues(), BieStateDependencyIssueType.OWNERSHIP),
                    BieStateDependencyIssueType.STATE_COMPATIBILITY);
            BieState futureState = futureStateMap.get(entry.getKey());
            BieState requiredFutureState = requiredFutureStateMap.get(entry.getKey());
            boolean requiredStateChange = requiredFutureState != null &&
                    !Objects.equals(topLevelAsbiep.state(), requiredFutureState);
            if (!requiredStateChange) {
                row.setIssues(issues);
                continue;
            }
            boolean stateChangeNeeded = shouldApplyDependencyStateChange(requester, topLevelAsbiep.state(), nextState);
            boolean compatible = futureState != null &&
                    requiredBieStatesForTransition(nextState).contains(futureState);

            if (!compatible) {
                issues = addIssue(issues, new BieStateDependencyIssue(
                        row.isSelectable() || !stateChangeNeeded
                                ? BieStateDependencyIssueType.STATE_COMPATIBILITY
                                : BieStateDependencyIssueType.OWNERSHIP,
                        row.isSelectable() || !stateChangeNeeded
                                ? buildBieComparableStateMessage(nextState)
                                : buildBieOwnershipMessage(topLevelAsbiep, nextState)));
            }

            row.setIssues(issues);
        }
    }

    private void applyBieDependencyConflictIssues(Map<TopLevelAsbiepId, BieStateDependencyTarget> targetMap,
                                                  BieStateDependencyGraph dependencyGraph,
                                                  BieState nextState,
                                                  Map<TopLevelAsbiepId, BieState> requiredFutureStateMap) {
        Map<TopLevelAsbiepId, Boolean> descendantIssueMap = new HashMap<>();
        for (TopLevelAsbiepId topLevelAsbiepId : targetMap.keySet()) {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = dependencyGraph.topLevelAsbiepMap().get(topLevelAsbiepId);
            BieState requiredFutureState = requiredFutureStateMap.get(topLevelAsbiepId);
            if (topLevelAsbiep == null || topLevelAsbiep.state() == null ||
                    requiredFutureState == null ||
                    Objects.equals(topLevelAsbiep.state(), requiredFutureState)) {
                continue;
            }
            if (hasDependentIssues(topLevelAsbiepId, dependencyGraph, targetMap, descendantIssueMap, new HashSet<>())) {
                BieStateDependencyTarget row = targetMap.get(topLevelAsbiepId);
                row.setIssues(addIssue(
                        row.getIssues(),
                        new BieStateDependencyIssue(
                                BieStateDependencyIssueType.DEPENDENCY_CONFLICT,
                                buildBieDependencyConflictMessage(nextState))));
            }
        }
    }

    private boolean hasDependentIssues(TopLevelAsbiepId topLevelAsbiepId,
                                       BieStateDependencyGraph dependencyGraph,
                                       Map<TopLevelAsbiepId, BieStateDependencyTarget> targetMap,
                                       Map<TopLevelAsbiepId, Boolean> descendantIssueMap,
                                       Set<TopLevelAsbiepId> visitedTopLevelAsbiepIds) {
        if (descendantIssueMap.containsKey(topLevelAsbiepId)) {
            return descendantIssueMap.get(topLevelAsbiepId);
        }
        if (!visitedTopLevelAsbiepIds.add(topLevelAsbiepId)) {
            return false;
        }

        boolean hasIssues = false;
        for (TopLevelAsbiepId childTopLevelAsbiepId : dependencyGraph.childMap().getOrDefault(topLevelAsbiepId, Collections.emptySet())) {
            BieStateDependencyTarget childRow = targetMap.get(childTopLevelAsbiepId);
            if (childRow == null) {
                continue;
            }
            if (!childRow.getIssues().isEmpty() ||
                    hasDependentIssues(childTopLevelAsbiepId, dependencyGraph, targetMap, descendantIssueMap, visitedTopLevelAsbiepIds)) {
                hasIssues = true;
                break;
            }
        }

        visitedTopLevelAsbiepIds.remove(topLevelAsbiepId);
        descendantIssueMap.put(topLevelAsbiepId, hasIssues);
        return hasIssues;
    }

    /**
     * Produces the user-facing validation text for one violated dependency
     * edge. Rules themselves only report that an edge is invalid; wording stays
     * centralized here so the UI can remain consistent across rules.
     */
    private String buildStateTransitionMessage(BieStateTransitionDependency dependency,
                                               BieState sourceFutureState,
                                               CodeListSummaryRecord target) {
        return switch (dependency) {
            case USES_CODE_LIST, USED_BY_BIE -> buildCodeListComparableStateMessage(sourceFutureState, target);
            default -> buildCodeListComparableStateMessage(sourceFutureState, target);
        };
    }

    /**
     * Returns the preferred display label for one BIE row.
     */
    private String toName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return hasLength(topLevelAsbiep.displayName()) ?
                topLevelAsbiep.displayName() :
                topLevelAsbiep.propertyTerm();
    }

    /**
     * Projects one code-list dependency into the shared dialog row transport
     * model.
     */
    private BieStateDependencyTarget toCodeListStateDependencyTarget(
            ScoreUser requester,
            CodeListSummaryRecord codeList,
            BieState nextState,
            boolean checked,
            List<BieStateDependencyRelation> dependencies,
            int edgeDistance,
            String stateTransitionMessage,
            CcState actualFutureState,
            CcState requiredFutureState) {
        AgencyIdListValueSummaryRecord agencyIdListValue = null;
        if (codeList.agencyIdListValueManifestId() != null) {
            agencyIdListValue = repositoryFactory.agencyIdListQueryRepository(requester)
                    .getAgencyIdListValueSummary(codeList.agencyIdListValueManifestId());
        }
        boolean dependencyUpdateAllowed = isCodeListDependencyUpdateAllowed(requester, codeList, nextState);
        return new BieStateDependencyTarget(
                toNodeKey(codeList.codeListManifestId()),
                BieStateDependencyNodeType.CODE_LIST,
                null,
                codeList.codeListManifestId(),
                Collections.emptyList(),
                dependencies,
                edgeDistance,
                null,
                codeList.listId(),
                null,
                codeList.name(),
                (codeList.guid() != null) ? codeList.guid().value() : null,
                ownerLoginId(codeList),
                (agencyIdListValue != null) ? agencyIdListValue.value() : null,
                (agencyIdListValue != null) ? agencyIdListValue.name() : null,
                Collections.emptyList(),
                codeList.versionId(),
                null,
                null,
                (codeList.state() != null) ? codeList.state().name() : null,
                dependencyUpdateAllowed,
                dependencyUpdateAllowed &&
                        codeList.state() != null &&
                        requiredFutureState != null &&
                        codeList.state() != requiredFutureState,
                checked && dependencyUpdateAllowed,
                buildCodeListIssues(requester, codeList, nextState, actualFutureState, stateTransitionMessage)
        );
    }

    /**
     * Builds the stable dialog row key for one BIE node.
     */
    private String toNodeKey(TopLevelAsbiepId topLevelAsbiepId) {
        return "BIE:" + topLevelAsbiepId;
    }

    /**
     * Builds the stable dialog row key for one code-list node.
     */
    private String toNodeKey(CodeListManifestId codeListManifestId) {
        return "CODE_LIST:" + codeListManifestId;
    }

    private String ownerLoginId(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (topLevelAsbiep == null || topLevelAsbiep.owner() == null) {
            return null;
        }
        return hasLength(topLevelAsbiep.owner().loginId()) ? topLevelAsbiep.owner().loginId() : null;
    }

    private String ownerLoginId(CodeListSummaryRecord codeList) {
        if (codeList == null || codeList.owner() == null) {
            return null;
        }
        return hasLength(codeList.owner().loginId()) ? codeList.owner().loginId() : null;
    }

    /**
     * Formats a human-readable state list used in validation messages.
     */
    private String toQuotedStateList(List<CcState> states) {
        if (states == null || states.isEmpty()) {
            return "a compatible state";
        }
        if (states.size() == 1) {
            return "'" + states.get(0) + "'";
        }
        if (states.size() == 2) {
            return "'" + states.get(0) + "' or '" + states.get(1) + "'";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < states.size(); i++) {
            if (i > 0) {
                builder.append(i == states.size() - 1 ? ", or " : ", ");
            }
            builder.append("'").append(states.get(i)).append("'");
        }
        return builder.toString();
    }

    /**
     * Formats a human-readable BIE state list used in dependency messages.
     */
    private String toQuotedBieStateList(List<BieState> states) {
        if (states == null || states.isEmpty()) {
            return "a compatible state";
        }
        if (states.size() == 1) {
            return "'" + states.get(0) + "'";
        }
        if (states.size() == 2) {
            return "'" + states.get(0) + "' or '" + states.get(1) + "'";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < states.size(); i++) {
            if (i > 0) {
                builder.append(i == states.size() - 1 ? ", or " : ", ");
            }
            builder.append("'").append(states.get(i)).append("'");
        }
        return builder.toString();
    }

    /**
     * Returns whether the current user may cascade this code list together
     * with the requested BIE transition.
     */
    private boolean isCodeListDependencyUpdateAllowed(ScoreUser requester,
                                                      CodeListSummaryRecord codeList,
                                                      BieState nextState) {
        if (requester == null || codeList == null || nextState == null ||
                codeList.owner() == null || !requester.userId().equals(codeList.owner().userId())) {
            return false;
        }
        CcState targetState = nextCodeListState(nextState, codeList);
        return targetState != null &&
                codeList.state() != null &&
                (codeList.state() == targetState || codeList.state().canMove(targetState));
    }

    /**
     * Returns whether a dependency row would actually change state if it
     * participates in the requested transition.
     */
    private boolean shouldApplyDependencyStateChange(ScoreUser requester, BieState currentState, BieState nextState) {
        if (currentState == null || nextState == null) {
            return false;
        }
        if (isAdminDiscardTransition(requester, nextState)) {
            return currentState != BieState.Initiating;
        }
        return currentState.canMove(nextState);
    }

    private boolean isAdminDiscardTransition(ScoreUser requester, BieState nextState) {
        return isDiscardStateTransition(nextState) &&
                requester != null &&
                requester.hasRole(ADMINISTRATOR);
    }

    /**
     * Returns the prerequisite state a dependency must already have before it
     * can be promoted into the requested next state.
     */
    private BieState requiredState(BieState state) {
        if (state == BieState.Production) {
            return BieState.QA;
        }
        if (state == BieState.QA) {
            return BieState.WIP;
        }
        return state;
    }

    private BieEditTreeController getTreeController(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        DefaultBieEditTreeController bieEditTreeController =
                applicationContext.getBean(DefaultBieEditTreeController.class);

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
        bieEditTreeController.initialize(requester, topLevelAsbiep);

        return bieEditTreeController;
    }

    private void ensureBieRelationshipsForChangingState(ScoreUser requester,
                                                        TopLevelAsbiepId topLevelAsbiepId,
                                                        BieState state) {
        StringBuilder failureMessageBody = new StringBuilder();
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        if (state == BieState.WIP) {
            List<TopLevelAsbiepSummaryRecord> reusedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                            .filter(e -> !e.owner().userId().equals(requester.userId()))
                            .filter(e -> e.state().getLevel() > state.getLevel())
                            .collect(Collectors.toList());
            if (!reusedTopLevelAsbiepList.isEmpty()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following reusing BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : reusedTopLevelAsbiepList) {
                    failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        } else {
            List<TopLevelAsbiepSummaryRecord> reusingTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                            .filter(e -> !e.owner().userId().equals(requester.userId()))
                            .filter(e -> e.state().getLevel() < state.getLevel())
                            .collect(Collectors.toList());

            if (!reusingTopLevelAsbiepList.isEmpty()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following reused BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : reusingTopLevelAsbiepList) {
                    failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        }

        if (state == BieState.WIP) {
            List<TopLevelAsbiepSummaryRecord> derivedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId).stream()
                            .filter(e -> !e.owner().userId().equals(requester.userId()))
                            .filter(e -> e.state().getLevel() > state.getLevel())
                            .collect(Collectors.toList());
            if (!derivedTopLevelAsbiepList.isEmpty()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following inherited BIEs:")
                        .append("\n\n");
                for (TopLevelAsbiepSummaryRecord target : derivedTopLevelAsbiepList) {
                    failureMessageBody.append("- [")
                            .append(target.propertyTerm())
                            .append("](")
                            .append("/profile_bie/").append(target.topLevelAsbiepId())
                            .append(") (")
                            .append(target.guid())
                            .append(") - ")
                            .append(target.state()).append(" state")
                            .append("\n");
                }
            }
        } else {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiep.basedTopLevelAsbiepId());

            if (basedTopLevelAsbiep != null &&
                    !requester.userId().equals(basedTopLevelAsbiep.owner().userId()) &&
                    basedTopLevelAsbiep.state().getLevel() < state.getLevel()) {
                var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
                failureMessageBody.append("\n---\n[**")
                        .append(source.get(ASCCP.PROPERTY_TERM))
                        .append("**](")
                        .append("/profile_bie/").append(topLevelAsbiepId)
                        .append(") (")
                        .append(source.get(ASBIEP.GUID))
                        .append(") cannot move to ")
                        .append(state)
                        .append(" because the following base BIE:")
                        .append("\n\n")
                        .append("- [")
                        .append(basedTopLevelAsbiep.propertyTerm())
                        .append("](")
                        .append("/profile_bie/").append(basedTopLevelAsbiep.topLevelAsbiepId())
                        .append(") (")
                        .append(basedTopLevelAsbiep.guid())
                        .append(") - ")
                        .append(basedTopLevelAsbiep.state()).append(" state")
                        .append("\n");
            }
        }

        ensureSameOwnerDependencyTargetsForChangingState(
                requester, topLevelAsbiepId, state, failureMessageBody, topLevelAsbiepQuery);

        if (failureMessageBody.length() > 0) {
            throw new DataAccessForbiddenException(toPlainTextStateFailureMessage(
                    "Failed to update BIE state", failureMessageBody.toString()));
        }
    }

    private void ensureCodeListDependenciesForChangingState(ScoreUser requester,
                                                            Collection<TopLevelAsbiepId> topLevelAsbiepIds,
                                                            BieState state,
                                                            Collection<CodeListManifestId> dependencyCodeListManifestIds) {
        if (topLevelAsbiepIds == null || topLevelAsbiepIds.isEmpty() || state == null ||
                isDiscardStateTransition(state)) {
            return;
        }

        StringBuilder failureMessageBody = new StringBuilder();
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        Set<CodeListManifestId> selectedCodeListIdSet = (dependencyCodeListManifestIds != null)
                ? new HashSet<>(dependencyCodeListManifestIds) : Collections.emptySet();

        for (TopLevelAsbiepId topLevelAsbiepId : new LinkedHashSet<>(topLevelAsbiepIds)) {
            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
            if (topLevelAsbiep == null) {
                continue;
            }

            List<CodeListSummaryRecord> blockingCodeLists =
                    Stream.concat(
                                    repositoryFactory.bbieQueryRepository(requester)
                                            .getAssignedCodeListSummaryList(topLevelAsbiepId).stream(),
                                    repositoryFactory.bbieScQueryRepository(requester)
                                            .getAssignedCodeListSummaryList(topLevelAsbiepId).stream())
                            .map(BieCodeListStateDependencyRecord::codeListManifestId)
                            .collect(Collectors.toMap(
                                    codeListManifestId -> codeListManifestId,
                                    codeListQuery::getCodeListSummary,
                                    (left, right) -> left,
                                    LinkedHashMap::new))
                            .values().stream()
                            .filter(Objects::nonNull)
                            .filter(codeList -> codeList.state() != null)
                            .filter(codeList -> !(selectedCodeListIdSet.contains(codeList.codeListManifestId()) &&
                                    isCodeListDependencyUpdateAllowed(requester, codeList, state)))
                            .filter(codeList -> !CodeListStateLevel.isCompatible(state, codeList))
                            .sorted(Comparator.comparing(CodeListSummaryRecord::name,
                                    Comparator.nullsLast(String::compareToIgnoreCase)))
                            .toList();
            if (blockingCodeLists.isEmpty()) {
                continue;
            }

            failureMessageBody.append("\n---\n[**")
                    .append(toName(topLevelAsbiep))
                    .append("**](")
                    .append("/profile_bie/").append(topLevelAsbiep.topLevelAsbiepId())
                    .append(") (")
                    .append(topLevelAsbiep.guid())
                    .append(") cannot move to ")
                    .append(state)
                    .append(" because the following assigned code lists are in incompatible states:")
                    .append("\n\n");
            for (CodeListSummaryRecord codeList : blockingCodeLists) {
                failureMessageBody.append("- ")
                        .append(codeList.name())
                        .append(" - ")
                        .append(codeList.state()).append(" state")
                        .append("\n");
            }
        }

        if (failureMessageBody.length() > 0) {
            throw new DataAccessForbiddenException(toPlainTextStateFailureMessage(
                    "Failed to update BIE state", failureMessageBody.toString()));
        }
    }

    private void ensureSameOwnerDependencyTargetsForChangingState(ScoreUser requester,
                                                                  TopLevelAsbiepId topLevelAsbiepId,
                                                                  BieState state,
                                                                  StringBuilder failureMessageBody,
                                                                  TopLevelAsbiepQueryRepository topLevelAsbiepQuery) {
        if (state == null || state.getLevel() <= BieState.WIP.getLevel()) {
            return;
        }

        int requiredLevel = state.getLevel() - 1;
        Set<TopLevelAsbiepId> visitedTopLevelAsbiepIds = new HashSet<>();
        Queue<TopLevelAsbiepSummaryRecord> topLevelAsbiepQueue = new LinkedList<>();
        Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> blockingTargetMap = new LinkedHashMap<>();

        TopLevelAsbiepSummaryRecord rootTopLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
        if (rootTopLevelAsbiep == null) {
            return;
        }
        topLevelAsbiepQueue.offer(rootTopLevelAsbiep);

        while (!topLevelAsbiepQueue.isEmpty()) {
            TopLevelAsbiepSummaryRecord currentTopLevelAsbiep = topLevelAsbiepQueue.poll();
            if (!visitedTopLevelAsbiepIds.add(currentTopLevelAsbiep.topLevelAsbiepId())) {
                continue;
            }

            List<TopLevelAsbiepSummaryRecord> dependencyTargets = new ArrayList<>();
            dependencyTargets.addAll(topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(
                    currentTopLevelAsbiep.topLevelAsbiepId()));
            TopLevelAsbiepSummaryRecord basedTopLevelAsbiep =
                    topLevelAsbiepQuery.getTopLevelAsbiepSummary(currentTopLevelAsbiep.basedTopLevelAsbiepId());
            if (basedTopLevelAsbiep != null) {
                dependencyTargets.add(basedTopLevelAsbiep);
            }

            dependencyTargets.stream()
                    .filter(target -> currentTopLevelAsbiep.owner().userId().equals(target.owner().userId()))
                    .forEach(target -> {
                        topLevelAsbiepQueue.offer(target);
                        if (!topLevelAsbiepId.equals(target.topLevelAsbiepId()) &&
                                shouldApplyDependencyStateChange(requester, target.state(), state) &&
                                target.state().getLevel() < requiredLevel) {
                            blockingTargetMap.putIfAbsent(target.topLevelAsbiepId(), target);
                        }
                    });
        }

        if (!blockingTargetMap.isEmpty()) {
            var source = bieService.selectAsccpPropertyTermAndAsbiepGuidByTopLevelAsbiepId(topLevelAsbiepId);
            failureMessageBody.append("\n---\n[**")
                    .append(source.get(ASCCP.PROPERTY_TERM))
                    .append("**](")
                    .append("/profile_bie/").append(topLevelAsbiepId)
                    .append(") (")
                    .append(source.get(ASBIEP.GUID))
                    .append(") cannot move to ")
                    .append(state)
                    .append(" because the following same-owner reused or inherited BIEs must already be in ")
                    .append(requiredState(state))
                    .append(" state:")
                    .append("\n\n");
            for (TopLevelAsbiepSummaryRecord target : blockingTargetMap.values().stream()
                    .sorted(Comparator.comparing(TopLevelAsbiepSummaryRecord::propertyTerm)
                            .thenComparing(target -> target.guid().value()))
                    .toList()) {
                failureMessageBody.append("- [")
                        .append(target.propertyTerm())
                        .append("](")
                        .append("/profile_bie/").append(target.topLevelAsbiepId())
                        .append(") (")
                        .append(target.guid())
                        .append(") - ")
                        .append(target.state()).append(" state")
                        .append("\n");
            }
        }
    }

    public void ensureDependencySelectionStateChange(ScoreUser requester,
                                                     Collection<TopLevelAsbiepId> requestedTopLevelAsbiepIds,
                                                     BieState state,
                                                     Collection<TopLevelAsbiepId> dependencyTopLevelAsbiepIds,
                                                     Collection<CodeListManifestId> dependencyCodeListManifestIds) {
        boolean hasInvalidSelection = validateStateDependencies(
                requester,
                requestedTopLevelAsbiepIds,
                Collections.emptyList(),
                state,
                dependencyTopLevelAsbiepIds,
                dependencyCodeListManifestIds).stream()
                .anyMatch(target -> target.getIssues() != null && !target.getIssues().isEmpty());
        if (hasInvalidSelection) {
            if (isDiscardStateTransition(state)) {
                String subject = (requestedTopLevelAsbiepIds != null && requestedTopLevelAsbiepIds.size() > 1)
                        ? "Selected BIEs cannot be discarded."
                        : "This BIE cannot be discarded.";
                throw new DataAccessForbiddenException("Failed to discard BIE\n" +
                        subject + " Resolve the conflicting records to continue.");
            }
            String subject = (requestedTopLevelAsbiepIds != null && requestedTopLevelAsbiepIds.size() > 1)
                    ? "Selected BIEs cannot move to '"
                    : "This BIE cannot move to '";
            throw new DataAccessForbiddenException("Failed to update BIE state\n" +
                    subject + state + "'. Resolve the conflicting records to continue.");
        }
    }

    private void updateSelectedCodeLists(ScoreUser requester,
                                         BieState bieState,
                                         Collection<CodeListManifestId> dependencyCodeListManifestIds) {
        if (dependencyCodeListManifestIds == null || dependencyCodeListManifestIds.isEmpty() || bieState == null) {
            return;
        }

        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        for (CodeListManifestId codeListManifestId : new LinkedHashSet<>(dependencyCodeListManifestIds)) {
            CodeListSummaryRecord codeList = codeListQuery.getCodeListSummary(codeListManifestId);
            if (codeList == null) {
                continue;
            }
            CcState targetState = nextCodeListState(bieState, codeList);
            if (targetState == null || codeList.state() == targetState) {
                continue;
            }
            codeListCommandService.updateState(requester, codeListManifestId, targetState);
        }
    }

    private String toPlainTextStateFailureMessage(String subject, String messageBody) {
        String plainTextMessageBody = messageBody
                .replace("\n---\n", "\n\n")
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\[(.*?)\\]\\([^)]*\\)", "$1");
        return subject + "\n" + plainTextMessageBody.trim();
    }
}
